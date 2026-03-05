package com.juntong.multimodalantiscamassistant.module.user.vo;

import com.juntong.multimodalantiscamassistant.module.guardian.vo.BindVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应体
 */
@Data
@Schema(description = "用户信息响应模型")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "年龄段: 1=儿童 2=青壮年 3=老年")
    private Integer ageGroup;

    @Schema(description = "性别: 1=男 2=女")
    private Integer gender;

    @Schema(description = "职业")
    private String occupation;

    @Schema(description = "风险灵敏度: 1=低 2=中 3=高")
    private Integer riskPreference;

    @Schema(description = "当前风险分阈值")
    private BigDecimal riskThreshold;

    @Schema(description = "干预策略: 1=弹窗提醒 2=语音阻断 3=自动联系监护人")
    private Integer interventionStrategy;

    @Schema(description = "账号创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "已绑定的监护人列表")
    private List<BindVO> binds;
}
