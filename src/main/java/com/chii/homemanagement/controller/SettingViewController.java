package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.User;
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
 * 系统设置视图控制器
 */
@Controller
@RequestMapping("/settings")
public class SettingViewController {
    
    @Autowired
    private UserService userService;

    /**
     * 系统设置页面
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
        
        // 添加用户信息到模型
        model.addAttribute("user", user);
        
        return "settings/index";
    }
    
    /**
     * 个人资料设置页面
     */
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // 添加用户信息到模型
        model.addAttribute("user", user);
        
        return "settings/profile";
    }
    
    /**
     * 系统参数设置页面
     */
    @GetMapping("/system")
    public String system(Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // 添加用户信息到模型
        model.addAttribute("user", user);
        
        return "settings/system";
    }
} 