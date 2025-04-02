package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.EntityService;
import com.chii.homemanagement.service.TagService;
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
 * 首页控制器
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private TagService tagService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EntityService entityService;

    /**
     * 首页
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
        
        // 将用户信息放入 session
        session.setAttribute("user", user);

        // 获取当前所有者ID
        Long ownerId = (Long) session.getAttribute("currentownerId");
        if (ownerId == null) {
            // 如果用户还没选择所有者，设置一个默认所有者ID（可以从数据库获取）
            ownerId = 1L; // 默认所有者ID
            session.setAttribute("currentownerId", ownerId);
        }

        // 实体总数（物品类型）
        int entityCount = entityService.getEntitiesByType(ownerId, "item").size();
        model.addAttribute("itemCount", entityCount);

        // 实体总价值
        double totalValue = entityService.sumEntitiesValue(ownerId);
        model.addAttribute("totalValue", totalValue);

        // 标签数量
        int tagCount = tagService.getTagsByOwnerId(ownerId).size();
        model.addAttribute("tagCount", tagCount);

        // 添加用户信息
        model.addAttribute("user", user);

        return "index";
    }
} 