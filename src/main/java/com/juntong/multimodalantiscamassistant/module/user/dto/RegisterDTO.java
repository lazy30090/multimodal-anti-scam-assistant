package com.juntong.multimodalantiscamassistant.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求体
 */
@Data
@Schema(description = "用户注册请求体")
public class RegisterDTO {

    @Schema(description = "用户名 (3~50字)", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需在 3~50 个字符之间")
    private String username;

    @Schema(description = "密码 (至少6位)", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度至少 6 位")
    private String password;
}
