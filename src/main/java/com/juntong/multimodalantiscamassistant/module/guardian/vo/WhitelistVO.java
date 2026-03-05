package com.juntong.multimodalantiscamassistant.module.guardian.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 白名单条目视图模型
 */
@Data
@Schema(description = "白名单条目响应模型")
public class WhitelistVO {
    @Schema(description = "记录唯一ID")
    private Long id;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "具体联系方式 (电话、微信号或卡号)")
    private String contactInfo;

    @Schema(description = "添加时间")
    private LocalDateTime createdAt;
}
