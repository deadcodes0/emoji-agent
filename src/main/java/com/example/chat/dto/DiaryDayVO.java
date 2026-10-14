package com.example.chat.dto;

import java.util.List;

public record DiaryDayVO(
        String date,
        String weekday,
        String summary,
        String dominantEmotion,
        String riskLevel,
        List<DiaryTurnVO> turns
) {}