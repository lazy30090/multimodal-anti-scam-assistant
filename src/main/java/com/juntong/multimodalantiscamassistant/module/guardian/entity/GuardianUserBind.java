package com.juntong.multimodalantiscamassistant.module.guardian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 监护人-用户绑定关系实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("guardian_user_bind")
public class GuardianUserBind {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long guardianId;
    private Long userId;

    /** 关系描述，如 父亲/母亲/子女 */
    private String relation;

    /** 绑定状态: 1=有效 0=已解除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
