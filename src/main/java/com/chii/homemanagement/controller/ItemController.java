package com.chii.homemanagement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chii.homemanagement.entity.Item;
import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.ItemService;
import com.chii.homemanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 物品管理控制器
 */
@RestController
@RequestMapping("/api/items")
@Tag(name = "物品管理", description = "物品的增删改查接口")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @GetMapping("/page")
    @Operation(summary = "分页查询物品列表", description = "根据条件分页查询物品列表")
    public ResponseInfo<IPage<Item>> pageItems(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "物品名称") @RequestParam(required = false) String name,
            @Parameter(description = "规格") @RequestParam(required = false) String specification,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "使用频率") @RequestParam(required = false) String usageFrequency,
            @Parameter(description = "使用人ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "存放空间ID") @RequestParam(required = false) Long spaceId,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        // 构建分页对象
        Page<Item> page = new Page<>(current, size);

        // 构建查询条件
        Item item = new Item();
        item.setName(name);
        item.setSpecification(specification);
        item.setStatus(status);
        item.setUsageFrequency(usageFrequency);
        item.setUserId(userId);
        item.setSpaceId(spaceId);

        // 调用服务层方法
        IPage<Item> result = itemService.pageItems(page, item, familyId);

        return ResponseInfo.successResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询物品详情", description = "根据ID查询物品详情")
    public ResponseInfo<Item> getItemDetail(
            @Parameter(description = "物品ID") @PathVariable Long id) {

        Item item = itemService.getItemDetail(id);

        return ResponseInfo.successResponse(item);
    }

    @PostMapping
    @Operation(summary = "新增物品", description = "新增物品信息")
    public ResponseInfo<Boolean> addItem(@RequestBody Item item) {
        // 获取当前登录用户，设置为创建者
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            if (user != null) {
                item.setCreateUserId(user.getId());
            }
        }
        
        boolean result = itemService.addItem(item);
        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("新增物品失败");
        }
    }

    @PutMapping
    @Operation(summary = "更新物品", description = "更新物品信息")
    public ResponseInfo<Boolean> updateItem(@RequestBody Item item) {
        boolean result = itemService.updateItem(item);
        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("更新物品失败");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物品", description = "根据ID删除物品")
    public ResponseInfo<Boolean> deleteItem(
            @Parameter(description = "物品ID") @PathVariable Long id) {

        boolean result = itemService.deleteItem(id);

        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("删除物品失败");
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除物品", description = "批量删除物品")
    public ResponseInfo<Boolean> batchDeleteItems(@RequestBody List<Long> ids) {
        boolean result = itemService.batchDeleteItems(ids);
        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("批量删除物品失败");
        }
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "根据分类查询物品列表", description = "根据分类ID查询物品列表")
    public ResponseInfo<List<Item>> listItemsByCategory(
            @Parameter(description = "分类ID") @PathVariable Long categoryId,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Item> items = itemService.listItemsByCategory(categoryId, familyId);

        return ResponseInfo.successResponse(items);
    }

    @GetMapping("/space/{spaceId}")
    @Operation(summary = "根据空间查询物品列表", description = "根据空间ID查询物品列表")
    public ResponseInfo<List<Item>> listItemsBySpace(
            @Parameter(description = "空间ID") @PathVariable Long spaceId,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Item> items = itemService.listItemsBySpace(spaceId, familyId);

        return ResponseInfo.successResponse(items);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据使用人查询物品列表", description = "根据使用人ID查询物品列表")
    public ResponseInfo<List<Item>> listItemsByUser(
            @Parameter(description = "使用人ID") @PathVariable Long userId,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Item> items = itemService.listItemsByUser(userId, familyId);

        return ResponseInfo.successResponse(items);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询物品列表", description = "根据状态查询物品列表")
    public ResponseInfo<List<Item>> listItemsByStatus(
            @Parameter(description = "状态") @PathVariable String status,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Item> items = itemService.listItemsByStatus(status, familyId);

        return ResponseInfo.successResponse(items);
    }

    @GetMapping("/expiring")
    @Operation(summary = "查询即将过期的物品列表", description = "查询即将过期的物品列表")
    public ResponseInfo<List<Item>> listExpiringItems(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Item> items = itemService.listExpiringItems(days, familyId);

        return ResponseInfo.successResponse(items);
    }

    @GetMapping("/expired")
    @Operation(summary = "查询已过期的物品列表", description = "查询已过期的物品列表")
    public ResponseInfo<List<Item>> listExpiredItems(
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Item> items = itemService.listExpiredItems(familyId);

        return ResponseInfo.successResponse(items);
    }

    @GetMapping("/value")
    @Operation(summary = "统计物品总价值", description = "统计物品总价值")
    public ResponseInfo<Double> sumItemsValue(
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        double value = itemService.sumItemsValue(familyId);

        return ResponseInfo.successResponse(value);
    }

    @GetMapping("/stat/category")
    @Operation(summary = "统计各分类物品数量与价值", description = "统计各分类物品数量与价值")
    public ResponseInfo<List<Object>> statItemsByCategory(
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Object> stats = itemService.statItemsByCategory(familyId);

        return ResponseInfo.successResponse(stats);
    }

    @GetMapping("/stat/space")
    @Operation(summary = "统计各位置物品分布", description = "统计各位置物品分布")
    public ResponseInfo<List<Object>> statItemsBySpace(
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Object> stats = itemService.statItemsBySpace(familyId);

        return ResponseInfo.successResponse(stats);
    }

    @GetMapping("/stat/frequency")
    @Operation(summary = "统计使用频率分析", description = "统计使用频率分析")
    public ResponseInfo<List<Object>> statItemsByUsageFrequency(
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Object> stats = itemService.statItemsByUsageFrequency(familyId);

        return ResponseInfo.successResponse(stats);
    }

    @GetMapping("/date")
    @Operation(summary = "根据购买日期范围查询物品列表", description = "根据购买日期范围查询物品列表")
    public ResponseInfo<List<Item>> listItemsByPurchaseDateRange(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Item> items = itemService.listItemsByPurchaseDateRange(startDate, endDate, familyId);

        return ResponseInfo.successResponse(items);
    }

    @GetMapping("/price")
    @Operation(summary = "根据价格区间查询物品列表", description = "根据价格区间查询物品列表")
    public ResponseInfo<List<Item>> listItemsByPriceRange(
            @Parameter(description = "最小价格") @RequestParam Double minPrice,
            @Parameter(description = "最大价格") @RequestParam Double maxPrice,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Item> items = itemService.listItemsByPriceRange(minPrice, maxPrice, familyId);

        return ResponseInfo.successResponse(items);
    }


}