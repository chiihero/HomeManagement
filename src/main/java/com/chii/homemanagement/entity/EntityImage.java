package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体图片类
 */
@Data
@TableName("entity_image")
@Schema(description = "实体图片信息")
public class EntityImage {

    /**
     * 图片ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "图片ID")
    private Long id;

    /**
     * 实体ID
     */
    @Schema(description = "实体ID")
    private Long entityId;

    /**
     * 图片URL
     */
    @Schema(description = "图片URL")
    private String imageUrl;
    
    /**
     * 图片二进制数据
     */
    @JsonIgnore
    @Schema(description = "图片二进制数据")
    private byte[] imageData;

    /**
     * 图片类型
     */
    @Schema(description = "图片类型", allowableValues = {"normal", "receipt", "warranty"})
    private String imageType;
    
    /**
     * 内容类型
     */
    @Schema(description = "内容类型")
    private String contentType;
    
    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名")
    private String fileName;
    
    /**
     * 文件大小
     */
    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
} 