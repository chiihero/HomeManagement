-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active-活跃, locked-锁定, disabled-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
ALTER TABLE `user` ADD COLUMN `role` varchar(50) DEFAULT "USER" COMMENT "角色：ADMIN-管理员，USER-普通用户" AFTER `avatar`

-- 标签表
CREATE TABLE IF NOT EXISTS `tag` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
                                     `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    `owner_id` BIGINT NOT NULL COMMENT '所有者ID',
    `create_user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_owner` (`owner_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 实体标签关联表
CREATE TABLE IF NOT EXISTS `entity_tag` (
                                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
                                            `entity_id` BIGINT NOT NULL COMMENT '实体ID',
                                            `tag_id` BIGINT NOT NULL COMMENT '标签ID',
                                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                            PRIMARY KEY (`id`),
    UNIQUE KEY `uk_entity_tag` (`entity_id`, `tag_id`),
    KEY `idx_tag` (`tag_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体标签关联表';

-- 实体表（统一的物品和空间表）
CREATE TABLE IF NOT EXISTS `entity` (
                                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '实体ID',
                                        `name` VARCHAR(100) NOT NULL COMMENT '名称',
    `type` VARCHAR(20) NOT NULL DEFAULT 'item' COMMENT '类型: item-物品, space-空间',
    `code` VARCHAR(50) DEFAULT NULL COMMENT '编码',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
    `quantity` INT DEFAULT 1 COMMENT '数量（物品专用）',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '价格（物品专用）',
    `purchase_date` DATE DEFAULT NULL COMMENT '购买日期（物品专用）',
    `warranty_period` INT DEFAULT NULL COMMENT '保修期(月)（物品专用）',
    `warranty_end_date` DATE DEFAULT NULL COMMENT '保修截止日期（物品专用）',
    `usage_frequency` VARCHAR(20) DEFAULT NULL COMMENT '使用频率: daily-每天, weekly-每周, monthly-每月, rarely-很少（物品专用）',
    `usage_years` INT DEFAULT NULL COMMENT '使用年限（物品专用）',
    `user_id` BIGINT DEFAULT NULL COMMENT '使用人ID（物品专用）',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父实体ID（物品存放在哪个实体中）',
    `level` INT DEFAULT 1 COMMENT '层级（空间专用）',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '路径，例如: 1,2,3（空间专用）',
    `sort` INT DEFAULT 0 COMMENT '排序（空间专用）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '状态: normal-正常, damaged-损坏, discarded-丢弃, lent-借出（物品专用）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述/备注',
    `owner_id` BIGINT NOT NULL COMMENT '所有者ID',
    `create_user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_owner` (`owner_id`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体表（统一的物品和空间）';

-- 实体图片表
CREATE TABLE IF NOT EXISTS `entity_image` (
                                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
                                              `entity_id` BIGINT NOT NULL COMMENT '实体ID',
                                              `image_url` VARCHAR(255) COMMENT '图片URL',
    `image_data` MEDIUMBLOB COMMENT '图片二进制数据',
    `image_type` VARCHAR(20) DEFAULT 'normal' COMMENT '图片类型: normal-普通, receipt-购买凭证, warranty-保修凭证',
    `content_type` VARCHAR(100) COMMENT '内容类型，如image/jpeg, image/png等',
    `file_name` VARCHAR(255) COMMENT '原始文件名',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    KEY `idx_entity` (`entity_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体图片表';

-- 物品维护记录表
CREATE TABLE IF NOT EXISTS `entity_maintenance` (
                                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维护记录ID',
                                                    `entity_id` BIGINT NOT NULL COMMENT '实体ID',
                                                    `maintenance_date` DATE NOT NULL COMMENT '维护日期',
                                                    `maintenance_type` VARCHAR(20) NOT NULL COMMENT '维护类型: repair-维修, clean-清洁, check-检查',
    `cost` DECIMAL(10, 2) DEFAULT NULL COMMENT '花费',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `result` VARCHAR(20) DEFAULT NULL COMMENT '结果: success-成功, failure-失败',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_entity` (`entity_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体维护记录表';

-- 提醒表
CREATE TABLE IF NOT EXISTS `reminder` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
                                           `entity_id` BIGINT DEFAULT NULL COMMENT '实体ID',
                                           `owner_id` BIGINT NOT NULL COMMENT '所有者ID',
                                           `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '内容',
    `remind_date` DATE NOT NULL COMMENT '提醒日期',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: warranty-保修到期, maintenance-维护提醒, other-其他',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待处理, processed-已处理, ignored-已忽略',
    `create_user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_entity` (`entity_id`),
    KEY `idx_owner` (`owner_id`),
    KEY `idx_remind_date` (`remind_date`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒表';

-- 创建系统设置表
CREATE TABLE `system_settings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(50) NOT NULL COMMENT '设置类型：SYSTEM-系统设置, USER-用户个人设置',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID（对于用户个人设置）',
  `setting_key` varchar(100) NOT NULL COMMENT '设置键',
  `setting_value` text DEFAULT NULL COMMENT '设置值',
  `name` varchar(200) DEFAULT NULL COMMENT '设置名称/描述',
  `default_value` varchar(500) DEFAULT NULL COMMENT '预设值',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_key_user` (`type`, `setting_key`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

-- 添加初始系统设置数据
INSERT INTO `system_settings` (`type`, `setting_key`, `setting_value`, `name`, `default_value`, `created_at`, `updated_at`)
VALUES 
('SYSTEM', 'language', 'zh_CN', '系统语言', 'zh_CN', NOW(), NOW()),
('SYSTEM', 'theme', 'light', '系统主题', 'light', NOW(), NOW()),
('SYSTEM', 'dateFormat', 'yyyy-MM-dd', '日期格式', 'yyyy-MM-dd', NOW(), NOW()),
('SYSTEM', 'currency', 'CNY', '货币单位', 'CNY', NOW(), NOW()),
('SYSTEM', 'autoBackup', 'false', '自动备份', 'false', NOW(), NOW()),
('SYSTEM', 'backupFrequency', 'monthly', '备份频率', 'monthly', NOW(), NOW()),
('SYSTEM', 'backupTime', '03:00', '备份时间', '03:00', NOW(), NOW()),
('SYSTEM', 'backupRetention', '5', '备份保留数量', '5', NOW(), NOW()),
('SYSTEM', 'systemNotification', 'true', '系统通知', 'true', NOW(), NOW()),
('SYSTEM', 'reminderEnabled', 'true', '记账提醒', 'true', NOW(), NOW()),
('SYSTEM', 'reminderTime', '20:00', '提醒时间', '20:00', NOW(), NOW()),
('SYSTEM', 'sessionTimeout', '30', '会话超时时间(分钟)', '30', NOW(), NOW());
