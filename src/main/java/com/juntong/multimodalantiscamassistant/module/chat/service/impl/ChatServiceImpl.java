package com.juntong.multimodalantiscamassistant.module.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juntong.multimodalantiscamassistant.common.ml.MlPredictRequest;
import com.juntong.multimodalantiscamassistant.common.ml.MlPredictResult;
import com.juntong.multimodalantiscamassistant.common.ml.MlServiceClient;
import com.juntong.multimodalantiscamassistant.module.alert.service.AlertService;
import com.juntong.multimodalantiscamassistant.module.chat.dto.SendMessageDTO;
import com.juntong.multimodalantiscamassistant.module.chat.entity.ChatMessage;
import com.juntong.multimodalantiscamassistant.module.chat.mapper.ChatMessageMapper;
import com.juntong.multimodalantiscamassistant.module.chat.vo.ChatMessageVO;
import com.juntong.multimodalantiscamassistant.module.chat.vo.ChatResponseVO;
import com.juntong.multimodalantiscamassistant.module.user.entity.User;
import com.juntong.multimodalantiscamassistant.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    private final MlServiceClient mlServiceClient;
    private final AlertService alertService;
    private final UserMapper userMapper;

    /** 发送消息，返回 AI 回复 + 风险评估 */
    public ChatResponseVO send(Long userId, SendMessageDTO dto) {
        User user = userMapper.selectById(userId);

        // 取最近 10 条历史作为多轮上下文（DESC取最新，再反转成时间正序）
        List<ChatMessage> history = new java.util.ArrayList<>(lambdaQuery()
                .eq(ChatMessage::getUserId, userId)
                .orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT 10")
                .list());
        java.util.Collections.reverse(history);

        List<Map<String, String>> contextHistory = history.stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .toList();

        // 组装 ML 请求
        MlPredictRequest request = MlPredictRequest.builder()
                .text(dto.getText())
                .fileUrl(dto.getFileUrl())
                .userProfile(Map.of(
                        "ageGroup", user.getAgeGroup() != null ? user.getAgeGroup() : 0,
                        "occupation", user.getOccupation() != null ? user.getOccupation() : "",
                        "riskThreshold", user.getRiskThreshold() != null ? user.getRiskThreshold() : 70))
                .conversationHistory(contextHistory)
                .build();

        MlPredictResult result = mlServiceClient.predict(request);

        // 存用户消息
        saveMessage(userId, "user", dto.getText(), dto.getFileUrl());

        // 高风险触发预警
        if (result.getRiskScore() != null && result.getRiskScore() >= 70) {
            alertService.createAndPush(userId, result.getScamType(),
                    result.getSummary(), result.getSeverity());
        }

        // 生成 AI 回复内容（基于 summary + recommendedQuestions）
        String reply = buildReply(result);
        saveMessage(userId, "assistant", reply, null);

        // 组装响应
        ChatResponseVO vo = new ChatResponseVO();
        vo.setReply(reply);
        vo.setRiskScore(result.getRiskScore());
        vo.setRiskLevel(result.getRiskLevel());
        vo.setScamType(result.getScamType());
        vo.setRiskAction(result.getRiskAction());
        vo.setSummary(result.getSummary());
        vo.setRecommendedQuestions(result.getRecommendedQuestions());
        vo.setCreatedAt(LocalDateTime.now());
        return vo;
    }

    /** 获取历史消息列表 */
    public List<ChatMessageVO> history(Long userId) {
        return lambdaQuery()
                .eq(ChatMessage::getUserId, userId)
                .orderByAsc(ChatMessage::getCreatedAt)
                .list()
                .stream()
                .map(m -> {
                    ChatMessageVO vo = new ChatMessageVO();
                    BeanUtils.copyProperties(m, vo);
                    return vo;
                }).toList();
    }

    private void saveMessage(Long userId, String role, String content, String fileUrl) {
        save(ChatMessage.builder()
                .userId(userId)
                .role(role)
                .content(content != null ? content : "")
                .fileUrl(fileUrl)
                .createdAt(LocalDateTime.now())
                .build());
    }

    /** 将 ML 结果转化为面向用户的 AI 回复文本 */
    private String buildReply(MlPredictResult result) {
        StringBuilder sb = new StringBuilder();
        if (result.getSummary() != null)
            sb.append(result.getSummary());
        if (result.getRecommendedQuestions() != null && !result.getRecommendedQuestions().isEmpty()) {
            sb.append("\n\n我还需要了解一些情况：");
            result.getRecommendedQuestions().forEach(q -> sb.append("\n• ").append(q.getQuestion()));
        }
        return sb.toString();
    }
}
