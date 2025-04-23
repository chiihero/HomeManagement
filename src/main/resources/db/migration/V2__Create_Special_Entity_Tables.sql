-- 创建耗材表
CREATE TABLE IF NOT EXISTS `consumable_entity` (
  `id` bigint NOT NULL COMMENT '实体ID，与entity表的ID一致',
  `consumption_rate` varchar(50) COMMENT '消耗速率',
  `remaining_quantity` double COMMENT '剩余数量',
  `unit` varchar(20) COMMENT '单位',
  `replacement_cycle` int COMMENT '更换周期（天）',
  `last_replacement_date` date COMMENT '上次更换日期',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_consumable_entity` FOREIGN KEY (`id`) REFERENCES `entity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材表';

-- 创建药品表
CREATE TABLE IF NOT EXISTS `medicine_entity` (
  `id` bigint NOT NULL COMMENT '实体ID，与entity表的ID一致',
  `active_ingredient` varchar(255) COMMENT '有效成分',
  `dosage_form` varchar(50) COMMENT '剂型',
  `batch_number` varchar(50) COMMENT '批号',
  `usage_dosage` varchar(255) COMMENT '用法用量',
  `approval_number` varchar(50) COMMENT '批准文号',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_medicine_entity` FOREIGN KEY (`id`) REFERENCES `entity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品表'; 

-- 添加说明书文本和说明书图片URL列表字段
ALTER TABLE `medicine_entity` ADD COLUMN `instruction_text` TEXT COMMENT '说明书内容文本';
ALTER TABLE `medicine_entity` ADD COLUMN `instruction_images` varchar(1000) COMMENT '说明书图片URL列表，以逗号分隔'; 