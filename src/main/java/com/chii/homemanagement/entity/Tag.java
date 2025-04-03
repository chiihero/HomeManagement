package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
@TableName("tag")
@Schema(description = "标签信息")
public class Tag {

    @TableId(type = IdType.AUTO)
    @Schema(description = "标签ID")
    private Long id;

    /**
     * 标签名称
     */
    @Schema(description = "标签名称", required = true)
    private String name;

    /**
     * 标签颜色
     */
    @Schema(description = "标签颜色，如#FF0000")
    private String color;

    /**
     * 所属用户ID
     */
    @Schema(description = "所属用户ID", required = true)
    private Long userId;

    /**
     * 创建者用户ID
     */
    @Schema(description = "创建者用户ID")
    private Long createUserId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 