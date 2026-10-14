package com.example.chat.dto;

import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotNull Integer accountNumber,
        @NotNull Integer password,
        String nickname
) {}
