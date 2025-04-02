package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.EntityTag;
import com.chii.homemanagement.entity.Tag;
import com.chii.homemanagement.mapper.EntityTagMapper;
import com.chii.homemanagement.service.EntityTagService;
import com.chii.homemanagement.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实体标签关联服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EntityTagServiceImpl extends ServiceImpl<EntityTagMapper, EntityTag> implements EntityTagService {

    private final TagService tagService;

    @Override
    public List<Tag> getTagsByEntityId(Long entityId) {
        if (entityId == null) {
            log.warn("获取标签列表失败: 实体ID为空");
            return Collections.emptyList();
        }
        
        log.info("获取实体标签: entityId={}", entityId);
        
        // 查询实体关联的所有标签ID
        List<EntityTag> entityTags = this.list(new LambdaQueryWrapper<EntityTag>()
                .eq(EntityTag::getEntityId, entityId));
        
        if (entityTags.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取标签ID列表
        List<Long> tagIds = entityTags.stream()
                .map(EntityTag::getTagId)
                .distinct()
                .collect(Collectors.toList());
        
        log.info("实体关联标签数量: entityId={}, tagCount={}", entityId, tagIds.size());
        
        // 根据ID查询标签详细信息
        return tagService.listByIds(tagIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTagsToEntity(Long entityId, List<Long> tagIds) {
        if (entityId == null || tagIds == null || tagIds.isEmpty()) {
            log.warn("添加标签失败: 实体ID为空或标签列表为空");
            return;
        }
        
        // 去重
        List<Long> distinctTagIds = tagIds.stream()
                .distinct()
                .collect(Collectors.toList());
        
        log.info("添加标签到实体: entityId={}, tagCount={}", entityId, distinctTagIds.size());
        
        // 批量构建实体标签关联
        List<EntityTag> entityTags = distinctTagIds.stream()
                .map(tagId -> {
                    EntityTag entityTag = new EntityTag();
                    entityTag.setEntityId(entityId);
                    entityTag.setTagId(tagId);
                    return entityTag;
                })
                .collect(Collectors.toList());
        
        // 批量保存
        boolean success = this.saveBatch(entityTags);
        
        if (!success) {
            log.error("批量添加标签失败: entityId={}", entityId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEntityTags(Long entityId, List<Long> tagIds) {
        if (entityId == null) {
            log.warn("更新标签失败: 实体ID为空");
            return;
        }
        
        log.info("更新实体标签: entityId={}, tagCount={}", entityId, tagIds != null ? tagIds.size() : 0);
        
        try {
            // 先删除现有关联
            this.removeEntityTags(entityId);
            
            // 如果有新标签，则添加关联
            if (tagIds != null && !tagIds.isEmpty()) {
                this.addTagsToEntity(entityId, tagIds);
            }
        } catch (Exception e) {
            log.error("更新实体标签异常: entityId={}", entityId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeEntityTags(Long entityId) {
        if (entityId == null) {
            log.warn("删除标签关联失败: 实体ID为空");
            return;
        }
        
        log.info("删除实体标签关联: entityId={}", entityId);
        
        try {
            long count = this.count(new LambdaQueryWrapper<EntityTag>()
                    .eq(EntityTag::getEntityId, entityId));
            if (count == 0){
                log.info("无实体标签关联: entityId={}, removedCount={}", entityId, count);
                return;
            }

            boolean success = this.remove(new LambdaQueryWrapper<EntityTag>()
                    .eq(EntityTag::getEntityId, entityId));
            
            if (success) {
                log.info("删除实体标签关联成功: entityId={}, removedCount={}", entityId, count);
            } else {
                log.warn("删除实体标签关联失败: entityId={}", entityId);
            }
        } catch (Exception e) {
            log.error("删除实体标签关联异常: entityId={}", entityId, e);
            throw e;
        }
    }

    @Override
    public List<Long> getEntityIdsByTagId(Long tagId) {
        if (tagId == null) {
            log.warn("获取实体列表失败: 标签ID为空");
            return Collections.emptyList();
        }
        
        log.info("根据标签获取实体列表: tagId={}", tagId);
        
        List<EntityTag> entityTags = this.list(new LambdaQueryWrapper<EntityTag>()
                .eq(EntityTag::getTagId, tagId));
        
        List<Long> entityIds = entityTags.stream()
                .map(EntityTag::getEntityId)
                .distinct()
                .collect(Collectors.toList());
        
        log.info("标签关联实体数量: tagId={}, entityCount={}", tagId, entityIds.size());
        
        return entityIds;
    }
} 