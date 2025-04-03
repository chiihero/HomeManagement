package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Tag;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.EntityService;
import com.chii.homemanagement.service.EntityTagService;
import com.chii.homemanagement.service.TagService;
import com.chii.homemanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体视图控制器
 */
@Controller
@RequestMapping("/entities")
@Slf4j
public class EntityViewController {

    @Autowired
    private EntityService entityService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private EntityTagService entityTagService;

    /**
     * 实体管理主页面
     */
    @GetMapping
    public String index(Model model, HttpSession session) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // 将用户信息放入session
        session.setAttribute("user", user);
        
        // 获取当前用户ID
        Long userId = (Long) session.getAttribute("currentuserId");
        if (userId == null) {
            // 如果未选择所有者，设置默认用户ID
            userId = 1L;
            session.setAttribute("currentuserId", userId);
        }
        
        // 获取根实体（空间和顶级物品）
        List<Entity> rootEntities = entityService.getEntityTree(userId);
        model.addAttribute("rootEntities", rootEntities);
        
        // 添加用户信息和用户ID到模型
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        
        return "entities/index";
    }
    
    /**
     * 实体编辑页面（创建和修改共用）
     */
    @GetMapping("/edit")
    public String edit(Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // 获取当前用户ID
        Long userId = (Long) session.getAttribute("currentuserId");
        if (userId == null) {
            return "redirect:/";
        }
        
        // 获取所有可用的父实体
        List<Entity> parentEntities = entityService.getEntityTree(userId);
        model.addAttribute("parentEntities", parentEntities);
        
        // 获取所有标签
        List<Tag> tags = tagService.getTagsByUserId(userId);
        model.addAttribute("tags", tags);
        
        // 添加用户信息到模型
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        
        // 添加空白实体对象，用于新增实体的情况
        Entity entity = new Entity();
        // 设置默认值
        entity.setType("item"); // 默认为物品类型
        entity.setQuantity(1);
        entity.setPrice(new java.math.BigDecimal("0.00"));
        entity.setStatus("normal");
        entity.setUsageFrequency("rarely");
        
        model.addAttribute("entity", entity);
        
        return "entities/edit";
    }

    /**
     * 实体编辑页面（已存在实体）
     */
    @GetMapping("/edit/{id}")
    public String editEntity(@PathVariable Long id, Model model, HttpSession session) {
        try {
            // 获取当前用户
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/auth/login";
            }
            
            // 获取当前用户ID
            Long userId = (Long) session.getAttribute("currentuserId");
            if (userId == null) {
                return "redirect:/";
            }
            
            // 获取实体详情
            Entity entity = entityService.getEntityDetail(id);
            model.addAttribute("entity", entity);
            
            // 获取所有可用的父实体（排除当前实体及其子实体）
            List<Entity> parentEntities = entityService.getEntityTree(userId);
            model.addAttribute("parentEntities", parentEntities);
            
            // 获取所有标签
            List<Tag> tags = tagService.getTagsByUserId(userId);
            model.addAttribute("tags", tags);
            
            // 获取实体的标签
            List<Tag> entityTags = entityTagService.getTagsByEntityId(id);
            model.addAttribute("entityTags", entityTags);
            
            // 添加用户信息到模型
            model.addAttribute("user", user);
            model.addAttribute("userId", userId);
            
            return "entities/edit";
        } catch (Exception e) {
            log.error("编辑实体页异常", e);
            model.addAttribute("errorMessage", "获取实体编辑信息失败：" + e.getMessage());
            return "error";
        }
    }
    
    /**
     * 获取实体编辑表单片段（用于在AJAX请求中加载编辑表单）
     */
    @GetMapping("/edit-form/{id}")
    public String getEntityEditForm(@PathVariable Long id, Model model, HttpSession session) {
        try {
            // 获取当前用户
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/auth/login";
            }
            
            // 获取当前用户ID
            Long userId = (Long) session.getAttribute("currentuserId");
            if (userId == null) {
                return "redirect:/";
            }
            
            // 获取实体详情
            Entity entity = entityService.getEntityDetail(id);
            model.addAttribute("entity", entity);
            
            // 获取实体的标签ID列表
            List<Tag> entityTags = entityTagService.getTagsByEntityId(id);
            List<Long> tagIds = entityTags.stream().map(Tag::getId).toList();
            entity.setTagIds(tagIds);
            
            // 添加用户ID到模型
            model.addAttribute("userId", userId);
            
            // 返回片段视图
            return "entities/edit :: editForm(entity=${entity})";
        } catch (Exception e) {
            log.error("获取实体编辑表单片段异常", e);
            return "error";
        }
    }
    
    /**
     * 获取添加实体表单片段（用于在AJAX请求中加载添加表单）
     */
    @GetMapping("/add-form")
    public String getEntityAddForm(Model model, HttpSession session) {
        try {
            // 获取当前用户
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/auth/login";
            }
            
            // 获取当前用户ID
            Long userId = (Long) session.getAttribute("currentuserId");
            if (userId == null) {
                return "redirect:/";
            }
            
            // 创建一个新的实体对象，用于添加
            Entity entity = new Entity();
            // 设置默认值
            entity.setType("item"); // 默认为物品类型
            entity.setQuantity(1);
            entity.setPrice(new java.math.BigDecimal("0.00"));
            entity.setStatus("normal");
            entity.setUsageFrequency("rarely");
            entity.setTagIds(new ArrayList<>());
            
            model.addAttribute("entity", entity);
            
            // 添加用户ID到模型
            model.addAttribute("userId", userId);
            
            // 返回片段视图
            return "entities/edit :: editForm(entity=${entity})";
        } catch (Exception e) {
            log.error("获取实体添加表单片段异常", e);
            return "error";
        }
    }
} 