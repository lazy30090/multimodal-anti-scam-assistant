package com.juntong.multimodalantiscamassistant.module.guardian.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 添加白名单请求体
 */
@Data
@Schema(description = "添加白名单请求体")
public class AddWhitelistDTO {

    @Schema(description = "联系人名称", example = "王医生")
    @NotBlank(message = "联系人名称不能为空")
    private String contactName;

    @Schema(description = "联系方式 (电话/账号)", example = "13800138000")
    @NotBlank(message = "联系方式不能为空")
    private String contactInfo;
}
