package com.example.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * users 表：account_number / password 均为 int（保持表结构不动，明文比对）。
 */
@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String nickname;
    private String avatarUrl;
    private Integer accountNumber;
    private Integer password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
