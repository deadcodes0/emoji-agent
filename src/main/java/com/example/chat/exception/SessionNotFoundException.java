package com.example.chat.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String userId) {
        super("用户 " + userId + " 当前没有正在生成的会话");
    }
}
