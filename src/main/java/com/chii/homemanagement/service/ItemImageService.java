package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.ItemImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 物品图片服务接口
 */
public interface ItemImageService extends IService<ItemImage> {

    /**
     * 上传物品图片
     *
     * @param itemId 物品ID
     * @param file   图片文件
     * @param isMain 是否主图
     * @return 图片信息
     */
    ItemImage uploadImage(Long itemId, MultipartFile file, Boolean isMain);

    /**
     * 删除图片
     *
     * @param id 图片ID
     * @return 是否成功
     */
    boolean deleteImage(Long id);

    /**
     * 获取物品图片列表
     *
     * @param itemId 物品ID
     * @return 图片列表
     */
    List<ItemImage> getItemImages(Long itemId);

} 