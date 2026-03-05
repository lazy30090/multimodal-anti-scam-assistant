package com.juntong.multimodalantiscamassistant.common.ml;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * ML 服务 Mock 实现，ML 团队接口就绪前使用
 * 标注 @Primary 保证优先注入此实现
 * 替换时：删除此类，添加 RealMlServiceClient 即可
 */
@Primary
@Component
public class MockMlServiceClient implements MlServiceClient {

    private static final String[] SCAM_TYPES = {
            "IMPERSONATE_POLICE_GOV",
            "INVESTMENT_SCAM",
            "LOAN_SCAM",
            "FAMILY_EMERGENCY",
            "PHISHING_LINK_QRCODE"
    };

    private final Random random = new Random();

    @Override
    public MlPredictResult predict(MlPredictRequest request) {
        // 简单模拟：包含特定关键词则返回高风险
        String text = request.getText() != null ? request.getText() : "";
        boolean highRisk = text.contains("安全账户") || text.contains("公安") ||
                text.contains("转账") || text.contains("贷款") ||
                text.contains("中奖");

        MlPredictResult result = new MlPredictResult();
        result.setRiskScore(highRisk ? 85.0 + random.nextInt(15) : 15.0 + random.nextInt(25));
        result.setScamType(highRisk ? SCAM_TYPES[random.nextInt(SCAM_TYPES.length)] : "OTHER_UNKNOWN");
        result.setConfidence(highRisk ? 0.88 + random.nextDouble() * 0.10 : 0.30 + random.nextDouble() * 0.30);
        result.setRiskAction(highRisk ? "WARN" : "EDUCATE");
        result.setEvidence(highRisk
                ? List.of("检测到高风险话术", text.length() > 20 ? text.substring(0, 20) + "..." : text)
                : List.of());
        result.setSummary(highRisk
                ? "检测到疑似诈骗内容，建议谨慎操作，不要轻易转账或提供个人信息。"
                : "当前内容风险较低，请继续保持警惕。");
        result.setRecommendedQuestions(highRisk ? List.of(
                buildQ("transfer_requested", "对方是否要求你转账或充值？"),
                buildQ("identity_verified", "你是否核实了对方的真实身份？")) : List.of());

        return result;
    }

    private MlPredictResult.RecommendedQuestion buildQ(String key, String question) {
        MlPredictResult.RecommendedQuestion q = new MlPredictResult.RecommendedQuestion();
        q.setKey(key);
        q.setQuestion(question);
        return q;
    }
}
