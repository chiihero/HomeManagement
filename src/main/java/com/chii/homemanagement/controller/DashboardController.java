package com.chii.homemanagement.controller;

import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Tag;
import com.chii.homemanagement.service.EntityService;
import com.chii.homemanagement.service.ReminderService;
import com.chii.homemanagement.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘API控制器
 */
@RestController
@RequestMapping("/api/dashboard")
@io.swagger.v3.oas.annotations.tags.Tag(name = "仪表盘管理", description = "仪表盘数据接口")
@Slf4j
@RequiredArgsConstructor
public class DashboardController {

    private final EntityService entityService;
    private final ReminderService reminderService;
    private final TagService tagService;

    /**
     * 获取仪表盘统计数据
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取仪表盘统计数据", description = "获取仪表盘展示所需的统计数据")
    public ApiResponse<Map<String, Object>> getDashboardStatistics(@RequestParam Long userId) {
        try {
            log.info("获取仪表盘统计数据: userId={}", userId);
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取所有类型为item的实体
            List<Entity> allItems = entityService.getEntitiesByType(userId, "item");
            
            // 计算基本统计数据
            statistics.put("totalItems", allItems.size());
            statistics.put("availableItems", allItems.stream()
                    .filter(e -> "normal".equals(e.getStatus()))
                    .count());
            statistics.put("expiringItems", 0); // 需要实现即将过期的逻辑
            statistics.put("expiredItems", 0); // 需要实现已过期的逻辑
            
            // 计算总价值
            BigDecimal totalValue = allItems.stream()
                    .filter(e -> e.getPrice() != null && e.getQuantity() != null)
                    .map(e -> e.getPrice().multiply(new BigDecimal(e.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.put("totalValue", totalValue);
            
            // 分类数量
            statistics.put("categoriesCount", allItems.stream()
                    .map(Entity::getType)
                    .distinct()
                    .count());
            
            // 分类数据
            List<Map<String, Object>> categoryData = new ArrayList<>();
            Map<String, Long> typeCount = allItems.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getType() != null ? e.getType() : "其他",
                            Collectors.counting()
                    ));
            
            for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
                Map<String, Object> category = new HashMap<>();
                category.put("name", entry.getKey());
                category.put("count", entry.getValue());
                categoryData.add(category);
            }
            
            // 状态数据
            List<Map<String, Object>> statusData = new ArrayList<>();
            Map<String, Long> statusCount = allItems.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getStatus() != null ? e.getStatus() : "未知",
                            Collectors.counting()
                    ));
            
            for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
                Map<String, Object> status = new HashMap<>();
                status.put("status", entry.getKey());
                status.put("count", entry.getValue());
                statusData.add(status);
            }
            
            result.put("statistics", statistics);
            result.put("categoryData", categoryData);
            result.put("statusData", statusData);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据异常", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近添加的实体
     *
     * @param limit 数量限制
     * @return 实体列表
     */
    @GetMapping("/recent-entities")
    @Operation(summary = "获取最近添加的实体", description = "获取最近添加的实体数据")
    public ApiResponse<List<Entity>> getRecentEntities(
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam Long userId) {
        try {
            // 这里需要实现获取最近添加实体的逻辑
            List<Entity> recentEntities = entityService.getRecentEntities(userId, limit);
            return ApiResponse.success(recentEntities);
        } catch (Exception e) {
            log.error("获取最近添加的实体异常", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近的提醒
     *
     * @param limit 数量限制
     * @return 提醒列表
     */
    @GetMapping("/recent-reminders")
    @Operation(summary = "获取最近的提醒", description = "获取最近的提醒数据")
    public ApiResponse<List<Object>> getRecentReminders(
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam Long userId) {
        try {
            // 这里需要实现获取最近提醒的逻辑
            List<Object> recentReminders = reminderService.getRecentReminders(userId, limit);
            return ApiResponse.success(recentReminders);
        } catch (Exception e) {
            log.error("获取最近的提醒异常", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/stat/by-parent")
    @Operation(summary = "根据父实体统计子实体", description = "根据父实体统计子实体数量和价值")
    public ApiResponse<List<Object>> statEntitiesByParent(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {

        try {
            log.info("根据父实体统计子实体: userId={}", userId);
            List<Object> stats = entityService.statEntitiesByParent(userId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("根据父实体统计子实体异常: userId={}", userId, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/stat/by-tag")
    @Operation(summary = "根据标签统计物品", description = "根据标签统计物品数量和价值")
    public ApiResponse<List<Object>> statEntitiesByTag(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {

        try {
            log.info("根据标签统计物品: userId={}", userId);
            List<Object> stats = entityService.statEntitiesByTag(userId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("根据标签统计物品异常: userId={}", userId, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/stat/by-usage-frequency")
    @Operation(summary = "根据使用频率统计物品", description = "根据使用频率统计物品数量")
    public ApiResponse<List<Object>> statEntitiesByUsageFrequency(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {

        try {
            log.info("根据使用频率统计物品: userId={}", userId);
            List<Object> stats = entityService.statEntitiesByUsageFrequency(userId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("根据使用频率统计物品异常: userId={}", userId, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/sum-value")
    @Operation(summary = "统计物品总价值", description = "统计所有者物品总价值")
    public ApiResponse<Double> sumEntitiesValue(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {

        try {
            log.info("统计物品总价值: userId={}", userId);
            double totalValue = entityService.sumEntitiesValue(userId);
            return ApiResponse.success(totalValue);
        } catch (Exception e) {
            log.error("统计物品总价值异常: userId={}", userId, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "统计失败: " + e.getMessage());
        }
    }

} 