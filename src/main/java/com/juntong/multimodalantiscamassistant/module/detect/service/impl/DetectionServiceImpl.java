package com.juntong.multimodalantiscamassistant.module.detect.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juntong.multimodalantiscamassistant.common.ml.MlPredictRequest;
import com.juntong.multimodalantiscamassistant.common.ml.MlPredictResult;
import com.juntong.multimodalantiscamassistant.common.ml.MlServiceClient;
import com.juntong.multimodalantiscamassistant.module.alert.service.AlertService;
import com.juntong.multimodalantiscamassistant.module.detect.dto.BatchDetectDTO;
import com.juntong.multimodalantiscamassistant.module.detect.dto.BatchDetectItemDTO;
import com.juntong.multimodalantiscamassistant.module.detect.dto.DetectDTO;
import com.juntong.multimodalantiscamassistant.module.detect.entity.DetectionRecord;
import com.juntong.multimodalantiscamassistant.module.detect.mapper.DetectionRecordMapper;
import com.juntong.multimodalantiscamassistant.module.detect.vo.BatchDetectResultVO;
import com.juntong.multimodalantiscamassistant.module.detect.vo.DetectResultVO;
import com.juntong.multimodalantiscamassistant.module.user.entity.User;
import com.juntong.multimodalantiscamassistant.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DetectionServiceImpl extends ServiceImpl<DetectionRecordMapper, DetectionRecord> {

        private final MlServiceClient mlServiceClient;
        private final AlertService alertService;
        private final UserMapper userMapper;

        /**
         * 多模态检测核心逻辑：
         * 1. 组装用户画像 + 内容发给 ML
         * 2. 根据 riskScore 决定是否触发预警
         * 3. 存结果到 detection_record
         */
        public DetectResultVO detect(Long userId, DetectDTO dto) {
                User user = userMapper.selectById(userId);

                // 组装 ML 请求
                MlPredictRequest request = MlPredictRequest.builder()
                                .text(dto.getText())
                                .fileUrl(dto.getFileUrl())
                                .userProfile(Map.of(
                                                "ageGroup", user.getAgeGroup() != null ? user.getAgeGroup() : 0,
                                                "occupation", user.getOccupation() != null ? user.getOccupation() : "",
                                                "riskThreshold",
                                                user.getRiskThreshold() != null ? user.getRiskThreshold() : 70))
                                .build();

                MlPredictResult result = mlServiceClient.predict(request);

                // 高风险则创建预警并推送 WebSocket
                if (result.getRiskScore() != null && result.getRiskScore() >= 70) {
                        alertService.createAndPush(userId, result.getScamType(),
                                        result.getSummary(), result.getSeverity());
                }

                // 持久化检测记录
                DetectionRecord record = DetectionRecord.builder()
                                .userId(userId)
                                .fileUrl(dto.getFileUrl())
                                .textContent(dto.getText())
                                .riskScore(BigDecimal
                                                .valueOf(result.getRiskScore() != null ? result.getRiskScore() : 0))
                                .scamType(result.getScamType() != null ? result.getScamType() : "OTHER_UNKNOWN")
                                .riskAction(result.getRiskAction() != null ? result.getRiskAction() : "EDUCATE")
                                .summary(result.getSummary() != null ? result.getSummary() : "")
                                .createdAt(LocalDateTime.now())
                                .build();
                save(record);

                // 组装响应
                DetectResultVO vo = new DetectResultVO();
                vo.setRecordId(record.getId());
                vo.setRiskScore(result.getRiskScore());
                vo.setRiskLevel(result.getRiskLevel());
                vo.setScamType(result.getScamType());
                vo.setConfidence(result.getConfidence());
                vo.setRiskAction(result.getRiskAction());
                vo.setEvidence(result.getEvidence());
                vo.setSummary(result.getSummary());
                vo.setCreatedAt(record.getCreatedAt());
                return vo;
        }

        /**
         * 批量测试接口：读取文件集列表，逐一送测，最终计算 F1-score 与准确率等指标
         */
        public BatchDetectResultVO detectBatch(Long userId, BatchDetectDTO dto) {
                int tp = 0; // True Positive: 预期是欺诈，实际是欺诈
                int fp = 0; // False Positive: 预期不是欺诈，实际是欺诈（误报）
                int fn = 0; // False Negative: 预期是欺诈，实际不是欺诈（漏报）
                int tn = 0; // True Negative: 预期不是欺诈，实际也不是欺诈

                List<DetectResultVO> detailResults = new ArrayList<>();

                for (BatchDetectItemDTO item : dto.getItems()) {
                        DetectResultVO resultVO = detect(userId, item);
                        detailResults.add(resultVO);

                        // 假设得分 >= 70 判定为欺诈（即模型认为是欺诈）
                        boolean modelIsScam = resultVO.getRiskScore() != null && resultVO.getRiskScore() >= 70;
                        boolean expectedIsScam = item.getExpectedIsScam() != null && item.getExpectedIsScam() == 1;

                        if (expectedIsScam && modelIsScam) {
                                tp++;
                        } else if (!expectedIsScam && modelIsScam) {
                                fp++;
                        } else if (expectedIsScam && !modelIsScam) {
                                fn++;
                        } else {
                                tn++;
                        }
                }

                BatchDetectResultVO batchVO = new BatchDetectResultVO();
                batchVO.setTotalCount(dto.getItems().size());
                batchVO.setTruePositive(tp);
                batchVO.setFalsePositive(fp);
                batchVO.setTrueNegative(tn);
                batchVO.setFalseNegative(fn);

                // 计算 metrics
                double accuracy = batchVO.getTotalCount() == 0 ? 0 : (double) (tp + tn) / batchVO.getTotalCount();

                double precision = (tp + fp) == 0 ? 0 : (double) tp / (tp + fp);
                double recall = (tp + fn) == 0 ? 0 : (double) tp / (tp + fn);

                double f1Score = (precision + recall) == 0 ? 0 : 2 * (precision * recall) / (precision + recall);

                batchVO.setAccuracy(accuracy);
                batchVO.setPrecision(precision);
                batchVO.setRecall(recall);
                batchVO.setF1Score(f1Score);
                batchVO.setDetailResults(detailResults);

                return batchVO;
        }
}
