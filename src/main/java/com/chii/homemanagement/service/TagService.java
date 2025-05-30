package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService extends IService<Tag> {
    /**
     * 分页查询实体
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<Tag> pageTags(Page<Tag> page,  Long userId);

    /**
     * 获取物品的标签列表
     *
     * @param itemId 物品ID
     * @return 标签列表
     */
    List<Tag> getTagsByItemId(Long itemId);

    /**
     * 获取所有者的所有标签
     *
     * @param userId 用户ID
     * @return 标签列表
     */
    List<Tag> getTagsByUserId(Long userId);

    /**
     * 添加标签
     *
     * @param tag 标签对象
     * @return 标签ID
     */
    Long addTag(Tag tag);

    /**
     * 更新标签
     *
     * @param tag 标签对象
     * @return 是否成功
     */
    boolean updateTag(Tag tag);

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 是否成功
     */
    boolean deleteTag(Long id);

} 