package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/**
 * 认证控制器
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    /**
     * 处理注册请求
     */
    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String email,
            Model model) {

        // 验证用户名是否已存在
        if (userService.isUsernameExists(username)) {
            model.addAttribute("error", "用户名已存在");
            return "auth/register";
        }

        // 验证密码是否匹配
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致");
            return "auth/register";
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 密码会在service层加密
        user.setEmail(email);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userService.createUser(user);

        // 注册成功，重定向到登录页面
        return "redirect:/auth/login?registered";
    }
} 