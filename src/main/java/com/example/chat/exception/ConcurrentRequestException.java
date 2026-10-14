package com.example.chat.exception;


public class ConcurrentRequestException extends RuntimeException {
    public ConcurrentRequestException(String userId) {
        super("用户 " + userId + " 已有正在生成的请求，请勿重复提交");
    }
}