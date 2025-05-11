package com.chii.homemanagement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chii.homemanagement.entity.Tag;
import com.chii.homemanagement.service.TagService;
import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 * 
 * @author chii
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "标签的增删改查接口")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT认证")
public class TagController {

    private final TagService tagService;

    @GetMapping("/page")
    @Operation(summary = "分页查询实体列表", description = "根据条件分页查询实体列表")
    public ApiResponse<IPage<Tag>> pageTags(
            @Parameter(description = "当前页码") @RequestParam(value = "current", defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "使用人ID") @RequestParam(value = "userId") Long userId) {

        try {
            log.info("分页查询实体列表: userId={}, current={}, size={}", userId, current, size);

            // 构建分页对象
            Page<Tag> page = new Page<>(current, size);
            // 调用服务层方法
            IPage<Tag> result = tagService.pageTags(page, userId);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("分页查询实体列表异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "查询失败: " + e.getMessage());
        }
    }
    /**
     * 获取标签列表
     *
     * @param userId 用户ID
     * @return 标签列表响应
     */
    @GetMapping
    @Operation(summary = "获取标签列表", description = "获取用户的所有标签")

    public ApiResponse<List<Tag>> listTags(
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId) {
        try {
            log.info("获取标签列表: userId={}", userId);
            List<Tag> tags = tagService.getTagsByUserId(userId);
            log.debug("获取到标签列表, 数量: {}", tags.size());
            return ApiResponse.success(tags);
        } catch (Exception e) {
            log.error("获取标签列表异常: userId={}", userId, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取标签列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取物品的所有标签
     * 
     * @param itemId 物品ID
     * @return 标签列表
     */
    @GetMapping("/item/{itemId}")
    @Operation(summary = "获取物品标签", description = "根据物品ID获取标签")
    public ApiResponse<List<Tag>> getTagsByItemId(@PathVariable(value = "itemId") Long itemId) {
        List<Tag> tags = tagService.getTagsByItemId(itemId);
        return ApiResponse.success(tags);
    }

    /**
     * 添加标签
     *
     * @param tag 标签信息
     * @return 添加结果响应
     */
    @PostMapping
    @Operation(summary = "添加标签", description = "添加新标签")

    public ApiResponse<Tag> addTag(@RequestBody Tag tag) {
        try {
            log.info("添加标签: name={}, userId={}", tag.getName(), tag.getUserId());
            Long tagId = tagService.addTag(tag);
            
            if (tagId != null && tagId > 0) {
                log.info("标签添加成功: id={}, name={}", tagId, tag.getName());
                return ApiResponse.success(tag);
            } else {
                log.warn("标签添加失败");
                return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "添加标签失败");
            }
        } catch (Exception e) {
            log.error("添加标签异常: ", e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "添加标签失败: " + e.getMessage());
        }
    }

    /**
     * 更新标签
     *
     * @param id 标签ID
     * @param tag 标签信息
     * @return 更新结果响应
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新标签", description = "根据ID更新标签信息")

    public ApiResponse<Boolean> updateTag(
            @Parameter(description = "标签ID") @PathVariable(value = "id") Long id,
            @RequestBody Tag tag) {
        try {
            log.info("更新标签: id={}, name={}", id, tag.getName());
            tag.setId(id);
            
            boolean result = tagService.updateTag(tag);
            
            if (result) {
                log.info("标签更新成功: id={}", id);
                return ApiResponse.success(true);
            } else {
                log.warn("标签更新失败: id={}", id);
                return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新标签失败");
            }
        } catch (Exception e) {
            log.error("更新标签异常: id={}", id, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新标签失败: " + e.getMessage());
        }
    }

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 删除结果响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "根据ID删除标签")
    public ApiResponse<Boolean> deleteTag(
            @Parameter(description = "标签ID") @PathVariable(value = "id") Long id) {
        try {
            log.info("删除标签: id={}", id);
            
            boolean result = tagService.deleteTag(id);
            
            if (result) {
                log.info("标签删除成功: id={}", id);
                return ApiResponse.success(true);
            } else {
                log.warn("标签删除失败: id={}", id);
                return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除标签失败");
            }
        } catch (Exception e) {
            log.error("删除标签异常: id={}", id, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除标签失败: " + e.getMessage());
        }
    }
} 