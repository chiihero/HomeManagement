package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.entity.ResultCode;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.UserService;
import com.chii.homemanagement.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户登录、注册等认证相关接口")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口，返回用户信息和token")
    public ResponseInfo<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
            String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || password == null) {
            return ResponseInfo.response(ResultCode.PARAM_NOT_COMPLETE);
        }
        
        try {
            // 使用Spring Security进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // 如果没有抛出异常，则认证成功
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取认证用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 获取用户实体信息
            User user = userService.getUserByUsername(userDetails.getUsername());
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(userDetails.getUsername());
            
            // 准备返回数据
            Map<String, Object> responseData = new HashMap<>();
            
            // 用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            
            // 返回数据包括token和用户信息
            responseData.put("token", token);
            responseData.put("user", userInfo);
            responseData.put("loginTime", LocalDateTime.now());
            
            // 返回成功响应
            return ResponseInfo.successResponse(responseData);
            
        } catch (DisabledException e) {
            // 账号被禁用
            return ResponseInfo.response(ResultCode.USER_ACCOUNT_DISABLE);
        } catch (LockedException e) {
            // 账号被锁定
            return ResponseInfo.response(ResultCode.USER_ACCOUNT_LOCKED);
        } catch (BadCredentialsException e) {
            // 用户名或密码错误
            return ResponseInfo.response(ResultCode.USER_CREDENTIALS_ERROR);
        } catch (Exception e) {
            // 其他异常
            return ResponseInfo.errorResponse("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    @PostMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ResponseInfo<Map<String, Object>> getUserInfo() {
        try {
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseInfo.response(ResultCode.USER_NOT_LOGIN);
            }
            
            // 获取用户名
            String username = authentication.getName();
            
            // 获取用户实体信息
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return ResponseInfo.response(ResultCode.USER_ACCOUNT_NOT_EXIST);
            }
            
            // 准备返回数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            
            return ResponseInfo.successResponse(userInfo);
        } catch (Exception e) {
            return ResponseInfo.errorResponse("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public ResponseInfo<Void> logout() {
        try {
            // 清除当前认证信息
            SecurityContextHolder.clearContext();
            return ResponseInfo.successResponse();
        } catch (Exception e) {
            return ResponseInfo.errorResponse("登出失败: " + e.getMessage());
        }
    }
} 