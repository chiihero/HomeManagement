package com.chii.homemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 空间实体类
 */
@Data
@TableName("space")
public class Space {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间名称
     */
    private String name;

    /**
     * 父空间ID
     */
    private Long parentId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 空间路径，如：1,2,3
     */
    private String path;

    /**
     * 所属家庭ID
     */
    private Long familyId;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建者用户ID
     */
    private Long createUserId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}