package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.EntityImage;
import com.chii.homemanagement.mapper.EntityImageMapper;
import com.chii.homemanagement.service.EntityImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private EntityImageMapper entityImageMapper;

    @Override
    public List<EntityImage> getImagesByEntityId(Long entityId) {
        if (entityId == null) {
            return null;
        }
        return entityImageMapper.listByEntityId(entityId);
    }

    @Override
    @Transactional
    public boolean deleteByEntityId(Long entityId) {
        if (entityId == null) {
            return false;
        }
        return entityImageMapper.deleteByEntityId(entityId) >= 0;
    }

    @Override
    @Transactional
    public EntityImage saveEntityImageWithData(Long entityId, MultipartFile file, String imageType) throws IOException {
        if (entityId == null || file == null || file.isEmpty()) {
            return null;
        }
        
        log.info("保存实体图片数据: entityId={}, 文件名={}, 大小={}", entityId, file.getOriginalFilename(), file.getSize());
        
        EntityImage entityImage = new EntityImage();
        entityImage.setEntityId(entityId);
        entityImage.setImageType(imageType != null ? imageType : "normal");
        entityImage.setImageData(file.getBytes());
        entityImage.setContentType(file.getContentType());
        entityImage.setFileName(file.getOriginalFilename());
        entityImage.setFileSize(file.getSize());
        entityImage.setCreateTime(LocalDateTime.now());
        
        // 获取当前最大排序号，新图片排序号+1
        List<EntityImage> existingImages = getImagesByEntityId(entityId);
        int maxSortOrder = 0;
        if (existingImages != null && !existingImages.isEmpty()) {
            for (EntityImage img : existingImages) {
                if (img.getSortOrder() != null && img.getSortOrder() > maxSortOrder) {
                    maxSortOrder = img.getSortOrder();
                }
            }
        }
        entityImage.setSortOrder(maxSortOrder + 1);
        
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
            return null;
        }
        
        LambdaQueryWrapper<EntityImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityImage::getEntityId, entityId);
        
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(EntityImage::getImageType, type);
        }
        
        queryWrapper.orderByAsc(EntityImage::getSortOrder);
        
        return list(queryWrapper);
    }
} 