package com.example.chat.dto;

import java.util.List;

public record DiaryTurnVO(
        String time,
        String userMsg,
        String reply,
        String emotion,
        String riskLevel,
        List<String> suggestions
) {}