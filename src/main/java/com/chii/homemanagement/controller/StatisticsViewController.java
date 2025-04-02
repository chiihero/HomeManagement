package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 统计报表页面控制器
 */
@Controller
@RequestMapping("/statistics")
public class StatisticsViewController {

    /**
     * 统计报表页面
     *
     * @param model   模型
     * @param session 会话
     * @return 视图名称
     */
    @GetMapping
    public String index(Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        // 获取当前所有者ID
        Long ownerId = (Long) session.getAttribute("currentownerId");
        if (ownerId == null) {
            // 如果未选择所有者，重定向到所有者选择页面
            return "redirect:/";
        }

        // 添加模型属性
        model.addAttribute("user", user);
        model.addAttribute("ownerId", ownerId);

        return "statistics/index";
    }
} 