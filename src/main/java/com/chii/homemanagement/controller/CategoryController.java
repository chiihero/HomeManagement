package com.chii.homemanagement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chii.homemanagement.entity.Category;
import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 分类管理控制器
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "分类管理", description = "分类的增删改查接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    @Operation(summary = "分页查询分类列表", description = "根据条件分页查询分类列表")
    public ResponseInfo<IPage<Category>> pageCategories(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "分类名称") @RequestParam(required = false) String name,
            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "层级") @RequestParam(required = false) Integer level,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        // 构建分页对象
        Page<Category> page = new Page<>(current, size);

        // 构建查询条件
        Category category = new Category();
        category.setName(name);
        category.setParentId(parentId);
        category.setLevel(level);

        // 使用LambdaQueryWrapper进行分页查询
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getFamilyId, familyId)
                .like(name != null, Category::getName, name)
                .eq(parentId != null, Category::getParentId, parentId)
                .eq(level != null, Category::getLevel, level);

        IPage<Category> result = categoryService.page(page, queryWrapper);

        return ResponseInfo.successResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询分类详情", description = "根据ID查询分类详情")
    public ResponseInfo<Category> getCategoryDetail(
            @Parameter(description = "分类ID") @PathVariable Long id) {

        Category category = categoryService.getCategoryById(id);

        return ResponseInfo.successResponse(category);
    }

    @PostMapping
    @Operation(summary = "新增分类", description = "新增分类信息")
    public ResponseInfo<Boolean> addCategory(@RequestBody Category category) {
        // 设置创建者ID为1L，简化处理
        category.setCreateUserId(1L);
        
        boolean result = categoryService.addCategory(category);

        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("添加分类失败");
        }
    }

    @PutMapping
    @Operation(summary = "更新分类", description = "更新分类信息")
    public ResponseInfo<Boolean> updateCategory(@RequestBody Category category) {
        boolean result = categoryService.updateCategory(category);

        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("更新分类失败");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "根据ID删除分类")
    public ResponseInfo<Boolean> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {

        boolean result = categoryService.deleteCategory(id);

        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("删除分类失败");
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除分类", description = "批量删除分类")
    public ResponseInfo<Boolean> batchDeleteCategories(@RequestBody List<Long> ids) {
        boolean result = categoryService.removeBatchByIds(ids);

        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("批量删除分类失败");
        }
    }

    @GetMapping("/tree")
    @Operation(summary = "查询分类树", description = "查询分类树形结构")
    public ResponseInfo<List<Category>> getCategoryTree(
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Category> categories = categoryService.getCategoriesByFamilyId(familyId);
        List<Category> tree = buildCategoryTree(categories);

        return ResponseInfo.successResponse(tree);
    }

    @GetMapping("/children")
    @Operation(summary = "查询子分类列表", description = "根据父ID查询子分类列表")
    public ResponseInfo<List<Category>> listCategoriesByParentId(
            @Parameter(description = "父分类ID") @RequestParam(defaultValue = "0") Long parentId,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getFamilyId, familyId)
                .eq(Category::getParentId, parentId);
        List<Category> children = categoryService.list(queryWrapper);

        return ResponseInfo.successResponse(children);
    }

    @GetMapping("/path/{id}")
    @Operation(summary = "查询分类路径", description = "查询分类的完整路径")
    public ResponseInfo<List<Category>> getCategoryPath(
            @Parameter(description = "分类ID") @PathVariable Long id) {

        List<Category> path = new ArrayList<>();
        buildCategoryPath(id, path);

        // 反转路径，使其从根分类到当前分类
        Collections.reverse(path);

        return ResponseInfo.successResponse(path);
    }

    /**
     * 构建分类树
     *
     * @param categories 分类列表
     * @return 分类树
     */
    private List<Category> buildCategoryTree(List<Category> categories) {
        List<Category> tree = new ArrayList<>();
        Map<Long, Category> categoryMap = new HashMap<>();

        // 将分类放入Map
        for (Category category : categories) {
            categoryMap.put(category.getId(), category);
        }

        // 构建树结构
        for (Category category : categories) {
            Long parentId = category.getParentId();
            if (parentId == null || parentId == 0) {
                // 根分类
                tree.add(category);
            } else {
                // 子分类
                Category parent = categoryMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(category);
                }
            }
        }

        return tree;
    }

    /**
     * 构建分类路径
     *
     * @param categoryId 分类ID
     * @param path       路径列表
     */
    private void buildCategoryPath(Long categoryId, List<Category> path) {
        if (categoryId == null || categoryId == 0) {
            return;
        }

        Category category = categoryService.getById(categoryId);
        if (category != null) {
            path.add(category);
            buildCategoryPath(category.getParentId(), path);
        }
    }
}