package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.EntityMaintenance;
import com.chii.homemanagement.exception.BusinessException;
import com.chii.homemanagement.mapper.EntityMaintenanceMapper;
import com.chii.homemanagement.service.EntityMaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实体维护记录服务实现类
 */
@Service
public class EntityMaintenanceServiceImpl extends ServiceImpl<EntityMaintenanceMapper, EntityMaintenance> implements EntityMaintenanceService {

    @Autowired
    private EntityMaintenanceMapper entityMaintenanceMapper;

    @Override
    @Transactional
    public EntityMaintenance addMaintenance(EntityMaintenance maintenance) {
        maintenance.setCreateTime(LocalDateTime.now());
        save(maintenance);
        return maintenance;
    }

    @Override
    @Transactional
    public EntityMaintenance updateMaintenance(EntityMaintenance maintenance) {
        EntityMaintenance existingMaintenance = getById(maintenance.getId());
        if (existingMaintenance == null) {
            throw new BusinessException(6002, "维护记录不存在");
        }

        updateById(maintenance);
        return maintenance;
    }

    @Override
    @Transactional
    public boolean deleteMaintenance(Long id) {
        EntityMaintenance maintenance = getById(id);
        if (maintenance == null) {
            throw new BusinessException(6002, "维护记录不存在");
        }

        return removeById(id);
    }

    @Override
    public EntityMaintenance getMaintenanceById(Long id) {
        EntityMaintenance maintenance = getById(id);
        if (maintenance == null) {
            throw new BusinessException(6002, "维护记录不存在");
        }

        return maintenance;
    }

    @Override
    public List<EntityMaintenance> getMaintenancesByEntityId(Long entityId) {
        LambdaQueryWrapper<EntityMaintenance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMaintenance::getEntityId, entityId)
                .orderByDesc(EntityMaintenance::getMaintenanceDate);

        return list(queryWrapper);
    }

    @Override
    public EntityMaintenance getLatestMaintenance(Long entityId) {
        LambdaQueryWrapper<EntityMaintenance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMaintenance::getEntityId, entityId)
                .orderByDesc(EntityMaintenance::getMaintenanceDate)
                .last("LIMIT 1");

        return getOne(queryWrapper);
    }
    
    @Override
    public List<EntityMaintenance> getMaintenancesByType(String maintenanceType) {
        LambdaQueryWrapper<EntityMaintenance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMaintenance::getMaintenanceType, maintenanceType)
                .orderByDesc(EntityMaintenance::getMaintenanceDate);
                
        return list(queryWrapper);
    }
} 