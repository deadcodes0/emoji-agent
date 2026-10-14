package com.example.chat.dto;

import com.example.chat.entity.User;

public record UserVO(Long id, Integer accountNumber, String nickname, String avatarUrl) {
    public static UserVO from(User u) {
        return new UserVO(u.getId(), u.getAccountNumber(), u.getNickname(), u.getAvatarUrl());
    }
}
