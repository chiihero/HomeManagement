package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.SpaceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 空间管理页面控制器
 */
@Controller
@RequestMapping("/spaces")
public class SpaceViewController {

    @Autowired
    private SpaceService spaceService;

    /**
     * 空间管理页面
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

        // 获取当前家庭ID
        Long familyId = (Long) session.getAttribute("currentFamilyId");
        if (familyId == null) {
            // 如果未选择家庭，重定向到家庭选择页面
            return "redirect:/";
        }

        // 添加模型属性
        model.addAttribute("user", user);
        model.addAttribute("familyId", familyId);

        return "spaces/index";
    }
}