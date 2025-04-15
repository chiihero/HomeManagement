package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.EntityImage;
import com.chii.homemanagement.mapper.EntityImageMapper;
import com.chii.homemanagement.service.EntityImageService;
import com.chii.homemanagement.service.FileStorageService;
import com.chii.homemanagement.util.ByteArrayMultipartFile;
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

    @Autowired
    private FileStorageService fileStorageService;


    @Override
    public List<EntityImage> getImagesByEntityId(Long entityId) {
        if (entityId == null) {
            return null;
        }
        return entityImageMapper.listByEntityId(entityId);
    }

    @Override
    public boolean deleteById(Long imageId) {
        if (imageId == null) {
            return false;
        }
        EntityImage entityImage =  getById(imageId);
        if (entityImage !=null){
            if (entityImage.getImageUrl()!=null){
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
        for (EntityImage image : existingImages){
            deleteById(image.getId());
        }
        return true;
    }


    @Override
    @Transactional
    public EntityImage saveEntityImage(Long userId ,Long entityId, MultipartFile file, String imageType) throws IOException {
        if (entityId == null || file == null || file.isEmpty()) {
            return null;
        }
        log.info("保存实体图片数据: entityId={}, 文件名={}, 大小={}", entityId, file.getOriginalFilename(), file.getSize());



        // 获取当前最大排序号，新图片排序号+1
        List<EntityImage> existingImages = getImagesByEntityId(entityId);
        int maxSortOrder = 0;
        if (existingImages != null && !existingImages.isEmpty()) {
            for (EntityImage img : existingImages) {
                if (img.getSortOrder() != null && img.getSortOrder() > maxSortOrder) {
                    maxSortOrder = img.getSortOrder();
                }
                //处理图片在数据库问题，将文件保存在文件系统，删除数据库内的
                if (img.getImageUrl() == null || img.getImageUrl().isEmpty()) {
                    if (img.getImageData() != null){
                        MultipartFile multipartFile = new ByteArrayMultipartFile(
                                img.getImageData(),
                                "image",
                                img.getFileName(),
                                img.getContentType()
                        );
                        String fileUrl = fileStorageService.storeFile(multipartFile, userId+"/entity");
                        img.setImageUrl(fileUrl);
                        img.setImageData(null);
                        updateById(img);
                    }

                }
            }
        }
        EntityImage entityImage = new EntityImage();
        entityImage.setEntityId(entityId);
        entityImage.setImageType(imageType != null ? imageType : "normal");
        entityImage.setContentType(file.getContentType());
        entityImage.setImageData(file.getBytes());//todo 后期删除
        entityImage.setFileName(file.getOriginalFilename());
        entityImage.setFileSize(file.getSize());
        entityImage.setCreateTime(LocalDateTime.now());
        entityImage.setSortOrder(maxSortOrder + 1);
        //保存到本地
        String fileUrl = fileStorageService.storeFile(file, userId+"/entity");
        entityImage.setImageUrl(fileUrl);
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