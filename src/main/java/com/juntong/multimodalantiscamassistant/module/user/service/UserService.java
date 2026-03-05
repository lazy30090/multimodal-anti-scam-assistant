package com.juntong.multimodalantiscamassistant.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juntong.multimodalantiscamassistant.module.user.dto.LoginDTO;
import com.juntong.multimodalantiscamassistant.module.user.dto.RegisterDTO;
import com.juntong.multimodalantiscamassistant.module.user.dto.UpdateConfigDTO;
import com.juntong.multimodalantiscamassistant.module.user.dto.UpdateProfileDTO;
import com.juntong.multimodalantiscamassistant.module.user.entity.User;
import com.juntong.multimodalantiscamassistant.module.user.vo.UserVO;

public interface UserService extends IService<User> {

    /** 注册，返回 JWT Token */
    String register(RegisterDTO dto);

    /** 登录，返回 JWT Token */
    String login(LoginDTO dto);

    /** 获取用户信息 */
    UserVO getProfile(Long userId);

    /** 更新角色画像（年龄段、性别、职业） */
    void updateProfile(Long userId, UpdateProfileDTO dto);

    /** 更新风险配置（灵敏度、阈值、干预策略） */
    void updateConfig(Long userId, UpdateConfigDTO dto);

    /** 获取用户画像供 ML 服务使用 */
    UserVO getMemory(Long userId);
}
