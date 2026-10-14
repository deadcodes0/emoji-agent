package com.example.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * AI 输出 NDJSON 中一行的映射。
 * 句子行：{"i":0,"reply":"...","aiEmotion":"EMPATHY","aiEmotionIntensity":6}
 * 结束行：{"i":-1,"done":true,"userEmotion":"ANXIETY","userEmotionIntensity":7,
 *          "riskLevel":"LOW","suggestions":["..."]}
 * 两种行共用该结构，未出现的字段为 null。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiSentence(
        Integer i,
        String reply,
        Boolean done,
        String aiEmotion,
        Integer aiEmotionIntensity,
        String userEmotion,
        Integer userEmotionIntensity,
        String riskLevel,
        List<String> suggestions
) {

    /** 是否结束行（done=true 或 i=-1） */
    public boolean isDoneLine() {
        return Boolean.TRUE.equals(done) || (i != null && i == -1);
    }

    /** 是否有效句子行 */
    public boolean isSentenceLine() {
        return !isDoneLine() && reply != null && !reply.isBlank();
    }
}
