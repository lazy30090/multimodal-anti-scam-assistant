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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import com.juntong.multimodalantiscamassistant.common.exception.BusinessException;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    private final MlServiceClient mlServiceClient;
    private final AlertService alertService;
    private final UserMapper userMapper;

    /** HTTP URL 嗅探正则 */
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)(http|https)://.*");

    /** 发送消息，返回 AI 回复 + 风险评估 */
    public ChatResponseVO send(Long userId, SendMessageDTO dto) {

        // --- 1. 接入校验与基础清洗 ---
        String rawContent = dto.getContent();
        // 清洗：去除两端空格并强制转化为规整的 UTF-8 防止越界乱码
        String content = rawContent == null ? ""
                : new String(rawContent.trim().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        String fileUrl = dto.getFileUrl() == null ? "" : dto.getFileUrl().trim();

        // 核心阻断：内容不允许双空
        if (!StringUtils.hasText(content) && !StringUtils.hasText(fileUrl)) {
            throw new BusinessException(400, "非法接入，发送的文本或图片不能为空");
        }

        // --- 2. 基础安全校验 ---
        // 利用正则判断文本中是否包含外部 URL 并给 ML 报告打上印记
        boolean hasUrl = StringUtils.hasText(content) && URL_PATTERN.matcher(content).find();

        User user = userMapper.selectById(userId);

        // --- 3. (无需上下文特征保留) ---

        // --- 4. 元数据与特征补全 ---
        // 封装灵活字典，将时间、来源与用户画像聚合
        Map<String, Object> metaProfile = new HashMap<>();
        metaProfile.put("timestamp", System.currentTimeMillis()); // 请求时间戳
        metaProfile.put("inputType", StringUtils.hasText(fileUrl) ? "MULTIMODAL" : "TEXT"); // 输入来源标记
        metaProfile.put("containsHyperlink", hasUrl); // 安全感知预警标注

        metaProfile.put("userId", userId);
        metaProfile.put("ageGroup", user.getAgeGroup() != null ? user.getAgeGroup() : 0);
        metaProfile.put("occupation", user.getOccupation() != null ? user.getOccupation() : "");
        metaProfile.put("riskThreshold", user.getRiskThreshold() != null ? user.getRiskThreshold() : 70);

        // 组装 ML 预测请求载体
        MlPredictRequest request = MlPredictRequest.builder()
                .text(content)
                .fileUrl(StringUtils.hasText(fileUrl) ? fileUrl : null)
                .userProfile(metaProfile)
                .build();

        MlPredictResult result = mlServiceClient.predict(request);

        // 存用户消息
        saveMessage(userId, "user", content, StringUtils.hasText(fileUrl) ? fileUrl : null);

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
