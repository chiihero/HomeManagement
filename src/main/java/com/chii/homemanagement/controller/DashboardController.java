package com.chii.homemanagement.controller;

import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Reminder;
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
import java.time.OffsetDateTime;
import java.time.OffsetDateTime;
import java.time.LocalDate;
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
            OffsetDateTime now = OffsetDateTime.now();
            long expiring = 0;
            long expired = 0;

            for (Entity item : allItems) {
                LocalDate warrantyEnd = item.getWarrantyEndDate(); // 使用 warrantyEndDate
                if (warrantyEnd != null) {
                    // 将 LocalDate 转换为 OffsetDateTime (假设在当天开始时过期，使用当前偏移量)
                    OffsetDateTime expirationDateTime = warrantyEnd.atStartOfDay(now.getOffset()).toOffsetDateTime();
                    if (expirationDateTime.isAfter(now) && expirationDateTime.isBefore(now.plusDays(30))) {
                        expiring++; // 30天内到期
                    } else if (expirationDateTime.isBefore(now)) {
                        expired++; // 已过期
                    }
                }
            }

            statistics.put("expiringItems", expiring);
            statistics.put("expiredItems", expired);
            
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
            
            // 分类数据 (重命名为 categoryDistribution 并添加颜色)
            List<Map<String, Object>> categoryDistribution = new ArrayList<>();
            Map<String, Long> typeCount = allItems.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getType() != null ? e.getType() : "其他", // 假设 Type 是分类
                            Collectors.counting()
                    ));

            // 预定义一些颜色或使用颜色生成逻辑
            String[] categoryColors = {"#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399", "#FFD700", "#8A2BE2"};
            int categoryColorIndex = 0;
            for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
                Map<String, Object> category = new HashMap<>();
                category.put("name", entry.getKey());
                category.put("count", entry.getValue());
                category.put("color", categoryColors[categoryColorIndex % categoryColors.length]);
                categoryDistribution.add(category);
                categoryColorIndex++;
            }
            // 状态数据 (重命名为 statusDistribution 并添加颜色)
            List<Map<String, Object>> statusDistribution = new ArrayList<>();
            Map<String, Long> statusCount = allItems.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getStatus() != null ? e.getStatus() : "未知",
                            Collectors.counting()
                    ));

            // 预定义状态颜色
            Map<String, String> statusColors = new HashMap<>();
            statusColors.put("normal", "#67C23A"); // 正常 - 绿色
            statusColors.put("damaged", "#E6A23C"); // 损坏 - 橙色
            statusColors.put("discarded", "#909399"); // 丢弃 - 灰色
            statusColors.put("expired", "#F56C6C"); // 过期 - 红色
            statusColors.put("lent", "#409EFF"); // 借出 - 蓝色
            statusColors.put("未知", "#C0C4CC"); // 未知 - 浅灰色

            for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
                Map<String, Object> status = new HashMap<>();
                status.put("name", entry.getKey()); // 前端期望的是 name
                status.put("count", entry.getValue());
                status.put("color", statusColors.getOrDefault(entry.getKey(), "#C0C4CC")); // 获取颜色，默认为浅灰色
                statusDistribution.add(status);
            }
            result.put("statistics", statistics);
            // 使用新的键名
            result.put("categoryDistribution", categoryDistribution);
            result.put("statusDistribution", statusDistribution);
            
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
    public ApiResponse<List<Reminder>> getRecentReminders(
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam Long userId) {
        try {
            // 这里需要实现获取最近提醒的逻辑
            List<Reminder> recentReminders = reminderService.getRecentReminders(userId, limit);
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