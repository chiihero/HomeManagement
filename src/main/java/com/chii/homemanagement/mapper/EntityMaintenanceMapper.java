package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.EntityMaintenance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;

import java.time.LocalDate;
import java.util.List;

/**
 * 实体维护记录Mapper接口
 */
@Mapper
public interface EntityMaintenanceMapper extends BaseMapper<EntityMaintenance> {

    /**
     * 查询指定实体的维护记录
     *
     * @param entityId 实体ID
     * @return 维护记录列表
     */
    @Select("SELECT * FROM entity_maintenance WHERE entity_id = #{entityId} ORDER BY maintenance_date DESC")
    List<EntityMaintenance> findByEntityId(@Param("entityId") Long entityId);

    /**
     * 查询指定日期范围内的维护记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 维护记录列表
     */
    @Select("SELECT * FROM entity_maintenance WHERE maintenance_date >= #{startDate} AND maintenance_date <= #{endDate} ORDER BY maintenance_date DESC")
    List<EntityMaintenance> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询指定结果的维护记录
     *
     * @param result 维护结果
     * @return 维护记录列表
     */
    @Select("SELECT * FROM entity_maintenance WHERE result = #{result} ORDER BY maintenance_date DESC")
    List<EntityMaintenance> findByResult(@Param("result") String result);
    
    /**
     * 查询指定维护类型的记录
     *
     * @param maintenanceType 维护类型
     * @return 维护记录列表
     */
    @Select("SELECT * FROM entity_maintenance WHERE maintenance_type = #{maintenanceType} ORDER BY maintenance_date DESC")
    List<EntityMaintenance> findByMaintenanceType(@Param("maintenanceType") String maintenanceType);
} 