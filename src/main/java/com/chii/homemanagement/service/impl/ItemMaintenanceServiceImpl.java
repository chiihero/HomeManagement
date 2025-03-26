package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.ItemMaintenance;
import com.chii.homemanagement.exception.BusinessException;
import com.chii.homemanagement.mapper.ItemMaintenanceMapper;
import com.chii.homemanagement.service.ItemMaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品维护记录服务实现类
 */
@Service
public class ItemMaintenanceServiceImpl extends ServiceImpl<ItemMaintenanceMapper, ItemMaintenance> implements ItemMaintenanceService {

    @Autowired
    private ItemMaintenanceMapper itemMaintenanceMapper;

    @Override
    @Transactional
    public ItemMaintenance addMaintenance(ItemMaintenance maintenance) {
        maintenance.setCreateTime(LocalDateTime.now());
        save(maintenance);
        return maintenance;
    }

    @Override
    @Transactional
    public ItemMaintenance updateMaintenance(ItemMaintenance maintenance) {
        ItemMaintenance existingMaintenance = getById(maintenance.getId());
        if (existingMaintenance == null) {
            throw new BusinessException("维护记录不存在");
        }

        maintenance.setCreateTime(LocalDateTime.now());
        updateById(maintenance);

        return maintenance;
    }

    @Override
    @Transactional
    public boolean deleteMaintenance(Long id) {
        ItemMaintenance maintenance = getById(id);
        if (maintenance == null) {
            throw new BusinessException("维护记录不存在");
        }

        return removeById(id);
    }

    @Override
    public ItemMaintenance getMaintenanceById(Long id) {
        ItemMaintenance maintenance = getById(id);
        if (maintenance == null) {
            throw new BusinessException("维护记录不存在");
        }

        return maintenance;
    }

    @Override
    public List<ItemMaintenance> getMaintenancesByItemId(Long itemId) {
        LambdaQueryWrapper<ItemMaintenance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ItemMaintenance::getItemId, itemId)
                .orderByDesc(ItemMaintenance::getMaintenanceDate);

        return list(queryWrapper);
    }

    @Override
    public ItemMaintenance getLatestMaintenance(Long itemId) {
        LambdaQueryWrapper<ItemMaintenance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ItemMaintenance::getItemId, itemId)
                .orderByDesc(ItemMaintenance::getMaintenanceDate)
                .last("LIMIT 1");

        return getOne(queryWrapper);
    }
} 