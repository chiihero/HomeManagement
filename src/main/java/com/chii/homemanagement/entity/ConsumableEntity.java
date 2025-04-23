package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 耗材实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consumable_entity")
@Schema(description = "耗材信息")
public class ConsumableEntity extends Entity {

    /**
     * 消耗速率
     */
    @Schema(description = "消耗速率，例如：1包/月")
    private String consumptionRate;

    /**
     * 剩余数量
     */
    @Schema(description = "剩余数量")
    private Double remainingQuantity;

    /**
     * 单位
     */
    @Schema(description = "单位，如包、卷、瓶")
    private String unit;

    /**
     * 更换周期（天）
     */
    @Schema(description = "更换周期（天）")
    private Integer replacementCycle;

    /**
     * 上次更换日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "上次更换日期", example = "2023-01-01")
    private LocalDate lastReplacementDate;

    /**
     * 计算下次更换日期
     */
    public LocalDate getNextReplacementDate() {
        if (lastReplacementDate != null && replacementCycle != null && replacementCycle > 0) {
            return lastReplacementDate.plusDays(replacementCycle);
        }
        return null;
    }

    /**
     * 判断是否需要更换
     */
    public boolean needsReplacement() {
        LocalDate nextDate = getNextReplacementDate();
        return nextDate != null && !nextDate.isAfter(LocalDate.now());
    }
} 