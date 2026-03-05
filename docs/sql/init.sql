-- ============================================================
-- 多模态反诈智能助手 — 数据库初始化脚本
-- 创建时间: 2026-03-04
-- ============================================================

CREATE DATABASE IF NOT EXISTS anti_scam CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE anti_scam;

-- ------------------------------------------------------------
-- 1. 用户表（网页端）
-- ------------------------------------------------------------
CREATE TABLE `user`
(
    `id`                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`              VARCHAR(50)   NOT NULL COMMENT '用户名',
    `password`              VARCHAR(255)  NOT NULL COMMENT 'BCrypt 加密密码',
    -- 角色属性（影响个性化风险阈值，对应赛题"角色定制服务"）
    `age_group`             TINYINT       NOT NULL DEFAULT 2 COMMENT '年龄段: 1=儿童 2=青壮年 3=老年',
    `gender`                TINYINT       DEFAULT NULL COMMENT '性别: 1=男 2=女',
    `occupation`            VARCHAR(50)   DEFAULT NULL COMMENT '职业标签，如 student/finance',
    -- 风险配置（对应赛题"个性化风险评估"）
    `risk_preference`       TINYINT       NOT NULL DEFAULT 2 COMMENT '风险灵敏度: 1=低 2=中 3=高',
    `risk_threshold`        DECIMAL(5, 2) NOT NULL DEFAULT 70.00 COMMENT '触发预警的风险分阈值(0~100)',
    `intervention_strategy` TINYINT       NOT NULL DEFAULT 1 COMMENT '干预策略: 1=弹窗提醒 2=语音阻断 3=自动联系监护人',
    `deleted`               TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=已删除',
    `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表（网页端）';


-- ------------------------------------------------------------
-- 2. 监护人表（微信小程序端）
-- ------------------------------------------------------------
CREATE TABLE `guardian`
(
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '监护人ID',
    `open_id`       VARCHAR(64) NOT NULL COMMENT '微信 openid',
    `nickname`      VARCHAR(50) DEFAULT NULL COMMENT '微信昵称',
    `phone`         VARCHAR(20) DEFAULT NULL COMMENT '联系手机号（绑定时填写）',
    `notify_policy` TINYINT     NOT NULL DEFAULT 1 COMMENT '通知策略: 1=实时推送 2=周期汇总',
    `deleted`       TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=已删除',
    `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_open_id` (`open_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '监护人表（微信小程序端）';


-- ------------------------------------------------------------
-- 3. 监护人-用户绑定关系表
-- 对应：/api/guardian/bind（姓名、电话、关系）
-- ------------------------------------------------------------
CREATE TABLE `guardian_user_bind`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `guardian_id` BIGINT      NOT NULL COMMENT '监护人ID',
    `user_id`     BIGINT      NOT NULL COMMENT '被监护用户ID',
    `relation`    VARCHAR(20) DEFAULT NULL COMMENT '关系，如 父亲/母亲/子女',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '绑定状态: 1=有效 0=已解除',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_guardian_user` (`guardian_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_bind_guardian` FOREIGN KEY (`guardian_id`) REFERENCES `guardian` (`id`),
    CONSTRAINT `fk_bind_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '监护人-用户绑定关系表';


-- ------------------------------------------------------------
-- 4. 白名单表（信任联系人/账号，命中白名单时降低风险评分）
-- 对应：GET/POST /api/guardian/whitelist
-- ------------------------------------------------------------
CREATE TABLE `guardian_whitelist`
(
    `id`           BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT      NOT NULL COMMENT '该白名单归属的用户ID',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人名称',
    `contact_info` VARCHAR(100) NOT NULL COMMENT '联系方式（手机号/账号/微信号等）',
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_whitelist_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '白名单（信任联系人）';


-- ------------------------------------------------------------
-- 5. 预警记录表
-- 对应：GET /api/alert/history、GET /api/alert/detail/{id}、WS /ws/alert
-- ------------------------------------------------------------
CREATE TABLE `alert`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL COMMENT '触发预警的用户ID',
    `scam_type`  VARCHAR(50) NOT NULL COMM ENT '诈骗类型',
    `reasoning`  TEXT        NOT NULL COMMENT '判定依据',
    `severity`   TINYINT     NOT NULL COMMENT '风险等级: 1=低 2=中 3=高',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_alert_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '预警记录表';


-- ------------------------------------------------------------
-- 6. 安全监测报告表
-- 对应：POST /api/report/generate、GET /api/report/list
-- ------------------------------------------------------------
CREATE TABLE `report`
(
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL COMMENT '报告归属用户',
    `start_date` DATE     NOT NULL COMMENT '统计开始日期',
    `end_date`   DATE     NOT NULL COMMENT '统计结束日期',
    `content`    TEXT     NOT NULL COMMENT '报告内容（JSON格式）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_report_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '安全监测报告表';


-- ------------------------------------------------------------
-- 7. 多模态检测记录表
-- 对应：POST /api/detect/multimodal
-- ------------------------------------------------------------
CREATE TABLE `detection_record`
(
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT        NOT NULL COMMENT '触发检测的用户',
    `file_url`    VARCHAR(512)  DEFAULT NULL COMMENT '上传文件的访问 URL（可为空）',
    `text_content` TEXT         DEFAULT NULL COMMENT '文本内容（可为空）',
    `risk_score`  DECIMAL(5,2)  NOT NULL COMMENT 'ML 风险分值',
    `scam_type`   VARCHAR(50)   NOT NULL COMMENT '诈骗类型枚举值',
    `risk_action` VARCHAR(20)   NOT NULL COMMENT 'ML 建议动作: BLOCK/WARN/EDUCATE',
    `summary`     TEXT          NOT NULL COMMENT 'ML 分析摘要',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_detection_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '多模态检测记录表';


-- ------------------------------------------------------------
-- 8. 对话消息表（每个用户一条对话流，无 session 概念）
-- 对应：POST /api/chat/send，GET /api/chat/history
-- ------------------------------------------------------------
CREATE TABLE `chat_message`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL COMMENT '消息归属用户',
    `role`       VARCHAR(20)  NOT NULL COMMENT '角色: user / assistant',
    `content`    TEXT         NOT NULL COMMENT '消息内容',
    `file_url`   VARCHAR(512) DEFAULT NULL COMMENT '附件 URL（可为空）',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '对话消息表';




