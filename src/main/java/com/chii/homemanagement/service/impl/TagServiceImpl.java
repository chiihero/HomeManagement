package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Tag;
import com.chii.homemanagement.mapper.TagMapper;
import com.chii.homemanagement.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签服务实现类
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<Tag> getTagsByItemId(Long itemId) {
        return tagMapper.getTagsByItemId(itemId);
    }

    @Override
    public  List<Tag> getTagsByOwnerId(Long ownerId){
        return tagMapper.getTagsByOwnerId(ownerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTag(Tag tag) {
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        tag.setCreateTime(now);
        tag.setUpdateTime(now);
        
        // 如果没有设置颜色，默认为蓝色
        if (tag.getColor() == null || tag.getColor().isEmpty()) {
            tag.setColor("#409EFF");
        }
        
        // 保存标签
        save(tag);
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTag(Tag tag) {
        // 设置更新时间
        tag.setUpdateTime(LocalDateTime.now());
        return updateById(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTag(Long id) {
        return true;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setItemTags(Long itemId, List<Long> tagIds) {
    return true;
    }
} 