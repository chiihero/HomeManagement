package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.CategoryService;
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
 * 首页控制器
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserService userService;

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

        // 获取当前家庭ID
        Long familyId = (Long) session.getAttribute("currentFamilyId");
        if (familyId == null) {
            // 如果用户还没选择家庭，设置一个默认家庭ID（可以从数据库获取）
            familyId = 1L; // 默认家庭ID
            session.setAttribute("currentFamilyId", familyId);
        }

        // 物品总数
        int itemCount = itemService.getItemsByFamilyId(familyId).size();
        model.addAttribute("itemCount", itemCount);

        // 物品总价值
        double totalValue = itemService.sumItemsValue(familyId);
        model.addAttribute("totalValue", totalValue);

        // 分类数量
        int categoryCount = categoryService.list().size();
        model.addAttribute("categoryCount", categoryCount);

        // 添加用户信息
        model.addAttribute("user", user);

        return "index";
    }
} 