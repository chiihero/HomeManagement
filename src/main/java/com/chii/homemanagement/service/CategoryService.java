package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 根据家庭ID获取分类列表
     *
     * @param familyId 家庭ID
     * @return 分类列表
     */
    List<Category> getCategoriesByFamilyId(Long familyId);

    /**
     * 获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    Category getCategoryById(Long id);

    /**
     * 新增分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    boolean addCategory(Category category);

    /**
     * 更新分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    boolean updateCategory(Category category);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);
} 