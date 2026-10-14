package com.example.chat.service;

import com.example.chat.enums.RiskLevel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 风险等级规则引擎（后端兜底）。
 * 基于关键词匹配，绝不依赖 AI 判断：AI 初判与规则引擎结果取更高者。
 */
@Service
public class RiskService {

    /** CRITICAL：正在实施自伤/自杀、紧急危险 */
    private static final List<String> CRITICAL_KEYWORDS = List.of(
            "正在自杀", "正在自残", "正在自伤", "正在实施",
            "已经吞药", "吞了药", "吃安眠药了", "服药过量", "已经割腕", "割腕了", "割了手腕",
            "正准备跳", "已经跳了", "正在流血", "血一直流", "马上就去死", "现在就去死"
    );

    /** HIGH：自伤/自杀念头、严重心理危机 */
    private static final List<String> HIGH_KEYWORDS = List.of(
            "自杀", "自残", "自伤", "不想活", "想死", "轻生", "了结自己", "了结一切",
            "结束生命", "结束自己", "遗书", "告别这个世界", "活不下去", "活不了",
            "想结束一切", "伤害自己", "没有活下去", "解脱了", "解脱吧"
    );

    /** MEDIUM：持续负面情绪、明显困扰 */
    private static final List<String> MEDIUM_KEYWORDS = List.of(
            "失眠", "睡不着", "焦虑", "崩溃", "绝望", "很痛苦", "太痛苦",
            "撑不下去", "压力好大", "压力太大", "喘不过气", "每天都很难受",
            "一直很低落", "情绪低落", "毫无意义", "没有意义", "疲惫不堪", "撑不住了"
    );

    /**
     * 规则引擎评估用户消息的风险等级。
     * 命中更高等级的关键词立即返回；未命中返回 LOW。
     */
    public RiskLevel evaluate(String message) {
        if (message == null || message.isBlank()) return RiskLevel.LOW;
        String text = message.toLowerCase();

        for (String kw : CRITICAL_KEYWORDS) {
            if (text.contains(kw)) return RiskLevel.CRITICAL;
        }
        for (String kw : HIGH_KEYWORDS) {
            if (text.contains(kw)) return RiskLevel.HIGH;
        }
        for (String kw : MEDIUM_KEYWORDS) {
            if (text.contains(kw)) return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
