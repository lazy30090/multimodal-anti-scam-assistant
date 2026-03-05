package com.juntong.multimodalantiscamassistant.module.detect.controller;

import com.juntong.multimodalantiscamassistant.common.Result;
import com.juntong.multimodalantiscamassistant.common.exception.BusinessException;
import com.juntong.multimodalantiscamassistant.module.detect.dto.BatchDetectDTO;
import com.juntong.multimodalantiscamassistant.module.detect.dto.DetectDTO;
import com.juntong.multimodalantiscamassistant.module.detect.service.impl.DetectionServiceImpl;
import com.juntong.multimodalantiscamassistant.module.detect.vo.BatchDetectResultVO;
import com.juntong.multimodalantiscamassistant.module.detect.vo.DetectResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "5. 多模态检测模块", description = "提供单次消息检测及批量测评自动化工具，支持文本、图片及语音")
@RestController
@RequestMapping("/api/detect")
@RequiredArgsConstructor
public class DetectionController {

    private final DetectionServiceImpl detectionService;

    @Operation(summary = "单次多模态检测", description = "非对话场景下的直接风险检测。支持文本、图片、长文本或语音文件地址。")
    @PostMapping("/multimodal")
    public Result<DetectResultVO> detect(@RequestBody DetectDTO dto) {
        if ((dto.getText() == null || dto.getText().isBlank()) && dto.getFileUrl() == null) {
            throw new BusinessException(400, "text 和 fileUrl 至少填一个");
        }
        return Result.ok(detectionService.detect(currentId(), dto));
    }

    @Operation(summary = "批量自动化检测与测评", description = "支持传入包含 label 的测试集，系统自动完成批处理检测并返回模型评估指标 (F1-score, Accuracy)")
    @PostMapping("/batch")
    public Result<BatchDetectResultVO> detectBatch(@RequestBody BatchDetectDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException(400, "批量检测列表不能为空");
        }
        return Result.ok(detectionService.detectBatch(currentId(), dto));
    }

    private Long currentId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
