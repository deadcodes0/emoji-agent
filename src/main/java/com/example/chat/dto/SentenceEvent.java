package com.example.chat.dto;

/**
 * SSE event:sentence 的载荷。
 * emoji key 由后端 EmojiUtil.pick() 算好，前端直接渲染。
 */
public record SentenceEvent(
        int i,
        String reply,
        String aiEmotion,
        int aiEmotionIntensity,
        String emoji
) {}
