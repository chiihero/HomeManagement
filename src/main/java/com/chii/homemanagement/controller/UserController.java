package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ResponseInfo<User> getCurrentUser(HttpSession session) {
        // 从session中获取用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                user = userService.getUserByUsername(username);
                if (user != null) {
                    // 将用户存入session
                    session.setAttribute("user", user);
                    // 清除密码
                    user.setPassword(null);
                }
            }
        } else {
            // 清除密码
            user.setPassword(null);
        }

        if (user == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }

        return ResponseInfo.successResponse(user);
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    @Operation(summary = "更新个人资料", description = "更新当前登录用户的个人资料")
    public ResponseInfo<User> updateProfile(@RequestBody User userParam, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }

        // 更新个人资料
        User user = new User();
        user.setId(currentUser.getId());
        // 只允许更新以下字段
        user.setEmail(userParam.getEmail());
        user.setPhone(userParam.getPhone());
        user.setUpdateTime(LocalDateTime.now());

        try {
            userService.updateUser(user);
            // 更新session中的用户信息
            User updatedUser = userService.getUserById(currentUser.getId());
            session.setAttribute("user", updatedUser);
            // 清除密码
            updatedUser.setPassword(null);
            return ResponseInfo.successResponse(updatedUser);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("更新个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新密码
     */
    @PutMapping("/password")
    @Operation(summary = "更新密码", description = "更新当前登录用户的密码")
    public ResponseInfo<Boolean> updatePassword(@RequestBody Map<String, String> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }

        String currentPassword = params.get("currentPassword");
        String newPassword = params.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseInfo.errorResponse("当前密码和新密码不能为空");
        }

        // 验证当前密码
        if (!userService.validatePassword(currentUser.getUsername(), currentPassword)) {
            return ResponseInfo.errorResponse("当前密码不正确");
        }

        // 更新密码
        User user = new User();
        user.setId(currentUser.getId());
        user.setPassword(newPassword);
        user.setUpdateTime(LocalDateTime.now());

        try {
            userService.updateUser(user);
            return ResponseInfo.successResponse(true);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("更新密码失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传当前登录用户的头像")
    public ResponseInfo<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }

        // TODO: 实现头像上传逻辑，保存文件并返回URL
        // 此处简化处理，实际应该保存文件并设置URL

        return ResponseInfo.errorResponse("头像上传功能暂未实现");
    }

    /**
     * 删除头像
     */
    @DeleteMapping("/avatar")
    @Operation(summary = "删除头像", description = "删除当前登录用户的头像")
    public ResponseInfo<Boolean> deleteAvatar(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }

        // 更新用户信息，清空头像URL
        User user = new User();
        user.setId(currentUser.getId());
        user.setAvatar(null);
        user.setUpdateTime(LocalDateTime.now());

        try {
            userService.updateUser(user);
            // 更新session中的用户信息
            User updatedUser = userService.getUserById(currentUser.getId());
            session.setAttribute("user", updatedUser);
            return ResponseInfo.successResponse(true);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("删除头像失败: " + e.getMessage());
        }
    }

    /**
     * 更新通知设置
     */
    @PutMapping("/notifications")
    @Operation(summary = "更新通知设置", description = "更新当前登录用户的通知设置")
    public ResponseInfo<Boolean> updateNotifications(@RequestBody Map<String, Object> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseInfo.errorResponse("未登录或登录已过期");
        }

        // TODO: 实现通知设置逻辑
        // 此处简化处理，实际应该保存到用户设置表

        return ResponseInfo.successResponse(true);
    }
} 