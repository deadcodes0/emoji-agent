package com.example.chat.dto;

public record UpdateProfileRequest(
        String nickname,
        String avatarUrl
) {}