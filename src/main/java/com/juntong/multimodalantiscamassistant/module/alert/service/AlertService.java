package com.juntong.multimodalantiscamassistant.module.alert.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juntong.multimodalantiscamassistant.module.alert.entity.Alert;
import com.juntong.multimodalantiscamassistant.module.alert.vo.AlertVO;

import java.util.List;

public interface AlertService extends IService<Alert> {

    /** 获取当前用户的历史预警列表 */
    List<AlertVO> listHistory(Long userId);

    /** 获取单条预警详情 */
    AlertVO getDetail(Long alertId, Long userId);

    /**
     * 创建预警记录并通过 WebSocket 实时推送给用户
     * 由多轮对话/多模态检测模块在 ML 返回高风险时调用
     */
    void createAndPush(Long userId, String scamType, String reasoning, Integer severity);
}
