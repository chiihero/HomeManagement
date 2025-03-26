package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.ItemService;
import com.chii.homemanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 物品视图控制器
 */
@Controller
@RequestMapping("/items")
public class ItemViewController {

    @Autowired
    private ItemService itemService;
    
    @Autowired
    private UserService userService;

    /**
     * 物品管理页面
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
        
        // 获取当前家庭ID
        Long familyId = (Long) session.getAttribute("currentFamilyId");
        if (familyId == null) {
            // 如果未选择家庭，设置默认家庭ID
            familyId = 1L;
            session.setAttribute("currentFamilyId", familyId);
        }
        
        // 添加用户信息到模型
        model.addAttribute("user", user);
        
        return "items/index";
    }
    
    /**
     * 物品编辑页面
     */
    @GetMapping("/edit")
    public String edit(Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // 添加用户信息到模型
        model.addAttribute("user", user);
        
        return "items/edit";
    }
} 