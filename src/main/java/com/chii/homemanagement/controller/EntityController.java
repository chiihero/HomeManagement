package com.chii.homemanagement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chii.homemanagement.entity.*;
import com.chii.homemanagement.service.EntityService;
import com.chii.homemanagement.service.EntityTagService;
import com.chii.homemanagement.service.EntityImageService;
import com.chii.homemanagement.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 实体管理控制器
 */
@RestController
@RequestMapping("/api/entities")
@io.swagger.v3.oas.annotations.tags.Tag(name = "实体管理", description = "实体（物品/空间）的增删改查接口")
@Slf4j
@RequiredArgsConstructor
public class EntityController {

    private final EntityService entityService;
    private final EntityTagService entityTagService;
    private final EntityImageService entityImageService;
    private final FileStorageService fileStorageService;

    @GetMapping("/page")
    @Operation(summary = "分页查询实体列表", description = "根据条件分页查询实体列表")
    public ResponseInfo<IPage<Entity>> pageEntities(
            @Parameter(description = "当前页码") @RequestParam(value = "current", defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "名称") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "类型: item-物品, space-空间") @RequestParam(value = "type", required = false) String type,
            @Parameter(description = "规格") @RequestParam(value = "specification", required = false) String specification,
            @Parameter(description = "状态") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "使用频率") @RequestParam(value = "usageFrequency", required = false) String usageFrequency,
            @Parameter(description = "使用人ID") @RequestParam(value = "userId") Long userId,
            @Parameter(description = "父实体ID") @RequestParam(value = "parentId", required = false) Long parentId) {

        try {
            log.info("分页查询实体列表: userId={}, current={}, size={}", userId, current, size);
            
            // 构建分页对象
            Page<Entity> page = new Page<>(current, size);

            // 构建查询条件
            Entity entity = new Entity();
            entity.setName(name);
            entity.setType(type);
            entity.setSpecification(specification);
            entity.setStatus(status);
            entity.setUsageFrequency(usageFrequency);
            entity.setUserId(userId);
            entity.setParentId(parentId);

            // 调用服务层方法
            IPage<Entity> result = entityService.pageEntities(page, entity, userId);

            return ResponseInfo.successResponse(result);
        } catch (Exception e) {
            log.error("分页查询实体列表异常: ", e);
            return ResponseInfo.errorResponse("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询实体详情", description = "根据ID查询实体详情")
    public ResponseInfo<Entity> getEntityDetail(
            @Parameter(description = "实体ID") @PathVariable(value = "id") Long id) {

        try {
            log.info("查询实体详情: id={}", id);
            Entity entity = entityService.getEntityDetail(id);
            
            if (entity == null) {
                return ResponseInfo.errorResponse("未找到实体");
            }
            
            return ResponseInfo.successResponse(entity);
        } catch (Exception e) {
            log.error("查询实体详情异常: id={}", id, e);
            return ResponseInfo.errorResponse("查询实体详情失败: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "新增实体", description = "新增实体信息，图片上传请使用 EntityImageController 的上传接口")
    public ResponseInfo<Entity> addEntity(
            @RequestBody Entity entity) {
        try {
            log.info("新增实体: {}", entity.getName());
            
            boolean result = entityService.addEntity(entity);
            
            // 如果有标签ID，则设置标签关联
            if (result && entity.getId() != null && entity.getTagIds() != null && !entity.getTagIds().isEmpty()) {
                try {
                    // 设置实体的标签关联
                    entityTagService.addTagsToEntity(entity.getId(), entity.getTagIds());
                } catch (Exception e) {
                    log.error("处理标签时出错: ", e);
                    // 不阻止实体添加成功，仅记录错误
                }
            }
            
            if (result) {
                return ResponseInfo.successResponse(entity);
            } else {
                return ResponseInfo.errorResponse("新增实体失败");
            }
        } catch (Exception e) {
            log.error("新增实体异常: ", e);
            return ResponseInfo.errorResponse("新增实体失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新实体", description = "更新实体信息，图片上传请使用 EntityImageController 的上传接口")
    public ResponseInfo<Boolean> updateEntity(
            @Parameter(description = "实体ID") @PathVariable(value = "id") Long id,
            @RequestBody Entity entity) {
        
        try {
            log.info("更新实体: id={}, name={}", id, entity.getName());
            entity.setId(id);
            
            boolean result = entityService.updateEntity(entity);
            
            // 处理标签
            if (entity.getTagIds() != null) {
                // 更新实体的标签关联
                entityTagService.updateEntityTags(id, entity.getTagIds());
            }
            
            if (result) {
                return ResponseInfo.successResponse(true);
            } else {
                return ResponseInfo.errorResponse("更新实体失败");
            }
        } catch (Exception e) {
            log.error("更新实体异常: id={}", id, e);
            return ResponseInfo.errorResponse("更新实体失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除实体", description = "根据ID删除实体")
    public ResponseInfo<Boolean> deleteEntity(
            @Parameter(description = "实体ID") @PathVariable(value = "id") Long id) {
        
        try {
            log.info("删除实体: id={}", id);
            
            // 先删除实体的标签关联
            entityTagService.removeEntityTags(id);
            
            boolean result = entityService.deleteEntity(id);
            
            if (result) {
                return ResponseInfo.successResponse(true);
            } else {
                return ResponseInfo.errorResponse("删除实体失败");
            }
        } catch (Exception e) {
            log.error("删除实体异常: id={}", id, e);
            return ResponseInfo.errorResponse("删除实体失败: " + e.getMessage());
        }
    }

    @GetMapping("/tree")
    @Operation(summary = "获取实体树", description = "获取所有者下的实体树结构")
    public ResponseInfo<List<Entity>> getEntityTree(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {

        try {
            log.info("获取实体树: userId={}", userId);
            List<Entity> tree = entityService.getEntityTree(userId);
            log.info("获取到实体树，根节点数量: {}, 总节点数量: {}", 
                    tree.size(), 
                    countTotalNodes(tree));
            return ResponseInfo.successResponse(tree);
        } catch (Exception e) {
            log.error("获取实体树异常: userId={}", userId, e);
            return ResponseInfo.errorResponse("获取实体树失败: " + e.getMessage());
        }
    }

    /**
     * 计算树中的总节点数
     */
    private int countTotalNodes(List<Entity> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }
        
        int count = nodes.size();
        for (Entity node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                count += countTotalNodes(node.getChildren());
            }
        }
        return count;
    }


    @GetMapping("/list/by-parent")
    @Operation(summary = "获取子实体列表", description = "获取指定父实体下的子实体列表")
    public ResponseInfo<List<Entity>> listChildEntities(
            @Parameter(description = "父实体ID") @RequestParam(value = "parentId") Long parentId,
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("获取子实体列表: parentId={}, userId={}", parentId, userId);
            List<Entity> entities = entityService.listChildEntities(parentId, userId);
            return ResponseInfo.successResponse(entities);
        } catch (Exception e) {
            log.error("获取子实体列表异常: parentId={}, userId={}", parentId, userId, e);
            return ResponseInfo.errorResponse("获取子实体列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/by-user")
    @Operation(summary = "获取用户使用的物品列表", description = "获取指定用户使用的物品列表")
    public ResponseInfo<List<Entity>> listEntitiesByUser(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("获取用户使用的物品列表: userId={}", userId);
            List<Entity> entities = entityService.listEntitiesByUser(userId);
            return ResponseInfo.successResponse(entities);
        } catch (Exception e) {
            log.error("获取用户使用的物品列表异常: userId={}", userId, e);
            return ResponseInfo.errorResponse("获取用户物品列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/by-status")
    @Operation(summary = "根据状态获取物品列表", description = "获取指定状态的物品列表")
    public ResponseInfo<List<Entity>> listEntitiesByStatus(
            @Parameter(description = "状态: normal-正常, damaged-损坏, discarded-丢弃, lent-借出") @RequestParam(value = "status") String status,
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("根据状态获取物品列表: status={}, userId={}", status, userId);
            List<Entity> entities = entityService.listEntitiesByStatus(status, userId);
            return ResponseInfo.successResponse(entities);
        } catch (Exception e) {
            log.error("根据状态获取物品列表异常: status={}, userId={}", status, userId, e);
            return ResponseInfo.errorResponse("获取物品列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/expiring")
    @Operation(summary = "获取即将过保的物品列表", description = "获取未来指定天数内即将过保的物品列表")
    public ResponseInfo<List<Entity>> listExpiringEntities(
            @Parameter(description = "天数") @RequestParam(value = "days", defaultValue = "30") Integer days,
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("获取即将过保的物品列表: days={}, userId={}", days, userId);
            List<Entity> entities = entityService.listExpiringEntities(days, userId);
            return ResponseInfo.successResponse(entities);
        } catch (Exception e) {
            log.error("获取即将过保的物品列表异常: days={}, userId={}", days, userId, e);
            return ResponseInfo.errorResponse("获取即将过保物品列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/expired")
    @Operation(summary = "获取已过保的物品列表", description = "获取已过保的物品列表")
    public ResponseInfo<List<Entity>> listExpiredEntities(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("获取已过保的物品列表: userId={}", userId);
            List<Entity> entities = entityService.listExpiredEntities(userId);
            return ResponseInfo.successResponse(entities);
        } catch (Exception e) {
            log.error("获取已过保的物品列表异常: userId={}", userId, e);
            return ResponseInfo.errorResponse("获取已过保物品列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/by-tag")
    @Operation(summary = "根据标签获取实体列表", description = "获取具有指定标签的实体列表")
    public ResponseInfo<List<Entity>> listEntitiesByTag(
            @Parameter(description = "标签ID") @RequestParam(value = "tagId") Long tagId,
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("根据标签获取实体列表: tagId={}, userId={}", tagId, userId);
            List<Entity> entities = entityService.listEntitiesByTag(tagId, userId);
            return ResponseInfo.successResponse(entities);
        } catch (Exception e) {
            log.error("根据标签获取实体列表异常: tagId={}, userId={}", tagId, userId, e);
            return ResponseInfo.errorResponse("获取实体列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/by-date-range")
    @Operation(summary = "根据购买日期范围获取物品列表", description = "获取指定购买日期范围内的物品列表")
    public ResponseInfo<List<Entity>> listEntitiesByPurchaseDateRange(
            @Parameter(description = "开始日期") @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("根据购买日期范围获取物品列表: startDate={}, endDate={}, userId={}", startDate, endDate, userId);
            List<Entity> entities = entityService.listEntitiesByPurchaseDateRange(startDate, endDate, userId);
            return ResponseInfo.successResponse(entities);
        } catch (Exception e) {
            log.error("根据购买日期范围获取物品列表异常: startDate={}, endDate={}, userId={}", startDate, endDate, userId, e);
            return ResponseInfo.errorResponse("获取物品列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/by-price-range")
    @Operation(summary = "根据价格范围获取物品列表", description = "获取指定价格范围内的物品列表")
    public ResponseInfo<List<Entity>> listEntitiesByPriceRange(
            @Parameter(description = "最小价格") @RequestParam(value = "minPrice", required = false) Double minPrice,
            @Parameter(description = "最大价格") @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("根据价格范围获取物品列表: minPrice={}, maxPrice={}, userId={}", minPrice, maxPrice, userId);
            List<Entity> entities = entityService.listEntitiesByPriceRange(minPrice, maxPrice, userId);
            return ResponseInfo.successResponse(entities);
        } catch (Exception e) {
            log.error("根据价格范围获取物品列表异常: minPrice={}, maxPrice={}, userId={}", minPrice, maxPrice, userId, e);
            return ResponseInfo.errorResponse("获取物品列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/stat/by-parent")
    @Operation(summary = "根据父实体统计子实体", description = "根据父实体统计子实体数量和价值")
    public ResponseInfo<List<Object>> statEntitiesByParent(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("根据父实体统计子实体: userId={}", userId);
            List<Object> stats = entityService.statEntitiesByParent(userId);
            return ResponseInfo.successResponse(stats);
        } catch (Exception e) {
            log.error("根据父实体统计子实体异常: userId={}", userId, e);
            return ResponseInfo.errorResponse("统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/stat/by-tag")
    @Operation(summary = "根据标签统计物品", description = "根据标签统计物品数量和价值")
    public ResponseInfo<List<Object>> statEntitiesByTag(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("根据标签统计物品: userId={}", userId);
            List<Object> stats = entityService.statEntitiesByTag(userId);
            return ResponseInfo.successResponse(stats);
        } catch (Exception e) {
            log.error("根据标签统计物品异常: userId={}", userId, e);
            return ResponseInfo.errorResponse("统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/stat/by-usage-frequency")
    @Operation(summary = "根据使用频率统计物品", description = "根据使用频率统计物品数量")
    public ResponseInfo<List<Object>> statEntitiesByUsageFrequency(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("根据使用频率统计物品: userId={}", userId);
            List<Object> stats = entityService.statEntitiesByUsageFrequency(userId);
            return ResponseInfo.successResponse(stats);
        } catch (Exception e) {
            log.error("根据使用频率统计物品异常: userId={}", userId, e);
            return ResponseInfo.errorResponse("统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/sum-value")
    @Operation(summary = "统计物品总价值", description = "统计所有者物品总价值")
    public ResponseInfo<Double> sumEntitiesValue(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("统计物品总价值: userId={}", userId);
            double totalValue = entityService.sumEntitiesValue(userId);
            return ResponseInfo.successResponse(totalValue);
        } catch (Exception e) {
            log.error("统计物品总价值异常: userId={}", userId, e);
            return ResponseInfo.errorResponse("统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/tags")
    @Operation(summary = "获取实体的标签", description = "获取实体的标签列表")
    public ResponseInfo<List<Tag>> getEntityTags(
            @Parameter(description = "实体ID") @PathVariable(value = "id") Long id) {
        
        try {
            log.info("获取实体的标签: id={}", id);
            List<Tag> tags = entityTagService.getTagsByEntityId(id);
            return ResponseInfo.successResponse(tags);
        } catch (Exception e) {
            log.error("获取实体的标签异常: id={}", id, e);
            return ResponseInfo.errorResponse("获取标签失败: " + e.getMessage());
        }
    }

    @GetMapping("/recent")
    @Operation(summary = "获取最近添加的实体列表", description = "获取指定天数内添加的实体列表")
    public ResponseInfo<List<Entity>> getRecentEntities(
            @Parameter(description = "天数") @RequestParam(value = "days", defaultValue = "7") Integer days,
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        
        try {
            log.info("获取最近添加的实体列表: days={}, userId={}", days, userId);
            
            List<Entity> entities = entityService.getRecentEntitiesByDays(userId, days);
            return ResponseInfo.successResponse(entities);
        } catch (Exception e) {
            log.error("获取最近添加的实体列表异常: days={}, userId={}", days, userId, e);
            return ResponseInfo.errorResponse("获取最近添加的实体列表失败: " + e.getMessage());
        }
    }
} 