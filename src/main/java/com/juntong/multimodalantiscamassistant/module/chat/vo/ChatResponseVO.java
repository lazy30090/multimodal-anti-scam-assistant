package com.juntong.multimodalantiscamassistant.module.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.juntong.multimodalantiscamassistant.common.ml.MlPredictResult;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 发送消息响应模型
 */
@Data
@Schema(description = "对话发送响应模型 (含风险评估结果)")
public class ChatResponseVO {

    @Schema(description = "AI 生成的实时回复建议内容")
    private String reply;

    @Schema(description = "实时风险评估得分 (0~100)", example = "85.2")
    private Double riskScore;

    @Schema(description = "风险等级标识", example = "HIGH")
    private String riskLevel;

    @Schema(description = "判定的具体诈骗类型", example = "公检法诈骗")
    private String scamType;

    @Schema(description = "系统建议采取的即时行动干预")
    private String riskAction;

    @Schema(description = "本次交流中体现的风险点核心摘要")
    private String summary;

    @Schema(description = "AI 推荐的后续追问问题，用于驱动深度对话确认")
    private List<MlPredictResult.RecommendedQuestion> recommendedQuestions;

    @Schema(description = "处理完成时间")
    private LocalDateTime createdAt;
}
