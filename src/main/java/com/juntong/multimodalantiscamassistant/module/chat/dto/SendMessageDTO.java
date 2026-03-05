package com.juntong.multimodalantiscamassistant.module.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发送消息请求体
 */
@Data
@Schema(description = "发送对话消息模型")
public class SendMessageDTO {

    @Schema(description = "消息文本内容", example = "这张图片里的中奖信息是真的吗？")
    private String text;

    @Schema(description = "已上传文件的 URL（如图片、语音、链接截图等）", example = "http://example.com/oss/a.jpg")
    private String fileUrl;
}
