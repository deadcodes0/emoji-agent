package com.example.chat.config;

import com.example.chat.enums.Emotion;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 心理健康对话 Agent 的 System Prompt。
 * 枚举白名单全部从 Java 枚举动态生成，避免手写列表与枚举不一致。
 */
public class PromptConfig {

    public static final String SAFETY_DISCLAIMER =
            "以上内容来自 AI 助手，不能替代专业心理诊断或治疗。"
                    + "如果你正处于危机中，请立即拨打心理援助热线 12356 或紧急电话 110 / 120。";

    public static final String HEALTH_AGENT_PROMPT = buildHealthAgentPrompt();

    private PromptConfig() {}

    private static String buildHealthAgentPrompt() {
        // 枚举白名单动态生成（与 Java 枚举永远一致）
        String emotions = Emotion.allLabels();

//        # 核心原则（伙伴模式）
//        以损友的方式回复用户
        String res = """
        你现在处于【{emotion}】情绪，你是用户的嘴替
        
        # 输出格式（必须严格遵守）
        你逐行输出 JSONL（NDJSON），每行一个完整 JSON，不要数组、不要 Markdown、不要代码块。
        句子行格式：
        {"i":序号,"reply":"一句话","aiEmotion":"枚举名","aiEmotionIntensity":1-10}
        最后一行（分析行）格式：
        {"i":-1,"done":true,"userEmotion":"枚举名","userEmotionIntensity":1-10}

        ## 规则
        aiEmotion 和 userEmotion 以及 【{emotion}】 只能从以下枚举名中选一个：%s
        reply 不能包含换行符 \\n。
        aiEmotionIntensity 是 1-10 的整数，aiEmotion 必须贴合本句内容，随语气变化。
        只输出 JSONL：从第一行到最后一行都是 JSON。
        """.formatted(emotions);

        System.out.println(res);
        return res;
    }
}
