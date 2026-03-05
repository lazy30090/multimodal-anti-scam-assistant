package com.juntong.multimodalantiscamassistant.module.guardian.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.juntong.multimodalantiscamassistant.module.alert.vo.AlertVO;
import lombok.Data;

import java.util.List;

/**
 * 被监护人安全汇总视图
 */
@Data
@Schema(description = "被监护人安全汇总模型")
public class WardSummaryVO {

    @Schema(description = "被监护人用户ID")
    private Long wardId;

    @Schema(description = "被监护人用户名")
    private String wardUsername;

    @Schema(description = "近7天累计预警次数")
    private Integer weeklyAlertCount;

    @Schema(description = "近7天高风险预警次数")
    private Integer weeklyHighCount;

    @Schema(description = "最近5条历史预警详细信息")
    private List<AlertVO> recentAlerts;

    @Schema(description = "最新生成的安全报告核心摘要文字")
    private String latestReportSummary;
}
