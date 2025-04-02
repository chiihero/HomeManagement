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
public class AuthViewController {

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

} 