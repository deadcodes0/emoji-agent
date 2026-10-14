package com.example.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 危机干预服务：riskLevel >= HIGH 时触发。
 * - 生成危机提示文案（作为追加 sentence 推送）
 * - 记录高风险审计日志（requestId / userId / riskLevel / 原始消息）
 */
@Service
public class CrisisService {

    private static final Logger auditLog = LoggerFactory.getLogger("RISK_AUDIT");

    public static final String CRISIS_MESSAGE =
            "我真的很担心你现在的安全，想请你先做一件事：立刻联系一个你信任的人，"
                    + "或拨打心理援助热线 12356 / 希望24热线 400-161-9995；"
                    + "如果危险正在发生，请马上拨打 110 或 120。我会一直在这里陪着你。";

    /** 高风险审计日志 */
    public void audit(String userId, String requestId, String riskLevel, String message) {
        auditLog.warn("[AUDIT][HIGH-RISK] requestId={}, userId={}, riskLevel={}, message={}",
                requestId, userId, riskLevel, message);
    }
}
