package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.EntityImage;
import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
import com.chii.homemanagement.service.EntityImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 实体图片控制器
 */
@RestController
@RequestMapping("/api/entity-images")
@io.swagger.v3.oas.annotations.tags.Tag(name = "实体图片管理", description = "实体图片的上传、查询和删除接口")
@Slf4j
@RequiredArgsConstructor
public class EntityImageController {

    private final EntityImageService entityImageService;

    @GetMapping("/entity/{entityId}")
    @Operation(summary = "获取实体的图片列表", description = "获取实体的所有图片信息")
    public ApiResponse<List<EntityImage>> getEntityImages(
            @Parameter(description = "实体ID") @PathVariable(value = "entityId") Long entityId,
            @RequestParam(required = false) String type) {
        
        try {
            log.info("获取实体图片列表: entityId={}", entityId);
            List<EntityImage> images = entityImageService.getEntityImages(entityId, type);
            return ApiResponse.success(images);
        } catch (Exception e) {
            log.error("获取实体图片列表异常: entityId={}", entityId, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取图片列表失败: " + e.getMessage());
        }
    }

    
    @PostMapping("/entity/{entityId}")
    @Operation(summary = "上传图片", description = "上传图片到数据库或文件系统")
    public ApiResponse<EntityImage> uploadEntityImage(
            @Parameter(description = "实体ID") @PathVariable(value = "entityId") Long entityId,
            @Parameter(description = "用户ID") @RequestParam(value = "userId") Long userId,
            @Parameter(description = "图片") @RequestParam(value = "image") MultipartFile image,
            @Parameter(description = "图片类型") @RequestParam(value = "imageType", required = false, defaultValue = "normal") String imageType) {
        
        try {
            log.info("上传实体图片: entityId={}, imageType={}", entityId, imageType);
            //EntityImage entityImage = entityImageService.saveEntityImage(userId ,entityId, image, imageType);
            EntityImage entityImage = entityImageService.saveEntityImageAsAvif(userId ,entityId, image, imageType);

            return ApiResponse.success(entityImage);
        } catch (Exception e) {
            log.error("上传实体图片异常: entityId={}", entityId, e);
            return ApiResponse.error(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传图片失败: " + e.getMessage());
        }
    }

    @GetMapping("/{imageId}")
    @Operation(summary = "获取图片", description = "根据图片ID获取图片数据")
    public ResponseEntity<?> getImageByData(
            @Parameter(description = "图片ID") @PathVariable(value = "imageId") Long imageId) {
        
        try {
            log.info("获取图片数据: imageId={}", imageId);
            
            EntityImage image = entityImageService.getImageWithData(imageId);
            
            if (image == null || image.getImageData() == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            if (image.getContentType() != null) {
                headers.setContentType(MediaType.parseMediaType(image.getContentType()));
            } else {
                headers.setContentType(MediaType.IMAGE_JPEG);
            }
            
            if (image.getFileName() != null) {
                String filename = URLEncoder.encode(image.getFileName(), StandardCharsets.UTF_8.toString())
                        .replaceAll("\\+", "%20");
                headers.setContentDispositionFormData("inline", filename);
            }
            
            if (image.getFileSize() != null) {
                headers.setContentLength(image.getFileSize());
            }
            
            // 返回图片数据
            return new ResponseEntity<>(image.getImageData(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("获取图片异常: imageId={}", imageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/info/{imageId}")
    @Operation(summary = "获取图片信息", description = "根据图片ID获取图片元数据信息，不包含二进制数据")
    public ApiResponse<EntityImage> getImageInfo(
            @Parameter(description = "图片ID") @PathVariable(value = "imageId") Long imageId) {
        
        try {
            log.info("获取图片信息: imageId={}", imageId);
            
            EntityImage image = entityImageService.getById(imageId);
            
            if (image == null) {
                return ApiResponse.error(ErrorCode.DATA_NOT_EXIST.getCode(), "图片不存在");
            }
            
            return ApiResponse.success(image);
        } catch (Exception e) {
            log.error("获取图片信息异常: imageId={}", imageId, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "获取图片信息失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "删除图片", description = "根据图片ID删除图片")
    public ApiResponse<Boolean> deleteImage(
            @Parameter(description = "图片ID") @PathVariable(value = "imageId") Long imageId) {
        
        try {
            log.info("删除图片: imageId={}", imageId);
            
            // 删除数据库记录
            boolean result = entityImageService.deleteById(imageId);
            
            if (result) {
                return ApiResponse.success(true);
            } else {
                return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除图片失败");
            }
        } catch (Exception e) {
            log.error("删除图片异常: imageId={}", imageId, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除图片失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/entity/{entityId}")
    @Operation(summary = "删除实体的所有图片", description = "删除指定实体的所有图片")
    public ApiResponse<Boolean> deleteEntityImages(
            @Parameter(description = "实体ID") @PathVariable(value = "entityId") Long entityId,
            @RequestParam(required = false) String type) {
        
        try {
            log.info("删除实体的所有图片: entityId={}", entityId);
            
            // 删除数据库记录
            boolean result = entityImageService.deleteByEntityId(entityId);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("删除实体图片异常: entityId={}", entityId, e);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "删除实体图片失败: " + e.getMessage());
        }
    }
} 