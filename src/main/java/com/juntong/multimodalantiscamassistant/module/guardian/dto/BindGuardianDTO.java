package com.juntong.multimodalantiscamassistant.module.guardian.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 绑定监护人请求体
 */
@Data
@Schema(description = "绑定监护人请求体")
public class BindGuardianDTO {

    @Schema(description = "监护人注册时使用的手机号", example = "13800138001")
    @NotBlank(message = "监护人手机号不能为空")
    private String guardianPhone;

    @Schema(description = "与监护人的关系 (如：父亲、母亲)", example = "父亲")
    @NotBlank(message = "关系不能为空")
    private String relation;
}
