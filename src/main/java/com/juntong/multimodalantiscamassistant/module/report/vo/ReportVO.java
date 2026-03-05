package com.juntong.multimodalantiscamassistant.module.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报告响应模型
 */
@Data
@Schema(description = "安全监测报告响应模型")
public class ReportVO {

    @Schema(description = "报告ID")
    private Long id;

    @Schema(description = "统计开始日期")
    private LocalDate startDate;

    @Schema(description = "统计结束日期")
    private LocalDate endDate;

    @Schema(description = "报告生成时间")
    private LocalDateTime createdAt;

    @Schema(description = "监测期内累计预警总次数")
    private Integer totalAlerts;

    @Schema(description = "高风险事件次数")
    private Integer highSeverityCount;

    @Schema(description = "中风险事件次数")
    private Integer midSeverityCount;

    @Schema(description = "低风险事件次数")
    private Integer lowSeverityCount;

    @Schema(description = "诈骗类型统计分布 (Key: 类型名称, Value: 出现次数)", example = "{\"刷单诈骗\": 5, \"杀猪盘\": 2}")
    private Map<String, Integer> scamTypeStats;

    @Schema(description = "AI 生成的监测期内风险趋势总结与防护建议")
    private String summary;
}
