package com.chii.homemanagement.controller;

import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
import com.chii.homemanagement.entity.EntityMaintenance;
import com.chii.homemanagement.service.EntityMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实体维护记录控制器
 */
@RestController
@RequestMapping("/api/entity-maintenance")
@Tag(name = "实体维护记录", description = "实体维护记录相关接口")
public class EntityMaintenanceController {

    @Autowired
    private EntityMaintenanceService entityMaintenanceService;

    /**
     * 添加维护记录
     */
    @PostMapping
    @Operation(summary = "添加维护记录", description = "添加实体维护记录")
    public ApiResponse<EntityMaintenance> addMaintenance(@RequestBody EntityMaintenance maintenance) {
        EntityMaintenance result = entityMaintenanceService.addMaintenance(maintenance);
        return ApiResponse.success(result);
    }

    /**
     * 更新维护记录
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新维护记录", description = "更新实体维护记录")
    public ApiResponse<EntityMaintenance> updateMaintenance(@PathVariable(value = "id") Long id, @RequestBody EntityMaintenance maintenance) {
        maintenance.setId(id);
        EntityMaintenance result = entityMaintenanceService.updateMaintenance(maintenance);
        return ApiResponse.success(result);
    }

    /**
     * 删除维护记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除维护记录", description = "删除实体维护记录")
    public ApiResponse<Boolean> deleteMaintenance(@PathVariable(value = "id") Long id) {
        boolean result = entityMaintenanceService.deleteMaintenance(id);
        return ApiResponse.success(result);
    }

    /**
     * 获取维护记录详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取维护记录详情", description = "获取实体维护记录详情")
    public ApiResponse<EntityMaintenance> getMaintenanceById(@PathVariable(value = "id") Long id) {
        EntityMaintenance maintenance = entityMaintenanceService.getMaintenanceById(id);
        return ApiResponse.success(maintenance);
    }

    /**
     * 获取实体的所有维护记录
     */
    @GetMapping("/entity/{entityId}")
    @Operation(summary = "获取实体的所有维护记录", description = "获取指定实体的所有维护记录")
    public ApiResponse<List<EntityMaintenance>> getMaintenancesByEntityId(@PathVariable(value = "entityId") Long entityId) {
        List<EntityMaintenance> maintenances = entityMaintenanceService.getMaintenancesByEntityId(entityId);
        return ApiResponse.success(maintenances);
    }

    /**
     * 获取实体的最近一次维护记录
     */
    @GetMapping("/entity/{entityId}/latest")
    @Operation(summary = "获取最近一次维护记录", description = "获取实体的最近一次维护记录")
    public ApiResponse<EntityMaintenance> getLatestMaintenance(@PathVariable(value = "entityId") Long entityId) {
        EntityMaintenance maintenance = entityMaintenanceService.getLatestMaintenance(entityId);
        return ApiResponse.success(maintenance);
    }
    
    /**
     * 根据维护类型获取维护记录
     */
    @GetMapping("/type/{maintenanceType}")
    @Operation(summary = "根据维护类型获取维护记录", description = "根据维护类型获取维护记录")
    public ApiResponse<List<EntityMaintenance>> getMaintenancesByType(@PathVariable(value = "maintenanceType") String maintenanceType) {
        List<EntityMaintenance> maintenances = entityMaintenanceService.getMaintenancesByType(maintenanceType);
        return ApiResponse.success(maintenances);
    }
} 