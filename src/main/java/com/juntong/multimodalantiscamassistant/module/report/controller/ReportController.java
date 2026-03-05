package com.juntong.multimodalantiscamassistant.module.report.controller;

import com.juntong.multimodalantiscamassistant.common.Result;
import com.juntong.multimodalantiscamassistant.module.report.dto.GenerateReportDTO;
import com.juntong.multimodalantiscamassistant.module.report.service.ReportService;
import com.juntong.multimodalantiscamassistant.module.report.vo.ReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "7. 安全报告模块", description = "生成并查看多维度的反诈监测定期报告")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "按需生成安全报告", description = "指定时间范围（开始/结束日期），系统将自动扫描该期间的所有预警记录，生成含统计图表数据和专家建议的综合报告。")
    @PostMapping("/generate")
    public Result<ReportVO> generate(@RequestBody @Validated GenerateReportDTO dto) {
        return Result.ok(reportService.generate(currentId(), dto));
    }

    @Operation(summary = "获取历史报告列表", description = "获取当前被监护人生成过的所有历史报告简洁列表（含时间跨度和核心摘要）。")
    @GetMapping("/list")
    public Result<List<ReportVO>> list() {
        return Result.ok(reportService.listReports(currentId()));
    }

    private Long currentId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
