package com.example.chat.controller;

import com.example.chat.dto.AskRequest;
import com.example.chat.exception.ConcurrentRequestException;
import com.example.chat.security.UserContext;
import com.example.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * SEND：发起一次对话。
     * 事件流：event:sentence（逐句，含 aiEmotion/emoji）→ event:done（全局状态），
     * 异常时 event:error。userId 从 JWT 解析。
     */
    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> send(@Valid @RequestBody AskRequest request) {
        return chatService.send(UserContext.userIdString(), request.getMsg());
    }

    /**
     * RESUME：断线恢复，token 已标识用户，无需路径参数。
     * - 无进行中的流 -> 空 Flux（200 + 立即关闭）
     * - 有进行中的流 -> 立即发 event:open，再回放/转发已缓存事件
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> resume() {
        return chatService.resumeAsEvents(UserContext.userIdString());
    }

    /** 用户主动停止当前生成 */
    @PostMapping("/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stop() {
        chatService.stop(UserContext.userIdString());
    }

    @ExceptionHandler(ConcurrentRequestException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConcurrent(ConcurrentRequestException e) {
        return e.getMessage();
    }
}
