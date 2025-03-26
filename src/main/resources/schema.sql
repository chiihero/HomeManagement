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

-- 家庭表
CREATE TABLE IF NOT EXISTS `family` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '家庭ID',
    `name` VARCHAR(50) NOT NULL COMMENT '家庭名称',
    `description` VARCHAR(255) COMMENT '描述',
    `create_user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_user` (`create_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭表';

-- 家庭成员表
CREATE TABLE IF NOT EXISTS `family_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '家庭成员ID',
    `family_id` BIGINT NOT NULL COMMENT '家庭ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT '角色: admin-管理员, member-成员',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_family_user` (`family_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员表';

-- 物品分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父分类ID',
    `level` INT DEFAULT NULL COMMENT '层级',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '分类路径，例如: 1,2,3',
    `sort` INT DEFAULT NULL COMMENT '排序',
    `family_id` BIGINT NOT NULL COMMENT '所属家庭ID',
    `create_user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_family` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品分类表';

-- 空间表
CREATE TABLE IF NOT EXISTS `space` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '空间ID',
    `name` VARCHAR(50) NOT NULL COMMENT '空间名称',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父空间ID',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '空间路径，例如: 1,2,3',
    `family_id` BIGINT NOT NULL COMMENT '所属家庭ID',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `create_user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_family` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='空间表(存放位置)';

-- 物品表
CREATE TABLE IF NOT EXISTS `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    `name` VARCHAR(100) NOT NULL COMMENT '物品名称',
    `code` VARCHAR(50) DEFAULT NULL COMMENT '物品编码',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '价格',
    `purchase_date` DATE DEFAULT NULL COMMENT '购买日期',
    `warranty_period` INT DEFAULT NULL COMMENT '保修期(月)',
    `warranty_end_date` DATE DEFAULT NULL COMMENT '保修截止日期',
    `usage_frequency` VARCHAR(20) DEFAULT NULL COMMENT '使用频率: daily-每天, weekly-每周, monthly-每月, rarely-很少',
    `usage_years` INT DEFAULT NULL COMMENT '使用年限',
    `user_id` BIGINT DEFAULT NULL COMMENT '使用人ID',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `space_id` BIGINT DEFAULT NULL COMMENT '存放空间ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '状态: normal-正常, damaged-损坏, discarded-丢弃, lent-借出',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `family_id` BIGINT NOT NULL COMMENT '所属家庭ID',
    `create_user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_family` (`family_id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_space` (`space_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品表';

-- 物品图片表
CREATE TABLE IF NOT EXISTS `item_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
    `image_type` VARCHAR(20) DEFAULT 'normal' COMMENT '图片类型: normal-普通, receipt-购买凭证, warranty-保修凭证',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品图片表';

-- 物品维护记录表
CREATE TABLE IF NOT EXISTS `item_maintenance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维护记录ID',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `maintenance_date` DATE NOT NULL COMMENT '维护日期',
    `maintenance_type` VARCHAR(20) NOT NULL COMMENT '维护类型: repair-维修, clean-清洁, check-检查',
    `cost` DECIMAL(10, 2) DEFAULT NULL COMMENT '花费',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `result` VARCHAR(20) DEFAULT NULL COMMENT '结果: success-成功, failure-失败',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品维护记录表';

-- 物品借出表
CREATE TABLE IF NOT EXISTS `item_lending` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '借出记录ID',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `borrower_name` VARCHAR(50) NOT NULL COMMENT '借用人姓名',
    `borrower_contact` VARCHAR(50) DEFAULT NULL COMMENT '借用人联系方式',
    `lending_date` DATE NOT NULL COMMENT '借出日期',
    `expected_return_date` DATE DEFAULT NULL COMMENT '预计归还日期',
    `actual_return_date` DATE DEFAULT NULL COMMENT '实际归还日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'lent' COMMENT '状态: lent-已借出, returned-已归还, overdue-逾期未还',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_item` (`item_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品借出表';

-- 提醒表
CREATE TABLE IF NOT EXISTS `reminder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
    `item_id` BIGINT DEFAULT NULL COMMENT '物品ID',
    `family_id` BIGINT NOT NULL COMMENT '家庭ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '内容',
    `remind_date` DATE NOT NULL COMMENT '提醒日期',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: warranty-保修到期, maintenance-维护提醒, lending-借出提醒, other-其他',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待处理, processed-已处理, ignored-已忽略',
    `create_user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_item` (`item_id`),
    KEY `idx_family` (`family_id`),
    KEY `idx_remind_date` (`remind_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒表';
