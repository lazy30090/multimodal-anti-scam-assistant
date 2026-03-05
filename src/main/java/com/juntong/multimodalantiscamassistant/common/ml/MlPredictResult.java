package com.juntong.multimodalantiscamassistant.common.ml;

import lombok.Data;

import java.util.List;

/**
 * ML 服务返回的风险预测结果（精简版，与 ML 团队约定的接口）
 */
@Data
public class MlPredictResult {

    /** 风险分值 0~100 */
    private Double riskScore;

    /** 诈骗类型枚举，如 IMPERSONATE_POLICE_GOV */
    private String scamType;

    /** 模型置信度 0~1 */
    private Double confidence;

    /** 建议动作：BLOCK / WARN / EDUCATE */
    private String riskAction;

    /** 命中的关键证据短语列表 */
    private List<String> evidence;

    /** 面向用户展示的分析摘要 */
    private String summary;

    /** ML 建议追问的问题列表（驱动多轮对话） */
    private List<RecommendedQuestion> recommendedQuestions;

    @Data
    public static class RecommendedQuestion {
        private String key;
        private String question;
    }

    // -------------------------------------------------------
    // 便捷方法：后端根据 riskScore 推导 riskLevel
    // -------------------------------------------------------
    public String getRiskLevel() {
        if (riskScore == null)
            return "LOW";
        if (riskScore >= 90)
            return "CRITICAL";
        if (riskScore >= 70)
            return "HIGH";
        if (riskScore >= 40)
            return "MEDIUM";
        return "LOW";
    }

    /** 根据 riskLevel 映射到 alert 表中的 severity(1/2/3) */
    public int getSeverity() {
        return switch (getRiskLevel()) {
            case "CRITICAL", "HIGH" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };
    }
}
