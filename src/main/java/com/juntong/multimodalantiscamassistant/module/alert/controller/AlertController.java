package com.juntong.multimodalantiscamassistant.module.alert.controller;

import com.juntong.multimodalantiscamassistant.common.Result;
import com.juntong.multimodalantiscamassistant.module.alert.service.AlertService;
import com.juntong.multimodalantiscamassistant.module.alert.vo.AlertVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "3. 风险预警模块", description = "处理风险历史记录查询及详细判定依据展示")
@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @Operation(summary = "获取历史预警记录", description = "获取当前被监护用户的所有历史预警，含风险分、类型及简析")
    @GetMapping("/history")
    public Result<List<AlertVO>> history() {
        return Result.ok(alertService.listHistory(currentId()));
    }

    @Operation(summary = "获取预警详情", description = "基于预警 ID 获取单条记录的详细判定逻辑、关联聊天上下文等深度信息")
    @GetMapping("/detail/{id}")
    public Result<AlertVO> detail(@PathVariable Long id) {
        return Result.ok(alertService.getDetail(id, currentId()));
    }

    private Long currentId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
