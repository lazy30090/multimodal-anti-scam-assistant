package com.juntong.multimodalantiscamassistant.common.ml;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 发往 ML 服务的预测请求体
 */
@Data
@Builder
public class MlPredictRequest {

    /** 文本内容（可为空） */
    private String text;

    /** 文件访问 URL（音频/图片，可为空） */
    private String fileUrl;

    /** 用户画像，影响个性化风险阈值 */
    private Map<String, Object> userProfile;

    /** 最近 N 条对话上下文（多轮对话时传入） */
    private List<Map<String, String>> conversationHistory;
}
