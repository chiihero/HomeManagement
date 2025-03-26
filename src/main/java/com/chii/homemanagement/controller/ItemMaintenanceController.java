package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.ItemMaintenance;
import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.service.ItemMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物品维护记录控制器
 */
@RestController
@RequestMapping("/api/item-maintenances")
@Tag(name = "物品维护管理", description = "物品维护记录的增删改查接口")
public class ItemMaintenanceController {

    @Autowired
    private ItemMaintenanceService itemMaintenanceService;

    @GetMapping("/{id}")
    @Operation(summary = "获取维护记录详情", description = "根据ID获取维护记录详情")
    public ResponseInfo<ItemMaintenance> getMaintenanceById(
            @Parameter(description = "维护记录ID") @PathVariable Long id) {

        ItemMaintenance maintenance = itemMaintenanceService.getMaintenanceById(id);
        return ResponseInfo.successResponse(maintenance);
    }

    @GetMapping("/item/{itemId}")
    @Operation(summary = "获取物品维护记录列表", description = "获取指定物品的所有维护记录")
    public ResponseInfo<List<ItemMaintenance>> getMaintenancesByItemId(
            @Parameter(description = "物品ID") @PathVariable Long itemId) {

        List<ItemMaintenance> maintenances = itemMaintenanceService.getMaintenancesByItemId(itemId);
        return ResponseInfo.successResponse(maintenances);
    }

    @GetMapping("/item/{itemId}/latest")
    @Operation(summary = "获取最近一次维护记录", description = "获取指定物品的最近一次维护记录")
    public ResponseInfo<ItemMaintenance> getLatestMaintenance(
            @Parameter(description = "物品ID") @PathVariable Long itemId) {

        ItemMaintenance maintenance = itemMaintenanceService.getLatestMaintenance(itemId);
        return ResponseInfo.successResponse(maintenance);
    }

    @PostMapping
    @Operation(summary = "添加维护记录", description = "添加物品维护记录")
    public ResponseInfo<ItemMaintenance> addMaintenance(@RequestBody ItemMaintenance maintenance) {

        ItemMaintenance addedMaintenance = itemMaintenanceService.addMaintenance(maintenance);
        return ResponseInfo.successResponse(addedMaintenance);
    }

    @PutMapping
    @Operation(summary = "更新维护记录", description = "更新物品维护记录")
    public ResponseInfo<ItemMaintenance> updateMaintenance(@RequestBody ItemMaintenance maintenance) {

        ItemMaintenance updatedMaintenance = itemMaintenanceService.updateMaintenance(maintenance);
        return ResponseInfo.successResponse(updatedMaintenance);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除维护记录", description = "删除指定的物品维护记录")
    public ResponseInfo<Boolean> deleteMaintenance(
            @Parameter(description = "维护记录ID") @PathVariable Long id) {

        boolean result = itemMaintenanceService.deleteMaintenance(id);
        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("删除维护记录失败");
        }
    }
} 