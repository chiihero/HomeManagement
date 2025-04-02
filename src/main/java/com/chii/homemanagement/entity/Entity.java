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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 实体类（统一表示物品和空间）
 */
@Data
@TableName("entity")
@Schema(description = "实体信息（物品/空间）")
public class Entity {

    /**
     * 实体ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "实体ID")
    private Long id;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称", required = true)
    private String name;

    /**
     * 类型：item-物品, space-空间
     */
    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "item|space", message = "类型只能是物品或空间")
    @Schema(description = "类型", allowableValues = {"item", "space"}, defaultValue = "item")
    private String type = "item";

    /**
     * 编码
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 规格
     */
    @Schema(description = "规格")
    private String specification;

    /**
     * 数量（物品专用）
     */
    @Min(value = 1, message = "数量必须大于0")
    @Schema(description = "数量", minimum = "1")
    private Integer quantity;

    /**
     * 价格（物品专用）
     */
    @Min(value = 0, message = "价格不能为负数")
    @Schema(description = "价格")
    private BigDecimal price;

    /**
     * 购买日期（物品专用）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "购买日期", example = "2023-01-01")
    private LocalDate purchaseDate;

    /**
     * 保修期（月）（物品专用）
     */
    @Schema(description = "保修期（月）")
    private Integer warrantyPeriod;

    /**
     * 保修截止日期（物品专用）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "保修截止日期", example = "2024-01-01")
    private LocalDate warrantyEndDate;

    /**
     * 使用频率（物品专用）：daily-每天，weekly-每周，monthly-每月，rarely-很少
     */
    @Pattern(regexp = "daily|weekly|monthly|rarely", message = "使用频率只能是每天、每周、每月或很少")
    @Schema(description = "使用频率", allowableValues = {"daily", "weekly", "monthly", "rarely"})
    private String usageFrequency;

    /**
     * 使用年限（物品专用）
     */
    @Schema(description = "使用年限")
    private Integer usageYears;

    /**
     * 使用人ID（物品专用）
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
     * 父实体ID（物品/空间存放在哪个实体中）
     */
    @Schema(description = "父实体ID")
    private Long parentId;

    /**
     * 父实体名称（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "父实体名称")
    private String parentName;

    /**
     * 层级（空间专用）
     */
    @Schema(description = "层级")
    private Integer level;

    /**
     * 路径（空间专用）
     */
    @Schema(description = "路径，例如: 1,2,3")
    private String path;

    /**
     * 排序（空间专用）
     */
    @Schema(description = "排序")
    private Integer sort;
    
    /**
     * 子实体列表（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "子实体列表")
    private List<Entity> children;

    /**
     * 状态（物品专用）：normal-正常，damaged-损坏，discarded-丢弃
     */
    @Pattern(regexp = "normal|damaged|discarded", message = "状态只能是正常、损坏或丢弃")
    @Schema(description = "状态", allowableValues = {"normal", "damaged", "discarded"}, defaultValue = "normal")
    private String status = "normal";

    /**
     * 描述/备注
     */
    @Schema(description = "描述/备注")
    private String description;

    /**
     * 图片列表（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "图片列表")
    private List<EntityImage> images;

    /**
     * 标签列表（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "标签列表")
    private List<Tag> tags;

    /**
     * 标签ID列表（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    /**
     * 物品所有者ID
     */
    @NotNull(message = "所有者ID不能为空")
    @Schema(description = "所有者ID", required = true)
    private Long ownerId;

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
    
    /**
     * 判断是否是物品
     */
    public boolean isItem() {
        return "item".equals(this.type);
    }
    
    /**
     * 判断是否是空间
     */
    public boolean isSpace() {
        return "space".equals(this.type);
    }

    /**
     * 计算保修截止日期
     * 如果购买日期和保修期都有值，则计算保修截止日期并设置
     */
    public void calculateWarrantyEndDate() {
        if (this.purchaseDate != null && this.warrantyPeriod != null && this.warrantyPeriod > 0) {
            this.warrantyEndDate = this.purchaseDate.plusMonths(this.warrantyPeriod);
        } else {
            this.warrantyEndDate = null;
        }
    }

    /**
     * 判断是否已过保
     * @return 如果已经过了保修截止日期或没有保修期，则返回true；否则返回false
     */
    public boolean isWarrantyExpired() {
        if (this.warrantyEndDate == null) {
            return true; // 没有保修期视为已过保
        }
        return LocalDate.now().isAfter(this.warrantyEndDate);
    }

    /**
     * 获取剩余保修天数
     * @return 剩余的保修天数，如果已过保或没有保修期则返回0
     */
    public long getRemainingWarrantyDays() {
        if (this.warrantyEndDate == null || isWarrantyExpired()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), this.warrantyEndDate);
    }

    /**
     * 获取实体的总价值
     * @return 数量乘以价格的总价值，如果价格为空则返回0
     */
    public BigDecimal getTotalValue() {
        if (this.price == null) {
            return BigDecimal.ZERO;
        }
        int qty = (this.quantity == null || this.quantity < 1) ? 1 : this.quantity;
        return this.price.multiply(new BigDecimal(qty));
    }

    /**
     * 获取标签名称列表
     * @return 标签名称列表，用逗号分隔
     */
    public String getTagNames() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.stream()
                .map(Tag::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    /**
     * 获取标签ID列表
     * @return 标签ID列表
     */
    public List<Long> getTagIds() {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(Tag::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 检查是否有特定标签
     * @param tagId 标签ID
     * @return 是否含有该标签
     */
    public boolean hasTag(Long tagId) {
        if (tags == null || tags.isEmpty() || tagId == null) {
            return false;
        }
        return tags.stream().anyMatch(tag -> tagId.equals(tag.getId()));
    }

    /**
     * 格式化为简短字符串，用于日志输出
     */
    @Override
    public String toString() {
        return String.format("Entity{id=%d, name='%s', type='%s', status='%s'}", 
                Optional.ofNullable(id).orElse(-1L),
                name,
                type,
                status);
    }
} 