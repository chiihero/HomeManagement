package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.ItemImage;
import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.entity.ResultCode;
import com.chii.homemanagement.service.ItemImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 物品图片控制器
 */
@RestController
@RequestMapping("/api/item-images")
@Tag(name = "物品图片管理", description = "物品图片的上传和管理接口")
public class ItemImageController {

    @Autowired
    private ItemImageService itemImageService;

    @PostMapping("/upload")
    @Operation(summary = "上传物品图片", description = "上传物品的图片，支持设置主图")
    public ResponseInfo<ItemImage> uploadItemImage(
            @Parameter(description = "物品ID") @RequestParam Long itemId,
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "是否设为主图") @RequestParam(defaultValue = "false") Boolean isMain) {
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB限制
//            throw  Exception(ResultCode.PARAM_NOT_VALID, "文件大小超过限制");
        }
        ItemImage image = itemImageService.uploadImage(itemId, file, isMain);
        return ResponseInfo.successResponse(image);
    }

    @GetMapping("/item/{itemId}")
    @Operation(summary = "获取物品图片列表", description = "获取指定物品的所有图片")
    public ResponseInfo<List<ItemImage>> getItemImages(
            @Parameter(description = "物品ID") @PathVariable Long itemId) {

        List<ItemImage> images = itemImageService.getItemImages(itemId);
        return ResponseInfo.successResponse(images);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "删除图片", description = "删除指定的物品图片")
    public ResponseInfo<Boolean> deleteImage(
            @Parameter(description = "图片ID") @PathVariable Long id) {

        boolean result = itemImageService.deleteImage(id);
        if (result) {
            return ResponseInfo.successResponse(true);
        } else {
            return ResponseInfo.errorResponse("删除图片失败");
        }
    }
} 