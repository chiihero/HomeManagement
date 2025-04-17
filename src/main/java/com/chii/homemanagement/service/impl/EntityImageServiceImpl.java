package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.EntityImage;
import com.chii.homemanagement.mapper.EntityImageMapper;
import com.chii.homemanagement.mapper.EntityMapper;
import com.chii.homemanagement.service.EntityImageService;
import com.chii.homemanagement.service.FileStorageService;
import com.chii.homemanagement.util.ByteArrayMultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 实体图片服务实现类
 */
@Service
@Slf4j
public class EntityImageServiceImpl extends ServiceImpl<EntityImageMapper, EntityImage> implements EntityImageService {

    private static final Logger logger = LogManager.getLogger(EntityImageServiceImpl.class);

    @Autowired
    private EntityImageMapper entityImageMapper;

    @Autowired
    private EntityMapper entityMapper;

    @Autowired
    private FileStorageService fileStorageService;


    @Override
    public List<EntityImage> getImagesByEntityId(Long entityId) {
        if (entityId == null) {
            return List.of();
        }
        return entityImageMapper.listByEntityId(entityId);
    }

    @Override
    @Transactional
    public boolean deleteById(Long imageId) {
        if (imageId == null) {
            return false;
        }
        EntityImage entityImage = getById(imageId);
        if (entityImage != null) {
            if (entityImage.getImageUrl() != null) {
                fileStorageService.deleteFile(entityImage.getImageUrl());
            }
            return removeById(imageId);
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteByEntityId(Long entityId) {
        if (entityId == null) {
            return false;
        }
        List<EntityImage> existingImages = entityImageMapper.listByEntityId(entityId);
        for (EntityImage image : existingImages) {
            deleteById(image.getId());
        }
        return true;
    }


    @Override
    @Transactional
    public EntityImage saveEntityImage(Long userId, Long entityId, MultipartFile file, String imageType) throws IOException {
        if (entityId == null || file == null || file.isEmpty()) {
            return null;
        }
        log.info("保存实体图片数据: entityId={}, 文件名={}, 大小={}", entityId, file.getOriginalFilename(), file.getSize());

        // 获取当前最大排序号，新图片排序号+1
        Integer maxSort = entityImageMapper.maxSortByEntityId(entityId);
        int maxSortOrder = Math.max(0, maxSort != null ? maxSort : 0);

        EntityImage entityImage = new EntityImage();
        entityImage.setEntityId(entityId);
        entityImage.setImageType(imageType != null ? imageType : "normal");
        entityImage.setContentType(file.getContentType());
        //entityImage.setImageData(file.getBytes());//todo 后期删除
        entityImage.setFileName(file.getOriginalFilename());
        entityImage.setFileSize(file.getSize());
        entityImage.setCreateTime(LocalDateTime.now());
        entityImage.setSortOrder(maxSortOrder + 1);
        //保存到本地
        String fileUrl = fileStorageService.storeFile(file, userId + "/entities");
        entityImage.setImageUrl(fileUrl);
        save(entityImage);
        return entityImage;
    }

    @Override
    @Transactional
    public EntityImage saveEntityImageAsAvif(Long userId, Long entityId, MultipartFile file, String imageType) throws IOException {
        if (entityId == null || file == null || file.isEmpty()) {
            return null;
        }
        log.info("保存实体图片为AVIF格式: entityId={}, 文件名={}, 原始大小={} KB", 
                entityId, file.getOriginalFilename(), file.getSize() / 1024);

        // 获取当前最大排序值
        Integer maxSort = entityImageMapper.maxSortByEntityId(entityId);
        int maxSortOrder = Math.max(0, maxSort != null ? maxSort : 0);

        // 构建实体图片对象
        EntityImage entityImage = new EntityImage();
        entityImage.setEntityId(entityId);
        entityImage.setImageType(imageType != null ? imageType : "normal");
        entityImage.setContentType("image/avif");  // AVIF格式的MIME类型
        entityImage.setFileName(StringUtils.cleanPath(file.getOriginalFilename()) + ".avif");
        entityImage.setCreateTime(LocalDateTime.now());
        entityImage.setSortOrder(maxSortOrder + 1);
        
        // 转换并存储为AVIF格式
        String fileUrl = fileStorageService.storeImageAsAvif(file, userId + "/entities");
        entityImage.setImageUrl(fileUrl);
        
        // 获取转换后的文件大小
        String relativePath = fileUrl.startsWith("/uploads") ? fileUrl.substring("/uploads".length()) : fileUrl;
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get("uploads", relativePath);
            if (java.nio.file.Files.exists(filePath)) {
                long avifSize = java.nio.file.Files.size(filePath);
                entityImage.setFileSize(avifSize);
                log.info("AVIF转换完成: 原始大小={} KB, AVIF大小={} KB, 压缩率={}%", 
                        file.getSize() / 1024, avifSize / 1024, 
                        Math.round((1 - (double)avifSize / file.getSize()) * 100));
            } else {
                // 如果无法获取实际大小，则保留原始文件大小
                entityImage.setFileSize(file.getSize());
                logger.warn("无法获取转换后的AVIF文件大小，使用原始文件大小: {}", filePath);
            }
        } catch (Exception e) {
            // 如果获取文件大小失败，使用原始文件大小
            entityImage.setFileSize(file.getSize());
            logger.error("获取AVIF文件大小失败", e);
        }
        
        save(entityImage);
        return entityImage;
    }

    @Override
    public EntityImage getImageWithData(Long imageId) {
        if (imageId == null) {
            return null;
        }
        return getById(imageId);
    }

    @Override
    public List<EntityImage> getEntityImages(Long entityId, String type) {
        if (entityId == null) {
            return List.of();
        }

        LambdaQueryWrapper<EntityImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityImage::getEntityId, entityId);

        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(EntityImage::getImageType, type);
        }

        queryWrapper.orderByAsc(EntityImage::getSortOrder);

        return list(queryWrapper);
    }

    @Override
    public void setEntityImagesToFiles() {
        List<EntityImage> existingImages = list();
        for (EntityImage img : existingImages) {
            Long userId = entityMapper.selectById(img.getEntityId()).getUserId();
            //处理图片在数据库问题，将文件保存在文件系统，删除数据库内
            if (img.getImageUrl() == null || img.getImageUrl().isEmpty()) {
                if (img.getImageData() != null) {
                    MultipartFile multipartFile = new ByteArrayMultipartFile(
                            img.getImageData(),
                            "image",
                            img.getFileName(),
                            img.getContentType()
                    );
                    String fileUrl = null;
                    try {
                        fileUrl = fileStorageService.storeImageAsAvif (multipartFile, userId + "/entities");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    img.setImageUrl(fileUrl);
                    img.setImageData(null);
                    updateById(img);

                }
            }
        }
    }

}