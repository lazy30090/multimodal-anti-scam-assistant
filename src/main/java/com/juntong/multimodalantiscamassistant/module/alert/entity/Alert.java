package com.juntong.multimodalantiscamassistant.module.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预警记录实体，对应 alert 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("alert")
public class Alert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 诈骗类型，如 investment_fraud / impersonation_police */
    private String scamType;

    /** 判定依据 */
    private String reasoning;

    /** 风险等级: 1=低 2=中 3=高 */
    private Integer severity;

    private LocalDateTime createdAt;
}
