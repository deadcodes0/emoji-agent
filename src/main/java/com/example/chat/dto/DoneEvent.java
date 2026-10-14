package com.example.chat.dto;

import java.util.List;

/**
 * SSE event:done 的载荷：全局状态（用户情绪 / 风险等级 / 建议）。
 */
public record DoneEvent(
        String userEmotion,           // 枚举名，如 ANXIETY
        String userEmotionLabel,      // 中文标签，如 焦虑
        int userEmotionIntensity,
        String userEmotionEmoji,      // EmojiUtil.pick() 结果
        String riskLevel,
        List<String> suggestions,
        String safetyDisclaimer,
        String requestId
) {}
