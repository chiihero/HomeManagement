package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.Family;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.FamilyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 家庭管理页面控制器
 */
@Controller
@RequestMapping("/families")
public class FamilyViewController {

    @Autowired
    private FamilyService familyService;

    /**
     * 家庭管理页面
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

        // 获取用户的家庭列表
        List<Family> families = familyService.getUserFamilies(user.getId());
        model.addAttribute("families", families);
        model.addAttribute("user", user);

        return "families/index";
    }

    /**
     * 家庭详情页面
     *
     * @param id      家庭ID
     * @param model   模型
     * @param session 会话
     * @return 视图名称
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        // 获取家庭详情
        Family family = familyService.getFamilyById(id);

        // 获取家庭成员
        List<User> members = familyService.getFamilyMembers(id);

        // 设置当前家庭ID到会话中
        session.setAttribute("currentFamilyId", id);

        model.addAttribute("family", family);
        model.addAttribute("members", members);
        model.addAttribute("user", user);

        return "families/detail";
    }

    /**
     * 创建家庭页面
     *
     * @param model   模型
     * @param session 会话
     * @return 视图名称
     */
    @GetMapping("/create")
    public String createForm(Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);
        return "families/create";
    }
} 