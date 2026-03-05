package com.juntong.multimodalantiscamassistant.module.alert.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juntong.multimodalantiscamassistant.common.exception.BusinessException;
import com.juntong.multimodalantiscamassistant.module.alert.entity.Alert;
import com.juntong.multimodalantiscamassistant.module.alert.mapper.AlertMapper;
import com.juntong.multimodalantiscamassistant.module.alert.service.AlertService;
import com.juntong.multimodalantiscamassistant.module.alert.vo.AlertVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {

    /** Spring 内置的 STOMP 消息发送工具 */
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<AlertVO> listHistory(Long userId) {
        return lambdaQuery()
                .eq(Alert::getUserId, userId)
                .orderByDesc(Alert::getCreatedAt)
                .list()
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public AlertVO getDetail(Long alertId, Long userId) {
        Alert alert = getById(alertId);
        if (alert == null || !alert.getUserId().equals(userId)) {
            throw new BusinessException(404, "预警记录不存在");
        }
        return toVO(alert);
    }

    @Override
    public void createAndPush(Long userId, String scamType, String reasoning, Integer severity) {
        // 1. 持久化到数据库
        Alert alert = Alert.builder()
                .userId(userId)
                .scamType(scamType)
                .reasoning(reasoning)
                .severity(severity)
                .createdAt(LocalDateTime.now())
                .build();
        save(alert);

        // 2. 通过 WebSocket 实时推送给该用户
        // 前端订阅 /topic/alert/{userId} 即可收到
        messagingTemplate.convertAndSend("/topic/alert/" + userId, toVO(alert));
    }

    private AlertVO toVO(Alert alert) {
        AlertVO vo = new AlertVO();
        BeanUtils.copyProperties(alert, vo);
        return vo;
    }
}
