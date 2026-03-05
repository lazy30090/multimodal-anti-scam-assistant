package com.juntong.multimodalantiscamassistant.module.detect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 多模态检测记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("detection_record")
public class DetectionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String fileUrl;
    private String textContent;
    private BigDecimal riskScore;
    private String scamType;
    private String riskAction;
    private String summary;
    private LocalDateTime createdAt;
}
