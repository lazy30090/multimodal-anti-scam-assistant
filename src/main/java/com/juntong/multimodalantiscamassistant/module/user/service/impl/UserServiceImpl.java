package com.juntong.multimodalantiscamassistant.module.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juntong.multimodalantiscamassistant.common.exception.BusinessException;
import com.juntong.multimodalantiscamassistant.common.util.JwtUtil;
import com.juntong.multimodalantiscamassistant.module.user.dto.LoginDTO;
import com.juntong.multimodalantiscamassistant.module.user.dto.RegisterDTO;
import com.juntong.multimodalantiscamassistant.module.user.dto.UpdateConfigDTO;
import com.juntong.multimodalantiscamassistant.module.user.dto.UpdateProfileDTO;
import com.juntong.multimodalantiscamassistant.module.user.entity.User;
import com.juntong.multimodalantiscamassistant.module.user.mapper.UserMapper;
import com.juntong.multimodalantiscamassistant.module.guardian.service.GuardianService;
import com.juntong.multimodalantiscamassistant.module.user.service.UserService;
import com.juntong.multimodalantiscamassistant.module.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    @Lazy
    private final GuardianService guardianService;

    @Override
    public String register(RegisterDTO dto) {
        // 用户名唯一校验
        if (lambdaQuery().eq(User::getUsername, dto.getUsername()).exists()) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .ageGroup(2) // 默认青壮年
                .riskPreference(2) // 默认中等灵敏度
                .riskThreshold(new BigDecimal("70.00"))
                .interventionStrategy(1) // 默认弹窗提醒
                .build();
        save(user);
        return jwtUtil.generateToken(user.getId());
    }

    @Override
    public String login(LoginDTO dto) {
        // 使用 select = false 会跳过密码，这里需要手动查询带密码的用户
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .select(User::getId, User::getUsername, User::getPassword)
                .one();
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return jwtUtil.generateToken(user.getId());
    }

    @Override
    public UserVO getProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserVO vo = toVO(user);
        vo.setBinds(guardianService.listBinds(userId));
        return vo;
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        lambdaUpdate()
                .eq(User::getId, userId)
                .set(dto.getAgeGroup() != null, User::getAgeGroup, dto.getAgeGroup())
                .set(dto.getGender() != null, User::getGender, dto.getGender())
                .set(dto.getOccupation() != null, User::getOccupation, dto.getOccupation())
                .update();
    }

    @Override
    public void updateConfig(Long userId, UpdateConfigDTO dto) {
        lambdaUpdate()
                .eq(User::getId, userId)
                .set(dto.getRiskPreference() != null, User::getRiskPreference, dto.getRiskPreference())
                .set(dto.getRiskThreshold() != null, User::getRiskThreshold, dto.getRiskThreshold())
                .set(dto.getInterventionStrategy() != null, User::getInterventionStrategy,
                        dto.getInterventionStrategy())
                .update();
    }

    @Override
    public UserVO getMemory(Long userId) {
        // 画像即用户的角色属性，直接复用 getProfile
        return getProfile(userId);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
