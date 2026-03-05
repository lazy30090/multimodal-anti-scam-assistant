package com.juntong.multimodalantiscamassistant.module.guardian.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 绑定关系视图
 */
@Data
@Schema(description = "监护人绑定关系视图模型")
public class BindVO {
    @Schema(description = "绑定记录ID")
    private Long bindId;

    @Schema(description = "监护人ID")
    private Long guardianId;

    @Schema(description = "监护人手机号")
    private String guardianPhone;

    @Schema(description = "与监护人的关系")
    private String relation;

    @Schema(description = "状态: 1=正常")
    private Integer status;
}
