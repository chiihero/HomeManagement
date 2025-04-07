package com.chii.homemanagement.controller;

import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
import com.chii.homemanagement.entity.Tag;
import com.chii.homemanagement.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@RestController
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "标签的增删改查接口")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 获取标签列表
     * 
     * @param userId 用户ID
     * @return 标签列表
     */
    @GetMapping
    @Operation(summary = "获取标签列表", description = "根据用户ID获取所有标签")
    public ApiResponse<List<Tag>> getTags(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId != null) {
            // 如果提供了userId，返回对应所有者的标签
            List<Tag> tags = tagService.getTagsByUserId(userId);
            return ApiResponse.success(tags);
        } else {
            // 如果没有提供userId，返回错误信息
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "需要提供userId参数");
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
     * 添加新标签
     * 
     * @param tag 标签信息
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "创建标签", description = "创建新标签")
    public ApiResponse<Tag> addTag(@Validated @RequestBody Tag tag) {
        // 设置创建者ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            // 这里需要获取用户ID，由于示例中直接从认证上下文中获取不到ID，实际项目中应该有方法获取
            // 通常可以通过UserDetailsService实现类中的loadUserByUsername方法获取完整用户信息
            // 这里暂时假设有一个方法可以获取到用户ID
            Long userId = getCurrentUserId(username);
            tag.setCreateUserId(userId);
        }
        
        Long tagId = tagService.addTag(tag);
        if (tagId != null && tagId > 0) {
            return ApiResponse.success(tag);
        } else {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "添加标签失败");
        }
    }

    /**
     * 更新标签
     * 
     * @param id  标签ID
     * @param tag 标签信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新标签", description = "更新标签信息")
    public ApiResponse<Tag> updateTag(@PathVariable(value = "id") Long id, @Validated @RequestBody Tag tag) {
        tag.setId(id);
        boolean success = tagService.updateTag(tag);
        if (success) {
            return ApiResponse.success(tag);
        } else {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "更新标签失败");
        }
    }

    /**
     * 删除标签
     * 
     * @param id 标签ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "根据ID删除标签")
    public ApiResponse<Boolean> deleteTag(@PathVariable(value = "id") Long id) {
        boolean success = tagService.deleteTag(id);
        if (success) {
            return ApiResponse.success(true);
        } else {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除标签失败");
        }
    }

    /**
     * 设置物品的标签
     * 
     * @param itemId 物品ID
     * @param tagIds 标签ID列表
     * @return 设置结果
     */
    @PostMapping("/item/{itemId}")
    @Operation(summary = "设置物品标签", description = "设置物品的标签关联")
    public ApiResponse<Boolean> setItemTags(@PathVariable(value = "itemId") Long itemId, @RequestBody List<Long> tagIds) {
        boolean success = tagService.setItemTags(itemId, tagIds);
        if (success) {
            return ApiResponse.success(true);
        } else {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "设置物品标签失败");
        }
    }
    
    /**
     * 获取当前用户ID的辅助方法
     * 实际项目中应通过用户服务获取
     */
    private Long getCurrentUserId(String username) {
        // 这里应该通过用户服务获取用户ID
        // 示例实现，实际项目中需要替换为真实实现
        return 1L; // 临时返回默认值
    }
} 