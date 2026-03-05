package com.juntong.multimodalantiscamassistant.module.guardian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juntong.multimodalantiscamassistant.module.guardian.dto.*;
import com.juntong.multimodalantiscamassistant.module.guardian.entity.Guardian;
import com.juntong.multimodalantiscamassistant.module.guardian.vo.BindVO;
import com.juntong.multimodalantiscamassistant.module.guardian.vo.WhitelistVO;

import java.util.List;

import com.juntong.multimodalantiscamassistant.module.guardian.dto.InterventionDTO;
import com.juntong.multimodalantiscamassistant.module.guardian.vo.WardSummaryVO;

public interface GuardianService extends IService<Guardian> {

    /** 微信小程序登录，返回 JWT Token */
    String wxLogin(WxLoginDTO dto);

    /** 用户绑定监护人（按手机号匹配） */
    void bind(Long userId, BindGuardianDTO dto);

    /** 用户解绑监护人 */
    void unbind(Long userId, Long guardianId);

    /** 获取用户已绑定的监护人列表 */
    List<BindVO> listBinds(Long userId);

    /** 获取用户的白名单列表 */
    List<WhitelistVO> listWhitelist(Long userId);

    /** 用户添加白名单条目 */
    void addWhitelist(Long userId, AddWhitelistDTO dto);

    /** 监护人更新通知策略 */
    void updateNotifyPolicy(Long guardianId, UpdateNotifyPolicyDTO dto);

    /** 监护人主动拉取所有被监护人的安全周报 */
    List<WardSummaryVO> getWardSummaries(Long guardianId);

    /** 监护人远程干预：向被监护人实时推送干预消息 */
    void sendIntervention(Long guardianId, Long wardId, InterventionDTO dto);
}
