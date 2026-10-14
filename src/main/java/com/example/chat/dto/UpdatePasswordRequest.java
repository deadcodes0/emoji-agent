package com.example.chat.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePasswordRequest(
        @NotNull Integer oldPassword,
        @NotNull Integer newPassword
) {}