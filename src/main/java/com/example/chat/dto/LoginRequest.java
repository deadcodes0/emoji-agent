package com.example.chat.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull Integer accountNumber,
        @NotNull Integer password
) {}
