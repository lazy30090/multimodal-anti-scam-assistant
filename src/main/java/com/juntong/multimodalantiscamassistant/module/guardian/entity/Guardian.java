package com.juntong.multimodalantiscamassistant.module.guardian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 监护人实体，对应 guardian 表（微信小程序端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("guardian")
public class Guardian {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openId;
    private String nickname;
    private String phone;

    /** 通知策略: 1=实时推送 2=周期汇总 */
    private Integer notifyPolicy;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
