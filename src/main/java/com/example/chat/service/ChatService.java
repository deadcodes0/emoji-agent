package com.example.chat.service;

import com.example.chat.config.PromptConfig;
import com.example.chat.dto.AiSentence;
import com.example.chat.dto.DoneEvent;
import com.example.chat.dto.SentenceEvent;
import com.example.chat.enums.Emotion;
import com.example.chat.enums.RiskLevel;
import com.example.chat.exception.ConcurrentRequestException;
import com.example.chat.utils.EmojiUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 心理健康对话 Agent：
 * LLM 流式输出 NDJSON -> 逐行解析 -> 枚举校验 + 强度 clamp + emoji 映射
 * -> 风险等级二次校验（规则引擎取更高者）-> SSE 推送 sentence / done / error。
 *
 * 历史上下文由 Spring AI 的 MessageChatMemoryAdvisor 自动管理，
 * 业务层只负责传入当前用户消息与 conversationId。
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public static final int TIMEOUT_MINUTES = 1;

    private final ChatClient chatClient;
    private final RiskService riskService;
    private final CrisisService crisisService;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    public ChatService(ChatClient chatClient,
                       RiskService riskService,
                       CrisisService crisisService,
                       ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.riskService = riskService;
        this.crisisService = crisisService;
        this.objectMapper = objectMapper;
    }

    /**
     * 每个用户的会话：持有 SSE sink、LLM 订阅句柄与 NDJSON 解析状态。
     */
    private static class Session {
        final Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().replay().all();
        volatile Disposable llmSubscription;
        /** NDJSON 行缓冲（LLM token 是碎片，需按 \n 拼行） */
        final StringBuilder buffer = new StringBuilder();
        final String userId;
        final String userMsg;
        final String requestId = UUID.randomUUID().toString();
        /** 规则引擎前置评估结果（流式开始前即已知，用于提前禁用 PLAYFULNESS） */
        final RiskLevel ruleRisk;
        volatile boolean doneEmitted = false;
        volatile int sentenceCount = 0;

        Session(String userId, String userMsg, RiskLevel ruleRisk) {
            this.userId = userId;
            this.userMsg = userMsg;
            this.ruleRisk = ruleRisk;
        }
    }

    /* ================================================================
     * SEND：发起一次对话
     * ================================================================ */

    /**
     * 发起一次对话。
     *
     * 注意：历史消息由 ChatClient 上的 MessageChatMemoryAdvisor 自动注入与写回，
     * 通过 {@link ChatMemory#CONVERSATION_ID} 参数区分会话。
     *
     * @param conversationId 会话 ID，同一会话多次调用请传相同值（例如 userId）
     * @param msg            当前用户消息
     */
    public Flux<ServerSentEvent<String>> send(String conversationId, String msg) {
        // 并发保护仍然以 conversationId 为键，避免同一用户同时发起多次请求
        Session session = new Session(conversationId, msg, riskService.evaluate(msg));
        Session existing = sessions.putIfAbsent(conversationId, session);
        if (existing != null) {
            return Flux.error(new ConcurrentRequestException(conversationId));
        }

        Disposable sub = chatClient.prompt()
                // 只传当前消息，历史由 advisor 自动从 ChatMemory 中取出并拼接
                .user(msg)
                // 指定本次请求关联的会话 ID；advisor 会据此读取/写回历史
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .take(Duration.ofMinutes(TIMEOUT_MINUTES))
                .subscribe(
                        token -> {
                            synchronized (session.buffer) {
                                session.buffer.append(token == null ? "" : token);
                            }
                            drainLines(session, false);
                        },
                        err -> {
                            log.error("LLM 流式调用失败: {}", err.getMessage());
                            emit(session, "error", Map.of(
                                    "code", "STREAM_ERROR",
                                    "message", "AI 服务暂时不可用，请稍后重试"));
                            finishSession(session);
                        },
                        () -> {
                            drainLines(session, true);
                            finalizeStream(session);
                        }
                );
        session.llmSubscription = sub;

        return session.sink.asFlux();
    }

    /* ================================================================
     * STOP / RESUME
     * ================================================================ */

    /** 用户主动停止：取消 LLM 订阅、通知订阅者流结束、移除 session */
    public void stop(String userId) {
        Session session = sessions.remove(userId);
        if (session == null) return;

        Disposable sub = session.llmSubscription;
        if (sub != null && !sub.isDisposed()) {
            sub.dispose();
        }
        session.sink.tryEmitComplete();
    }

    /** 断线恢复：回放已产生的事件并跟随后续事件 */
    public Flux<ServerSentEvent<String>> resumeAsEvents(String userId) {
        Session session = sessions.get(userId);
        if (session == null) {
            return Flux.empty();
        }

        ServerSentEvent<String> open =
                ServerSentEvent.<String>builder().event("open").data("").build();

        return Flux.concat(Flux.just(open), session.sink.asFlux());
    }

    /* ================================================================
     * NDJSON 逐行处理
     * ================================================================ */

    /** 从缓冲中取出完整行（遇 \n 切分；flush=true 时把残行也取出） */
    private void drainLines(Session s, boolean flush) {
        List<String> lines = new ArrayList<>();
        synchronized (s.buffer) {
            int idx;
            while ((idx = s.buffer.indexOf("\n")) >= 0) {
                lines.add(s.buffer.substring(0, idx));
                s.buffer.delete(0, idx + 1);
            }
            if (flush && !s.buffer.isEmpty()) {
                lines.add(s.buffer.toString());
                s.buffer.setLength(0);
            }
        }
        for (String line : lines) {
            processLine(s, line);
        }
    }

    private void processLine(Session s, String raw) {
        String line = raw == null ? "" : raw.trim();
        // 过滤空行、思考痕迹、Markdown 围栏等非 JSON 行
        if (line.isEmpty() || !line.startsWith("{")) {
            return;
        }
        AiSentence dto;
        try {
            dto = objectMapper.readValue(line, AiSentence.class);
        } catch (Exception e) {
            log.warn("NDJSON 行解析失败，跳过: {}", line);
            return;
        }

        if (dto.isDoneLine()) {
            handleDone(s, dto);
        } else if (dto.isSentenceLine()) {
            handleSentence(s, dto);
        }
    }

    /** 单句处理：枚举校验 + 强度 clamp + emoji 映射 */
    private void handleSentence(Session s, AiSentence dto) {
        Emotion ae = Emotion.fromName(dto.aiEmotion());
        // 高风险上下文禁用 PLAYFULNESS（规则引擎前置判定）
        if (s.ruleRisk.isHighOrCritical() && ae == Emotion.PLAYFULNESS) {
            ae = Emotion.SERIOUS;
        }
        int intensity = clamp(dto.aiEmotionIntensity());
        String reply = sanitizeReply(dto.reply());
        if (reply == null) {
            return;
        }
        emit(s, "sentence", new SentenceEvent(
                s.sentenceCount++, reply, ae.name(), intensity, EmojiUtil.pick(ae, intensity)));
    }

    /** done 行处理：风险等级二次校验（AI + 规则引擎取更高）+ 危机干预 */
    private void handleDone(Session s, AiSentence dto) {
        if (s.doneEmitted) {
            return;
        }
        s.doneEmitted = true;

        Emotion ue = Emotion.fromName(dto.userEmotion());
        int ui = clamp(dto.userEmotionIntensity());

        RiskLevel aiRisk = RiskLevel.fromName(dto.riskLevel());
        // AI 漏 riskLevel 时保守取 MEDIUM，再与规则引擎结果取更高者
        RiskLevel finalRisk = RiskLevel.max(
                aiRisk != null ? aiRisk : RiskLevel.MEDIUM, s.ruleRisk);

        emitCrisisIfNeeded(s, finalRisk);

        emit(s, "done", new DoneEvent(
                ue.name(), ue.getLabel(), ui, EmojiUtil.pick(ue, ui),
                finalRisk.name(), sanitizeSuggestions(dto.suggestions()),
                PromptConfig.SAFETY_DISCLAIMER, s.requestId));
    }

    /** 流正常结束但 AI 漏输出 done 行：合成兜底 done */
    private void finalizeStream(Session s) {
        if (!s.doneEmitted) {
            log.warn("AI 未输出 done 行，合成兜底 done。requestId={}", s.requestId);
            if (s.sentenceCount == 0) {
                emit(s, "sentence", new SentenceEvent(0,
                        "谢谢你愿意和我分享这些，我一直在听。",
                        Emotion.GENTLE.name(), 5, EmojiUtil.pick(Emotion.GENTLE, 5)));
                s.sentenceCount = 1;
            }
            RiskLevel finalRisk = RiskLevel.max(RiskLevel.MEDIUM, s.ruleRisk);
            emitCrisisIfNeeded(s, finalRisk);
            emit(s, "done", new DoneEvent(
                    Emotion.UNKNOWN.name(), Emotion.UNKNOWN.getLabel(), 1,
                    EmojiUtil.pick(Emotion.UNKNOWN, 1),
                    finalRisk.name(), List.of(),
                    PromptConfig.SAFETY_DISCLAIMER, s.requestId));
        }
        finishSession(s);
    }

    /** 高风险触发危机流程：追加危机提示 sentence + 审计日志 */
    private void emitCrisisIfNeeded(Session s, RiskLevel finalRisk) {
        if (!finalRisk.isHighOrCritical()) {
            return;
        }
        crisisService.audit(s.userId, s.requestId, finalRisk.name(), s.userMsg);
        emit(s, "sentence", new SentenceEvent(
                s.sentenceCount++, CrisisService.CRISIS_MESSAGE,
                Emotion.SERIOUS.name(), 8, EmojiUtil.pick(Emotion.SERIOUS, 8)));
    }

    private void finishSession(Session s) {
        s.sink.tryEmitComplete();
        sessions.remove(s.userId, s);
    }

    /* ================================================================
     * SSE 事件输出
     * ================================================================ */
    private void emit(Session s, String event, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            s.sink.tryEmitNext(ServerSentEvent.<String>builder()
                    .event(event).data(json).build());
        } catch (Exception e) {
            log.warn("SSE 事件序列化失败: {}", e.getMessage());
        }
    }

    /* ================================================================
     * 工具方法
     * ================================================================ */

    /** 强度 clamp 到 1-10，null 视为 1 */
    private static int clamp(Integer intensity) {
        if (intensity == null) return 1;
        return Math.max(1, Math.min(10, intensity));
    }

    /** reply 去换行、去首尾空白；空句返回 null（跳过） */
    private static String sanitizeReply(String reply) {
        if (reply == null) return null;
        String cleaned = reply.replace("\r", "").replace("\n", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    /** 建议过滤：非空、不超过 30 字、最多 3 条 */
    private static List<String> sanitizeSuggestions(List<String> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(t -> !t.isEmpty() && t.length() <= 30)
                .limit(3)
                .toList();
    }
}