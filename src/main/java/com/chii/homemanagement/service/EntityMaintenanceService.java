package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.EntityMaintenance;

import java.util.List;

/**
 * 实体维护记录服务接口
 */
public interface EntityMaintenanceService extends IService<EntityMaintenance> {

    /**
     * 添加维护记录
     *
     * @param maintenance 维护记录
     * @return 添加后的维护记录
     */
    EntityMaintenance addMaintenance(EntityMaintenance maintenance);

    /**
     * 更新维护记录
     *
     * @param maintenance 维护记录
     * @return 更新后的维护记录
     */
    EntityMaintenance updateMaintenance(EntityMaintenance maintenance);

    /**
     * 删除维护记录
     *
     * @param id 维护记录ID
     * @return 是否删除成功
     */
    boolean deleteMaintenance(Long id);

    /**
     * 获取维护记录详情
     *
     * @param id 维护记录ID
     * @return 维护记录详情
     */
    EntityMaintenance getMaintenanceById(Long id);

    /**
     * 获取实体的所有维护记录
     *
     * @param entityId 实体ID
     * @return 维护记录列表
     */
    List<EntityMaintenance> getMaintenancesByEntityId(Long entityId);

    /**
     * 获取实体的最近一次维护记录
     *
     * @param entityId 实体ID
     * @return 最近的维护记录
     */
    EntityMaintenance getLatestMaintenance(Long entityId);
    
    /**
     * 根据维护类型获取维护记录
     *
     * @param maintenanceType 维护类型
     * @return 维护记录列表
     */
    List<EntityMaintenance> getMaintenancesByType(String maintenanceType);
} 