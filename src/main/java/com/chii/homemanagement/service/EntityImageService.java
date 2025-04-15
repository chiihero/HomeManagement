package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.EntityImage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 实体图片服务接口
 */
public interface EntityImageService extends IService<EntityImage> {

    /**
     * 获取实体的所有图片
     *
     * @param entityId 实体ID
     * @return 图片列表
     */
    List<EntityImage> getImagesByEntityId(Long entityId);

    /**
     *
     * 删除图片
     *
     * @param imageId 图片ID
     * @return 是否成功
     */
    boolean deleteById(Long imageId);

    /**
     * 删除实体的所有图片
     *
     * @param entityId 实体ID
     * @return 是否成功
     */
    boolean deleteByEntityId(Long entityId);
    
    /**
     * 保存实体图片（存储二进制数据 or 存储文件）
     *
     * @param userId 用户ID
     * @param entityId 实体ID
     * @param file 图片文件
     * @param imageType 图片类型
     * @return 保存的图片
     * @throws IOException 如果文件处理失败
     */
    EntityImage saveEntityImage(Long userId ,Long entityId, MultipartFile file, String imageType) throws IOException;

    /**
     * 根据ID获取图片数据
     *
     * @param imageId 图片ID
     * @return 图片实体对象
     */
    EntityImage getImageWithData(Long imageId);

    /**
     * 获取实体的图片列表
     * @param entityId 实体ID
     * @param type 图片类型（可选）
     * @return 图片列表
     */
    List<EntityImage> getEntityImages(Long entityId, String type);
} 