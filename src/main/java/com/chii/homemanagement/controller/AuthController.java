package com.chii.homemanagement.controller;

import com.chii.homemanagement.common.ApiResponse;
import com.chii.homemanagement.common.ErrorCode;
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
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || password == null) {
            return ApiResponse.error(ErrorCode.PARAM_NOT_COMPLETE.getCode(), ErrorCode.PARAM_NOT_COMPLETE.getMessage());
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
            
            // 生成刷新令牌
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());
            
            // 准备返回数据
            Map<String, Object> responseData = new HashMap<>();
            
            // 用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            
            // 返回数据包括token和用户信息
            responseData.put("token", token);
            responseData.put("refreshToken", refreshToken);
            responseData.put("expiresIn", jwtUtil.getTokenExpiresIn());
            responseData.put("user", userInfo);
            responseData.put("loginTime", LocalDateTime.now());
            
            // 返回成功响应
            return ApiResponse.success(responseData);
            
        } catch (DisabledException e) {
            // 账号被禁用
            return ApiResponse.error(ErrorCode.USER_ACCOUNT_DISABLE.getCode(), ErrorCode.USER_ACCOUNT_DISABLE.getMessage());
        } catch (LockedException e) {
            // 账号被锁定
            return ApiResponse.error(ErrorCode.USER_ACCOUNT_LOCKED.getCode(), ErrorCode.USER_ACCOUNT_LOCKED.getMessage());
        } catch (BadCredentialsException e) {
            // 用户名或密码错误
            return ApiResponse.error(ErrorCode.USER_CREDENTIALS_ERROR.getCode(), ErrorCode.USER_CREDENTIALS_ERROR.getMessage());
        } catch (Exception e) {
            // 其他异常
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public ApiResponse<Void> logout() {
        try {
            // 清除当前认证信息
            SecurityContextHolder.clearContext();
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "登出失败: " + e.getMessage());
        }
    }

    /**
     * 刷新Token
     *
     * @param refreshRequest 包含刷新令牌的请求
     * @return 新的访问令牌和刷新令牌
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌和刷新令牌")
    public ApiResponse<Map<String, Object>> refreshToken(@RequestBody Map<String, String> refreshRequest) {
        String refreshTokenValue = refreshRequest.get("refreshToken");
        
        if (refreshTokenValue == null || refreshTokenValue.isEmpty()) {
            return ApiResponse.error(ErrorCode.PARAM_NOT_COMPLETE.getCode(), ErrorCode.PARAM_NOT_COMPLETE.getMessage());
        }
        
        try {
            // 验证刷新令牌
            if (!jwtUtil.validateRefreshToken(refreshTokenValue)) {
                return ApiResponse.error(ErrorCode.REFRESH_TOKEN_INVALID.getCode(), ErrorCode.REFRESH_TOKEN_INVALID.getMessage());
            }
            
            // 获取用户名
            String username = jwtUtil.getUsernameFromToken(refreshTokenValue);
            
            // 检查用户是否存在
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_NOT_EXIST.getCode(), ErrorCode.USER_ACCOUNT_NOT_EXIST.getMessage());
            }
            
            // 生成新的访问令牌和刷新令牌
            String newToken = jwtUtil.generateToken(username);
            String newRefreshToken = jwtUtil.generateRefreshToken(username);
            
            // 准备返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", newToken);
            responseData.put("refreshToken", newRefreshToken);
            responseData.put("expiresIn", jwtUtil.getTokenExpiresIn());
            
            return ApiResponse.success(responseData);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "刷新令牌失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户注册
     *
     * @param registerRequest 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public ApiResponse<Boolean> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");
        String email = registerRequest.get("email");
        
        if (username == null || password == null || email == null) {
            return ApiResponse.error(ErrorCode.PARAM_NOT_COMPLETE.getCode(), ErrorCode.PARAM_NOT_COMPLETE.getMessage());
        }
        
        try {
            // 检查用户名是否已存在
            User existingUser = userService.getUserByUsername(username);
            if (existingUser != null) {
                return ApiResponse.error(ErrorCode.USER_ACCOUNT_ALREADY_EXIST.getCode(), "用户名已存在");
            }
            
            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // Service层会处理密码加密
            user.setEmail(email);
            user.setRole("USER"); // 默认角色
            user.setStatus("active"); // 默认状态
            user.setCreateTime(LocalDateTime.now());
            
            userService.createUser(user);
            
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "注册失败: " + e.getMessage());
        }
    }
    
    /**
     * 忘记密码发送邮件
     *
     * @param emailRequest 包含邮箱的请求
     * @return 操作结果
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "发送重置密码邮件")
    public ApiResponse<Boolean> forgotPassword(@RequestBody Map<String, String> emailRequest) {
        String email = emailRequest.get("email");
        
        if (email == null || email.isEmpty()) {
            return ApiResponse.error(ErrorCode.PARAM_NOT_COMPLETE.getCode(), ErrorCode.PARAM_NOT_COMPLETE.getMessage());
        }
        
        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                // 出于安全考虑，即使用户不存在也返回成功
                return ApiResponse.success(true);
            }
            
            // TODO: 生成重置令牌并发送邮件
            String resetToken = userService.generatePasswordResetToken(user);
            // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "发送重置密码邮件失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置密码
     *
     * @param resetRequest 包含重置令牌和新密码的请求
     * @return 操作结果
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "使用重置令牌重置密码")
    public ApiResponse<Boolean> resetPassword(@RequestBody Map<String, String> resetRequest) {
        String token = resetRequest.get("token");
        String newPassword = resetRequest.get("newPassword");
        
        if (token == null || newPassword == null) {
            return ApiResponse.error(ErrorCode.PARAM_NOT_COMPLETE.getCode(), ErrorCode.PARAM_NOT_COMPLETE.getMessage());
        }
        
        try {
            // 验证重置令牌
            User user = userService.validatePasswordResetToken(token);
            if (user == null) {
                return ApiResponse.error(ErrorCode.REFRESH_TOKEN_INVALID.getCode(), "无效的重置令牌或令牌已过期");
            }
            
            // 更新密码
            userService.resetPassword(user, newPassword);
            
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "重置密码失败: " + e.getMessage());
        }
    }
} 