package com.juntong.multimodalantiscamassistant.module.guardian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juntong.multimodalantiscamassistant.common.exception.BusinessException;
import com.juntong.multimodalantiscamassistant.common.util.JwtUtil;
import com.juntong.multimodalantiscamassistant.common.util.WxUtil;
import com.juntong.multimodalantiscamassistant.module.guardian.dto.*;
import com.juntong.multimodalantiscamassistant.module.guardian.entity.Guardian;
import com.juntong.multimodalantiscamassistant.module.guardian.entity.GuardianUserBind;
import com.juntong.multimodalantiscamassistant.module.guardian.entity.GuardianWhitelist;
import com.juntong.multimodalantiscamassistant.module.guardian.mapper.GuardianMapper;
import com.juntong.multimodalantiscamassistant.module.guardian.mapper.GuardianUserBindMapper;
import com.juntong.multimodalantiscamassistant.module.guardian.mapper.GuardianWhitelistMapper;
import com.juntong.multimodalantiscamassistant.module.guardian.service.GuardianService;
import com.juntong.multimodalantiscamassistant.module.guardian.vo.BindVO;
import com.juntong.multimodalantiscamassistant.module.guardian.vo.WhitelistVO;
import com.juntong.multimodalantiscamassistant.module.alert.entity.Alert;
import com.juntong.multimodalantiscamassistant.module.alert.mapper.AlertMapper;
import com.juntong.multimodalantiscamassistant.module.alert.vo.AlertVO;
import com.juntong.multimodalantiscamassistant.module.guardian.vo.WardSummaryVO;
import com.juntong.multimodalantiscamassistant.module.report.entity.Report;
import com.juntong.multimodalantiscamassistant.module.report.mapper.ReportMapper;
import com.juntong.multimodalantiscamassistant.module.user.entity.User;
import com.juntong.multimodalantiscamassistant.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuardianServiceImpl extends ServiceImpl<GuardianMapper, Guardian> implements GuardianService {

    private final JwtUtil jwtUtil;
    private final WxUtil wxUtil;
    private final GuardianUserBindMapper bindMapper;
    private final GuardianWhitelistMapper whitelistMapper;
    private final AlertMapper alertMapper;
    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public String wxLogin(WxLoginDTO dto) {
        String openId = wxUtil.getOpenId(dto.getCode());
        // 按 openId 查找监护人，不存在则自动注册
        Guardian guardian = lambdaQuery().eq(Guardian::getOpenId, openId).one();
        if (guardian == null) {
            guardian = Guardian.builder()
                    .openId(openId)
                    .nickname(dto.getNickname())
                    .notifyPolicy(1)
                    .build();
            save(guardian);
        } else if (dto.getNickname() != null) {
            // 更新昵称
            lambdaUpdate().eq(Guardian::getId, guardian.getId())
                    .set(Guardian::getNickname, dto.getNickname()).update();
        }
        return jwtUtil.generateToken(guardian.getId(), "GUARDIAN");
    }

    @Override
    public void bind(Long userId, BindGuardianDTO dto) {
        // 按手机号查找监护人账号
        Guardian guardian = lambdaQuery().eq(Guardian::getPhone, dto.getGuardianPhone()).one();
        if (guardian == null) {
            throw new BusinessException(404, "监护人尚未注册，请让对方先在小程序登录并完善手机号");
        }
        // 检查是否已绑定
        Long count = bindMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GuardianUserBind>()
                        .eq(GuardianUserBind::getGuardianId, guardian.getId())
                        .eq(GuardianUserBind::getUserId, userId)
                        .eq(GuardianUserBind::getStatus, 1));
        if (count > 0) {
            throw new BusinessException(400, "该监护人已绑定");
        }
        GuardianUserBind bind = GuardianUserBind.builder()
                .guardianId(guardian.getId())
                .userId(userId)
                .relation(dto.getRelation())
                .status(1)
                .build();
        bindMapper.insert(bind);
    }

    @Override
    public void unbind(Long userId, Long guardianId) {
        int rows = new com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper<>(bindMapper)
                .eq(GuardianUserBind::getUserId, userId)
                .eq(GuardianUserBind::getGuardianId, guardianId)
                .set(GuardianUserBind::getStatus, 0)
                .update() ? 1 : 0;
        if (rows == 0) {
            throw new BusinessException(404, "绑定关系不存在");
        }
    }

    @Override
    public List<BindVO> listBinds(Long userId) {
        List<GuardianUserBind> binds = new com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<>(
                bindMapper)
                .eq(GuardianUserBind::getUserId, userId)
                .eq(GuardianUserBind::getStatus, 1)
                .list();
        return binds.stream().map(b -> {
            BindVO vo = new BindVO();
            vo.setBindId(b.getId());
            vo.setGuardianId(b.getGuardianId());
            vo.setRelation(b.getRelation());
            vo.setStatus(b.getStatus());
            // 查监护人手机号
            Guardian g = getById(b.getGuardianId());
            if (g != null)
                vo.setGuardianPhone(g.getPhone());
            return vo;
        }).toList();
    }

    @Override
    public List<WhitelistVO> listWhitelist(Long userId) {
        List<GuardianWhitelist> list = new com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<>(
                whitelistMapper)
                .eq(GuardianWhitelist::getUserId, userId)
                .list();
        return list.stream().map(w -> {
            WhitelistVO vo = new WhitelistVO();
            BeanUtils.copyProperties(w, vo);
            return vo;
        }).toList();
    }

    @Override
    public void addWhitelist(Long userId, AddWhitelistDTO dto) {
        GuardianWhitelist entry = GuardianWhitelist.builder()
                .userId(userId)
                .contactName(dto.getContactName())
                .contactInfo(dto.getContactInfo())
                .build();
        whitelistMapper.insert(entry);
    }

    @Override
    public void updateNotifyPolicy(Long guardianId, UpdateNotifyPolicyDTO dto) {
        lambdaUpdate()
                .eq(Guardian::getId, guardianId)
                .set(Guardian::getNotifyPolicy, dto.getNotifyPolicy())
                .update();
    }

    @Override
    public List<WardSummaryVO> getWardSummaries(Long guardianId) {
        // 获取该监护人的所有有效被监护人
        List<GuardianUserBind> binds = new com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<>(
                bindMapper)
                .eq(GuardianUserBind::getGuardianId, guardianId)
                .eq(GuardianUserBind::getStatus, 1)
                .list();

        return binds.stream().map(bind -> {
            Long wardId = bind.getUserId();
            WardSummaryVO vo = new WardSummaryVO();
            vo.setWardId(wardId);

            // 获取被监护人用户名
            User ward = userMapper.selectById(wardId);
            if (ward != null)
                vo.setWardUsername(ward.getUsername());

            // 近7天预警
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<Alert> alerts = new com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<>(
                    alertMapper)
                    .eq(Alert::getUserId, wardId)
                    .ge(Alert::getCreatedAt, weekAgo)
                    .orderByDesc(Alert::getCreatedAt)
                    .list();
            vo.setWeeklyAlertCount(alerts.size());
            vo.setWeeklyHighCount((int) alerts.stream().filter(a -> a.getSeverity() == 3).count());

            // 最近5条
            List<AlertVO> recent = alerts.stream().limit(5).map(a -> {
                AlertVO avo = new AlertVO();
                BeanUtils.copyProperties(a, avo);
                return avo;
            }).toList();
            vo.setRecentAlerts(recent);

            // 最新报告摘要
            Report latestReport = new com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<>(
                    reportMapper)
                    .eq(Report::getUserId, wardId)
                    .orderByDesc(Report::getCreatedAt)
                    .last("LIMIT 1")
                    .one();
            if (latestReport != null) {
                // 简单截取 summary 字段（避免解析整个 JSON）
                String content = latestReport.getContent();
                int idx = content.indexOf("\"summary\":\"");
                if (idx >= 0) {
                    int start = idx + 11;
                    int end = content.indexOf('"', start);
                    if (end > start)
                        vo.setLatestReportSummary(content.substring(start, end));
                }
            }
            return vo;
        }).toList();
    }

    @Override
    public void sendIntervention(Long guardianId, Long wardId, InterventionDTO dto) {
        // 校验绑定关系
        Long count = bindMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GuardianUserBind>()
                        .eq(GuardianUserBind::getGuardianId, guardianId)
                        .eq(GuardianUserBind::getUserId, wardId)
                        .eq(GuardianUserBind::getStatus, 1));
        if (count == 0) {
            throw new BusinessException(403, "您没有权限干预此用户");
        }
        // 通过 WebSocket 推送干预消息到被监护人
        HashMap<String, String> payload = new HashMap<>();
        payload.put("type", "INTERVENTION");
        payload.put("message", dto.getMessage());
        payload.put("createdAt", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/alert/" + wardId, payload);
    }
}
