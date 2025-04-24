package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("medicine_entity")
@Schema(description = "药品信息")
public class MedicineEntity extends Entity {

    /**
     * 有效成分
     */
    @Schema(description = "有效成分")
    private String activeIngredient;

    /**
     * 剂型
     */
    @Schema(description = "剂型，如片剂、胶囊")
    private String dosageForm;

    /**
     * 批号
     */
    @Schema(description = "批号")
    private String batchNumber;

    /**
     * 用法用量
     */
    @Schema(description = "用法用量，如每日2次，每次1片")
    private String usageDosage;

    /**
     * 批准文号
     */
    @Schema(description = "批准文号，如国药准字")
    private String approvalNumber;
    
    /**
     * 说明书文本
     */
    @Schema(description = "说明书内容文本")
    private String instructionText;
    
    /**
     * 说明书图片URL列表，以逗号分隔
     */
    @Schema(description = "说明书图片URL列表，以逗号分隔")
    private String instructionImages;
    
    /**
     * 验证药品是否过期
     */
    public boolean isExpired() {
        return getProductionDate() != null && getWarrantyEndDate() != null && 
               getWarrantyEndDate().isBefore(java.time.LocalDate.now());
    }
    
    /**
     * 获取剩余有效期（天）
     */
    public Long getRemainingDays() {
        if (getWarrantyEndDate() == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), getWarrantyEndDate());
    }
} 