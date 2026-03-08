package com.juntong.multimodalantiscamassistant.module.chat.controller;

import com.juntong.multimodalantiscamassistant.common.Result;
import com.juntong.multimodalantiscamassistant.common.exception.BusinessException;
import com.juntong.multimodalantiscamassistant.module.chat.dto.SendMessageDTO;
import com.juntong.multimodalantiscamassistant.module.chat.service.impl.ChatServiceImpl;
import com.juntong.multimodalantiscamassistant.module.chat.vo.ChatMessageVO;
import com.juntong.multimodalantiscamassistant.module.chat.vo.ChatResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "4. 智能对话模块", description = "处理多模态反诈咨询对话，支持文本、图片、语音及链接输入")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatServiceImpl chatService;

    @Operation(summary = "发送咨询消息", description = "用户发起咨询。支持纯文本或文件（图片/语音等），返回 AI 回复及伴随的实时风险评估")
    @PostMapping("/send")
    public Result<ChatResponseVO> send(@RequestBody SendMessageDTO dto) {
        if ((dto.getContent() == null || dto.getContent().isBlank()) && dto.getFileUrl() == null) {
            throw new BusinessException(400, "content 和 fileUrl 至少填一个");
        }
        return Result.ok(chatService.send(currentId(), dto));
    }

    @Operation(summary = "获取当前会话历史", description = "获取当前用户的全量对话记录（含用户消息与 AI 回复）")
    @GetMapping("/history")
    public Result<List<ChatMessageVO>> history() {
        return Result.ok(chatService.history(currentId()));
    }

    private Long currentId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
