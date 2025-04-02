package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.EntityTag;
import com.chii.homemanagement.entity.Tag;

import java.util.List;

/**
 * 实体标签服务接口
 */
public interface EntityTagService extends IService<EntityTag> {

    /**
     * 根据实体ID获取所有标签
     * @param entityId 实体ID
     * @return 标签列表
     */
    List<Tag> getTagsByEntityId(Long entityId);

    /**
     * 为实体关联标签
     * @param entityId 实体ID
     * @param tagIds 标签ID列表
     */
    void addTagsToEntity(Long entityId, List<Long> tagIds);

    /**
     * 更新实体的标签
     * @param entityId 实体ID
     * @param tagIds 标签ID列表
     */
    void updateEntityTags(Long entityId, List<Long> tagIds);

    /**
     * 删除实体的所有标签关联
     * @param entityId 实体ID
     */
    void removeEntityTags(Long entityId);

    /**
     * 获取已标记指定标签的实体ID列表
     * @param tagId 标签ID
     * @return 实体ID列表
     */
    List<Long> getEntityIdsByTagId(Long tagId);
} 