package com.juntong.multimodalantiscamassistant.module.alert.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警记录响应模型
 */
@Data
@Schema(description = "风险预警响应模型")
public class AlertVO {

    @Schema(description = "预警记录ID")
    private Long id;

    @Schema(description = "疑似诈骗类型", example = "刷单返利诈骗")
    private String scamType;

    @Schema(description = "AI 判定理由与依据汇总")
    private String reasoning;

    @Schema(description = "风险严重程度: 1=低 2=中 3=高", example = "2")
    private Integer severity;

    @Schema(description = "触发时间")
    private LocalDateTime createdAt;
}
