package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.ResultCode;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.exception.BusinessException;
import com.chii.homemanagement.mapper.UserMapper;
import com.chii.homemanagement.service.UserService;
import com.chii.homemanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // 密码重置令牌有效期（24小时）
    private static final long PASSWORD_RESET_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

    @Autowired
    public UserServiceImpl(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Override
    public User getUserById(Long id) {
        return getById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return getOne(queryWrapper);
    }

    @Override
    public User createUser(User user) {
//        if (!user.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
//            throw new BusinessException(String.valueOf(ResultCode.PARAM_NOT_VALID), "密码需8位以上且包含字母数字");
//        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        save(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        // 如果提供了新密码，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        updateById(user);
        return user;
    }

    @Override
    public boolean deleteUser(Long id) {
        return removeById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return list();
    }

    @Override
    public boolean isUsernameExists(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return count(queryWrapper) > 0;
    }

    @Override
    public boolean validatePassword(String username, String password) {
        // 根据用户名获取用户
        User user = getUserByUsername(username);
        if (user == null) {
            return false;
        }

        // 使用passwordEncoder验证密码
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    @Override
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return getOne(queryWrapper);
    }
    
    @Override
    public String generatePasswordResetToken(User user) {
        // 使用JWT创建密码重置令牌，设置特殊的claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "password_reset");
        claims.put("userId", user.getId().toString());
        claims.put("id", UUID.randomUUID().toString()); // 确保令牌唯一性
        
        // 使用generateToken方法，该方法会使用JwtUtil中的doGenerateToken
        return jwtUtil.generateToken(user.getUsername());
    }
    
    @Override
    public User validatePasswordResetToken(String token) {
        try {
            // 尝试从令牌中获取用户名，如果令牌已过期或无效，这将抛出异常
            String username = jwtUtil.getUsernameFromToken(token);
            
            // 如果能成功获取用户名，则令牌有效，返回对应的用户
            User user = getUserByUsername(username);
            if (user == null) {
                return null;
            }
            
            // 验证令牌是否已过期，通过比较过期时间和当前时间
            Date expiration = jwtUtil.getExpirationDateFromToken(token);
            if (expiration.before(new Date())) {
                return null;
            }
            
            return user;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public void resetPassword(User user, String newPassword) {
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        // 更新用户信息
        updateById(user);
    }
} 