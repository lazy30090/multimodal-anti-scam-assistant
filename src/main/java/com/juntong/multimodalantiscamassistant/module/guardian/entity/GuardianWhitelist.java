package com.juntong.multimodalantiscamassistant.module.guardian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 白名单实体（信任联系人）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("guardian_whitelist")
public class GuardianWhitelist {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String contactName;
    private String contactInfo;
    @com.baomidou.mybatisplus.annotation.TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createdAt;
}
