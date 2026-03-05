package com.juntong.multimodalantiscamassistant.module.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 安全监测报告实体，content 字段存储 JSON 格式的统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;

    /** JSON 字符串，结构见 ReportContent */
    private String content;

    private LocalDateTime createdAt;
}
