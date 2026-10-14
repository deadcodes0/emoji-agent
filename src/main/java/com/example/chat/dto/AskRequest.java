package com.example.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * userId 不由前端传入，统一从 JWT（UserContext）解析。
 */
@Data
public class AskRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String msg;

    /** 最近几轮历史对话（可选），用于 LLM 上下文 */
    private List<ChatHistoryItem> history;
}
