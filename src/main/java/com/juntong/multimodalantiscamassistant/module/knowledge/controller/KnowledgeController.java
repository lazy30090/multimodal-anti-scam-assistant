package com.juntong.multimodalantiscamassistant.module.knowledge.controller;

import com.juntong.multimodalantiscamassistant.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Tag(name = "6. 知识库进化模块", description = "支持上传多模态诈骗案例，驱动算法模型的知识增量学习与进化")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    /** 上传目录，实际部署时应从配置读取 */
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/anti-scam-uploads/knowledge/";

    @Operation(summary = "提交新诈骗案例", description = "上传包含最新诈骗套路的文件（文本、图片、音视频等），提交后系统将异步触发特征提取与向量化入库，提升 AI 的识别精准度。")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Result<String> upload(
            @Parameter(description = "多模态案例文件", content = @Content(mediaType = "multipart/form-data")) @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail(400, "文件不能为空");
        }
        // 保存文件到本地磁盘
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(UPLOAD_DIR + filename);
        dest.getParentFile().mkdirs();
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            return Result.fail("文件保存失败: " + e.getMessage());
        }

        // TODO: 等 ML 团队提供接口后，在此处调用 ML 服务触发向量化入库
        // mlServiceClient.triggerVectorize(dest.getAbsolutePath());

        return Result.ok("上传成功，文件路径: " + dest.getAbsolutePath());
    }
}
