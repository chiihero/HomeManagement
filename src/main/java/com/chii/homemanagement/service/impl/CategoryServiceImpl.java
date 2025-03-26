// CategoryServiceImpl.java
package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Category;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.mapper.CategoryMapper;
import com.chii.homemanagement.service.CategoryService;
import com.chii.homemanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private UserService userService;

    @Override
    public List<Category> getCategoriesByFamilyId(Long familyId) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getFamilyId, familyId);
        return list(queryWrapper);
    }

    @Override
    public Category getCategoryById(Long id) {
        return getById(id);
    }

    @Override
    @Transactional
    public boolean addCategory(Category category) {
        // 设置创建者ID，如果未设置
        if (category.getCreateUserId() == null) {
            // 从Spring Security获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                try {
                    // 尝试获取当前用户
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof User) {
                        User user = (User) principal;
                        category.setCreateUserId(user.getId());
                    } else {
                        // 如果不是User对象，使用默认ID
                        category.setCreateUserId(1L);
                    }
                } catch (Exception e) {
                    // 如果出现异常，使用默认值
                    category.setCreateUserId(1L);
                }
            } else {
                // 如果未认证，使用默认ID
                category.setCreateUserId(1L);
            }
        }
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        category.setCreateTime(now);
        category.setUpdateTime(now);
        
        return save(category);
    }

    @Override
    @Transactional
    public boolean updateCategory(Category category) {
        // 设置更新时间
        category.setUpdateTime(LocalDateTime.now());
        return updateById(category);
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        return removeById(id);
    }
}
