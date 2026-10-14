package com.example.chat.config;

import com.example.chat.exception.BizException;
import com.example.chat.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IOException.class)
    public void handleClientDisconnect(IOException e) {
        // 客户端断开导致的 IOException，属于正常现象，降级为 debug 日志
        log.debug("Client disconnected: {}", e.getMessage());
    }

    /**
     * 请求体不可读（JSON 格式错误等）。
     * 必须单列：底层 Jackson 的 JsonParseException 继承自 IOException，
     * 会被上面的 IOException 处理器沿异常链捕获，吞成 200 空响应。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleNotReadable(HttpMessageNotReadableException e) {
        return Map.of("code", 400, "message", "请求体格式错误");
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleUnauthorized(UnauthorizedException e) {
        return Map.of("code", 401, "message", e.getMessage());
    }

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBiz(BizException e) {
        return Map.of("code", 400, "message", e.getMessage());
    }
}
