package com.juntong.multimodalantiscamassistant.module.guardian.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 远程干预请求体
 */
@Data
@Schema(description = "监护人远程干预模型")
public class InterventionDTO {

    @Schema(description = "干预消息内容，将实时推送至被监护人端", example = "检测到疑似诈骗，请立即挂断电话！")
    @NotBlank(message = "干预消息不能为空")
    private String message;
}
