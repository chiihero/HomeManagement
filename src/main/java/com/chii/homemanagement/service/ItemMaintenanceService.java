package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.ItemMaintenance;

import java.util.List;

/**
 * 物品维护记录服务接口
 */
public interface ItemMaintenanceService extends IService<ItemMaintenance> {

    /**
     * 添加维护记录
     *
     * @param maintenance 维护记录
     * @return 添加后的维护记录
     */
    ItemMaintenance addMaintenance(ItemMaintenance maintenance);

    /**
     * 更新维护记录
     *
     * @param maintenance 维护记录
     * @return 更新后的维护记录
     */
    ItemMaintenance updateMaintenance(ItemMaintenance maintenance);

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
    ItemMaintenance getMaintenanceById(Long id);

    /**
     * 获取物品的所有维护记录
     *
     * @param itemId 物品ID
     * @return 维护记录列表
     */
    List<ItemMaintenance> getMaintenancesByItemId(Long itemId);

    /**
     * 获取物品的最近一次维护记录
     *
     * @param itemId 物品ID
     * @return 最近的维护记录
     */
    ItemMaintenance getLatestMaintenance(Long itemId);
} 