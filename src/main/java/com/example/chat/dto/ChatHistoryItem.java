package com.example.chat.dto;

/**
 * 历史对话条目（role: user / assistant）。
 */
public record ChatHistoryItem(String role, String content) {}
