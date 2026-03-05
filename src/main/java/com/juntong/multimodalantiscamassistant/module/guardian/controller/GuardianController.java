package com.juntong.multimodalantiscamassistant.module.guardian.controller;

import com.juntong.multimodalantiscamassistant.common.Result;
import com.juntong.multimodalantiscamassistant.module.guardian.dto.*;
import com.juntong.multimodalantiscamassistant.module.guardian.service.GuardianService;
import com.juntong.multimodalantiscamassistant.module.guardian.vo.BindVO;
import com.juntong.multimodalantiscamassistant.module.guardian.vo.WardSummaryVO;
import com.juntong.multimodalantiscamassistant.module.guardian.vo.WhitelistVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "2. 监护人联动管理", description = "处理监护人端（小程序）的登录、绑定关系、白名单、安全周报及远程干预")
@RestController
@RequestMapping("/api/guardian")
@RequiredArgsConstructor
public class GuardianController {

    private final GuardianService guardianService;

    @Operation(summary = "微信小程序登录", description = "监护人端专用，通过微信 code 换取 Token")
    @PostMapping("/wx-login")
    public Result<String> wxLogin(@RequestBody @Validated WxLoginDTO dto) {
        return Result.ok(guardianService.wxLogin(dto));
    }

    @Operation(summary = "用户绑定监护人", description = "被监护人通过手机号搜索并绑定其监护人")
    @PostMapping("/bind")
    public Result<List<BindVO>> bind(@RequestBody @Validated BindGuardianDTO dto) {
        Long userId = currentId();
        guardianService.bind(userId, dto);
        return Result.ok(guardianService.listBinds(userId));
    }

    @Operation(summary = "解绑监护人", description = "被监护人主动解除与某位监护人的关联")
    @DeleteMapping("/unbind/{guardianId}")
    public Result<Void> unbind(@PathVariable Long guardianId) {
        guardianService.unbind(currentId(), guardianId);
        return Result.ok();
    }

    @Operation(summary = "获取白名单列表", description = "获取当前用户的信任联系人/账号列表，用于减少误报")
    @GetMapping("/whitelist")
    public Result<List<WhitelistVO>> getWhitelist() {
        return Result.ok(guardianService.listWhitelist(currentId()));
    }

    @Operation(summary = "添加白名单", description = "将被信任的联系人或账号信息加入白名单")
    @PostMapping("/whitelist")
    public Result<Void> addWhitelist(@RequestBody @Validated AddWhitelistDTO dto) {
        guardianService.addWhitelist(currentId(), dto);
        return Result.ok();
    }

    @Operation(summary = "更新通知策略", description = "监护人设置接收预警的方式：1=实时推送 2=周期汇总")
    @PutMapping("/notify-policy")
    public Result<Void> updateNotifyPolicy(@RequestBody @Validated UpdateNotifyPolicyDTO dto) {
        guardianService.updateNotifyPolicy(currentId(), dto);
        return Result.ok();
    }

    @Operation(summary = "查看被监护人安全汇总", description = "监护人主动查询其下所有被监护人的风险统计及最近预警记录")
    @GetMapping("/summary")
    public Result<List<WardSummaryVO>> summary() {
        return Result.ok(guardianService.getWardSummaries(currentId()));
    }

    @Operation(summary = "远程干预推送", description = "监护人向指定的被监护人发送实时提醒消息（通过 WebSocket 下发）")
    @PostMapping("/intervention/{wardId}")
    public Result<Void> intervention(@PathVariable Long wardId,
            @RequestBody @Validated InterventionDTO dto) {
        guardianService.sendIntervention(currentId(), wardId, dto);
        return Result.ok();
    }

    private Long currentId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
