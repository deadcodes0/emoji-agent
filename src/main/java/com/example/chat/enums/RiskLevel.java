package com.example.chat.enums;

/**
 * 风险等级。
 * 由 AI 初判 + 后端规则引擎二次校验，最终取两者中更高者。
 */
public enum RiskLevel {

    LOW(1, "低风险"),
    MEDIUM(2, "中等风险"),
    HIGH(3, "高风险"),
    CRITICAL(4, "危急");

    private final int score;
    private final String label;

    RiskLevel(int score, String label) {
        this.score = score;
        this.label = label;
    }

    public int getScore() { return score; }
    public String getLabel() { return label; }

    /**
     * 按枚举名安全解析；非法 / 空值返回 null（由调用方决定兜底策略）。
     */
    public static RiskLevel fromName(String name) {
        if (name == null || name.isBlank()) return null;
        for (RiskLevel r : values()) {
            if (r.name().equalsIgnoreCase(name.trim())) return r;
        }
        return null;
    }

    /** 取两者中更高的风险等级 */
    public static RiskLevel max(RiskLevel a, RiskLevel b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.score >= b.score ? a : b;
    }

    public boolean isHighOrCritical() {
        return this == HIGH || this == CRITICAL;
    }
}
