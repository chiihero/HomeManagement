package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.common.ErrorCode;
import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Tag;
import com.chii.homemanagement.exception.BusinessException;
import com.chii.homemanagement.mapper.TagMapper;
import com.chii.homemanagement.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 标签服务实现类
 */
@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public IPage<Tag> pageTags(Page<Tag> page, Long userId) {
        if (userId == null) {
            log.error("分页查询实体时用户ID为空");
            throw new BusinessException(ErrorCode.PARAM_NOT_VALID.getCode(), "用户ID不能为空");
        }

        log.info("开始分页查询实体: userId={}, page={}, size={}", userId, page.getCurrent(), page.getSize());

        // 默认分页参数
        page = Optional.ofNullable(page).orElse(new Page<>(1, 10));

        // 构建查询条件
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, userId)
                .orderByDesc(Tag::getCreateTime);


        IPage<Tag> result = page(page, queryWrapper);
        log.info("分页查询实体完成: 总记录数={}, 总页数={}", result.getTotal(), result.getPages());

        return result;
    }

    @Override
    public List<Tag> getTagsByItemId(Long itemId) {
        return tagMapper.getTagsByItemId(itemId);
    }

    @Override
    public  List<Tag> getTagsByUserId(Long userId){
        return tagMapper.getTagsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTag(Tag tag) {
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        tag.setCreateUserId(tag.getUserId());
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
        return removeById(id);

    }
} 