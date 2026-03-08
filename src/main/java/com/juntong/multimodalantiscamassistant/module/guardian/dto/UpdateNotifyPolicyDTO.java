package com.juntong.multimodalantiscamassistant.module.guardian.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新通知策略请求体
 */
@Data
@Schema(description = "监护人通知偏好设置模型")
public class UpdateNotifyPolicyDTO {

    @Schema(description = "通知策略: IMMEDIATE 实时推送, DAILY_SUMMARY 周期汇总", example = "DAILY_SUMMARY")
    @NotNull(message = "通知策略不能为空")
    private String notifyPolicy;
}
