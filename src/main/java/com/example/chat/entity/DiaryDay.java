package com.example.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("diary_days")
public class DiaryDay {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate diaryDate;
    private String summary;
    private String dominantEmotion;
    private String riskLevel;
    private Integer turnCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}