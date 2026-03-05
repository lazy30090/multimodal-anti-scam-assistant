package com.juntong.multimodalantiscamassistant.module.guardian.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新通知策略请求体
 */
@Data
@Schema(description = "监护人通知偏好设置模型")
public class UpdateNotifyPolicyDTO {

    @Schema(description = "通知策略: 1=实时推送 2=周期汇总", example = "1")
    @NotNull(message = "通知策略不能为空")
    @Min(value = 1, message = "通知策略取值范围: 1~2")
    @Max(value = 2, message = "通知策略取值范围: 1~2")
    private Integer notifyPolicy;
}
