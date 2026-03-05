package com.juntong.multimodalantiscamassistant.module.guardian.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录请求体
 */
@Data
@Schema(description = "微信小程序登录模型")
public class WxLoginDTO {

    @Schema(description = "微信登录凭证 (code)", example = "033xxxxxxx")
    @NotBlank(message = "code 不能为空")
    private String code;

    @Schema(description = "微信昵称（可选，登录时顺带更新）", example = "张三监护人")
    private String nickname;
}
