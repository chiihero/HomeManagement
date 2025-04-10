package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体标签关联表
 */
@Data
@TableName("entity_tag")
@Schema(description = "实体标签关联信息")
public class EntityTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "实体ID", required = true)
    private Long entityId;

    @Schema(description = "标签ID", required = true)
    private Long tagId;

    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-01-01 12:00:00")
    private LocalDateTime updateTime;
}