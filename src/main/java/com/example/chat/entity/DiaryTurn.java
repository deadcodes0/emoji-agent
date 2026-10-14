package com.example.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * suggestions 是 json 列；当前 EmotionDiaryView 里写死为空数组，
 * 暂不落库（保持列可空），后续接入 AI 输出时再补 JsonTypeHandler。
 */
@Data
@TableName("diary_turns")
public class DiaryTurn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long diaryDayId;
    private LocalDateTime occurredAt;
    private String turnTime;
    private String userMessage;
    private String botReply;
    private String emotion;
    private String riskLevel;
    private LocalDateTime createdAt;
}