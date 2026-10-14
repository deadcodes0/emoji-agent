package com.example.chat.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SaveTurnRequest(
        @NotBlank String date,      // yyyy-MM-dd
        @NotBlank String time,      // HH:mm
        String userMsg,
        String reply,
        String emotion,
        String riskLevel,
        List<String> suggestions
) {}