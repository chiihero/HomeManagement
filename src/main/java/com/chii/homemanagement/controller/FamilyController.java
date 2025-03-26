package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.Family;
import com.chii.homemanagement.entity.FamilyMember;
import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 家庭管理控制器
 */
@RestController
@RequestMapping("/api/families")
@Tag(name = "家庭管理", description = "家庭的增删改查接口")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    @GetMapping("/{id}")
    @Operation(summary = "获取家庭详情", description = "根据ID获取家庭详情")
    public ResponseInfo<Family> getFamilyById(
            @Parameter(description = "家庭ID") @PathVariable Long id) {
        Family family = familyService.getFamilyById(id);
        return ResponseInfo.successResponse(family);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户的家庭列表", description = "获取指定用户所属的所有家庭")
    public ResponseInfo<List<Family>> getUserFamilies(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<Family> families = familyService.getUserFamilies(userId);
        return ResponseInfo.successResponse(families);
    }

    @PostMapping
    @Operation(summary = "创建家庭", description = "创建新家庭")
    public ResponseInfo<Family> createFamily(
            @RequestBody Family family,
            @Parameter(description = "创建者ID") @RequestParam Long creatorId) {
        family.setCreateTime(LocalDateTime.now());
        family.setUpdateTime(LocalDateTime.now());
        Family createdFamily = familyService.createFamily(family, creatorId);
        return ResponseInfo.successResponse(createdFamily);
    }

    @PutMapping
    @Operation(summary = "更新家庭", description = "更新家庭信息")
    public ResponseInfo<Family> updateFamily(@RequestBody Family family) {
        family.setUpdateTime(LocalDateTime.now());
        Family updatedFamily = familyService.updateFamily(family);
        return ResponseInfo.successResponse(updatedFamily);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除家庭", description = "根据ID删除家庭")
    public ResponseInfo<Boolean> deleteFamily(
            @Parameter(description = "家庭ID") @PathVariable Long id) {
        boolean result = familyService.deleteFamily(id);
        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("移除家庭成员失败");
        }
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "获取家庭成员", description = "获取指定家庭的所有成员")
    public ResponseInfo<List<User>> getFamilyMembers(
            @Parameter(description = "家庭ID") @PathVariable Long id) {
        List<User> members = familyService.getFamilyMembers(id);
        return ResponseInfo.successResponse(members);
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "添加家庭成员", description = "向指定家庭添加新成员")
    public ResponseInfo<FamilyMember> addFamilyMember(
            @Parameter(description = "家庭ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色") @RequestParam(defaultValue = "member") String role) {
        FamilyMember member = familyService.addFamilyMember(id, userId, role);
        return ResponseInfo.successResponse(member);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "移除家庭成员", description = "从指定家庭移除成员")
    public ResponseInfo<Boolean> removeFamilyMember(
            @Parameter(description = "家庭ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        boolean result = familyService.removeFamilyMember(id, userId);
        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("移除家庭成员失败");
        }
    }
} 