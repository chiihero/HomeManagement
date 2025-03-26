package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.ItemMaintenance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 物品维修记录Mapper接口
 */
@Mapper
public interface ItemMaintenanceMapper extends BaseMapper<ItemMaintenance> {

    /**
     * 查询指定物品的维修记录
     *
     * @param itemId 物品ID
     * @return 维修记录列表
     */
    List<ItemMaintenance> findByItemId(@Param("itemId") Long itemId);

    /**
     * 查询指定日期范围内的维修记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 维修记录列表
     */
    List<ItemMaintenance> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询指定结果的维修记录
     *
     * @param result 维修结果
     * @return 维修记录列表
     */
    List<ItemMaintenance> findByResult(@Param("result") String result);
} 