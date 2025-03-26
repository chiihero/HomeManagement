package com.chii.homemanagement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.entity.Space;
import com.chii.homemanagement.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 空间管理控制器
 */
@RestController
@RequestMapping("/api/spaces")
@Tag(name = "空间管理", description = "空间的增删改查接口")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @GetMapping("/page")
    @Operation(summary = "分页查询空间列表", description = "根据条件分页查询空间列表")
    public ResponseInfo<IPage<Space>> pageSpaces(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "空间名称") @RequestParam(required = false) String name,
            @Parameter(description = "父空间ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        // 构建分页对象
        Page<Space> page = new Page<>(current, size);

        // 构建查询条件
        Space space = new Space();
        space.setName(name);
        space.setParentId(parentId);
        space.setFamilyId(familyId);

        // 调用服务层方法
        IPage<Space> result = spaceService.pageSpaces(page, space);

        return ResponseInfo.successResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询空间详情", description = "根据ID查询空间详情")
    public ResponseInfo<Space> getSpaceDetail(
            @Parameter(description = "空间ID") @PathVariable Long id) {

        Space space = spaceService.getById(id);

        return ResponseInfo.successResponse(space);
    }

    @PostMapping
    @Operation(summary = "新增空间", description = "新增空间信息")
    public ResponseInfo<Boolean> addSpace(@RequestBody Space space) {
        // 设置创建者ID为1L，简化处理
        space.setCreateUserId(1L);
        
        boolean result = spaceService.saveSpace(space);
        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("新增空间失败");
        }
    }

    @PutMapping
    @Operation(summary = "更新空间", description = "更新空间信息")
    public ResponseInfo<Boolean> updateSpace(@RequestBody Space space) {
        boolean result = spaceService.updateSpace(space);
        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("更新空间失败");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除空间", description = "根据ID删除空间")
    public ResponseInfo<Boolean> deleteSpace(
            @Parameter(description = "空间ID") @PathVariable Long id) {

        boolean result = spaceService.removeSpace(id);

        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("删除空间失败");
        }
    }

    @GetMapping("/tree")
    @Operation(summary = "获取空间树", description = "获取指定家庭的空间树结构")
    public ResponseInfo<List<Space>> getSpaceTree(
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Space> spaceTree = spaceService.getSpaceTree(familyId);

        return ResponseInfo.successResponse(spaceTree);
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子空间列表", description = "获取指定父空间下的子空间列表")
    public ResponseInfo<List<Space>> getChildrenSpaces(
            @Parameter(description = "父空间ID") @PathVariable Long parentId,
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Space> children = spaceService.getChildrenSpaces(parentId, familyId);

        return ResponseInfo.successResponse(children);
    }

    @GetMapping("/root")
    @Operation(summary = "获取根空间列表", description = "获取指定家庭的根空间列表")
    public ResponseInfo<List<Space>> getRootSpaces(
            @Parameter(description = "家庭ID") @RequestParam Long familyId) {

        List<Space> rootSpaces = spaceService.getRootSpaces(familyId);

        return ResponseInfo.successResponse(rootSpaces);
    }
}