package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品实体类
 */
@Data
@TableName("item")
@Schema(description = "物品信息")
public class Item {

    /**
     * 物品ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "物品ID")
    private Long id;

    /**
     * 物品名称
     */
    @NotBlank(message = "物品名称不能为空")
    @Schema(description = "物品名称", required = true)
    private String name;

    /**
     * 规格
     */
    @Schema(description = "规格")
    private String specification;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    @Schema(description = "数量", required = true, minimum = "1")
    private Integer quantity;

    /**
     * 价格
     */
    @Min(value = 0, message = "价格不能为负数")
    @Schema(description = "价格")
    private BigDecimal price;

    /**
     * 购买日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "购买日期", example = "2023-01-01")
    private LocalDate purchaseDate;

    /**
     * 保修期（月）
     */
    @Schema(description = "保修期（月）")
    private Integer warrantyPeriod;

    /**
     * 保修截止日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "保修截止日期", example = "2024-01-01")
    private LocalDate warrantyEndDate;

    /**
     * 使用频率：daily-每天，weekly-每周，monthly-每月，rarely-很少
     */
    @Pattern(regexp = "daily|weekly|monthly|rarely", message = "使用频率只能是每天、每周、每月或很少")
    @Schema(description = "使用频率", allowableValues = {"daily", "weekly", "monthly", "rarely"})
    private String usageFrequency;

    /**
     * 使用年限
     */
    @Schema(description = "使用年限")
    private Integer usageYears;

    /**
     * 使用人ID
     */
    @Schema(description = "使用人ID")
    private Long userId;

    /**
     * 使用人姓名（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "使用人姓名")
    private String userName;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID")
    private Long categoryId;

    /**
     * 分类名称（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 存放空间ID
     */
    @Schema(description = "存放空间ID")
    private Long spaceId;

    /**
     * 存放空间名称（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "存放空间名称")
    private String spaceName;

    /**
     * 状态：normal-正常，damaged-损坏，discarded-丢弃，lent-借出
     */
    @Pattern(regexp = "normal|damaged|discarded|lent", message = "状态只能是正常、损坏、丢弃或借出")
    @Schema(description = "状态", allowableValues = {"normal", "damaged", "discarded", "lent"}, defaultValue = "normal")
    private String status = "normal";

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 图片列表（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "图片列表")
    private List<ItemImage> images;

    /**
     * 所属家庭ID
     */
    @NotNull(message = "家庭ID不能为空")
    @Schema(description = "所属家庭ID", required = true)
    private Long familyId;

    /**
     * 创建者用户ID
     */
    @Schema(description = "创建者用户ID")
    private Long createUserId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}