package com.chii.homemanagement.service;

import com.chii.homemanagement.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends UserDetailsService {

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User getUserById(Long id);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);

    /**
     * 创建用户
     *
     * @param user 用户对象
     * @return 创建后的用户对象
     */
    User createUser(User user);

    /**
     * 更新用户
     *
     * @param user 用户对象
     * @return 更新后的用户对象
     */
    User updateUser(User user);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long id);

    /**
     * 获取所有用户
     *
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 验证用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 验证密码是否正确
     * @param username 用户名
     * @param password 密码
     * @return 是否正确
     */
    boolean validatePassword(String username, String password);
    
    /**
     * 根据邮箱获取用户
     *
     * @param email 用户邮箱
     * @return 用户对象
     */
    User getUserByEmail(String email);
    
    /**
     * 生成密码重置令牌
     *
     * @param user 用户对象
     * @return 重置令牌
     */
    String generatePasswordResetToken(User user);
    
    /**
     * 验证密码重置令牌
     *
     * @param token 重置令牌
     * @return 用户对象，如令牌无效则返回null
     */
    User validatePasswordResetToken(String token);
    
    /**
     * 重置用户密码
     *
     * @param user 用户对象
     * @param newPassword 新密码
     */
    void resetPassword(User user, String newPassword);

    /**
     * 上传当前登录用户的头像
     *
     * @param id 用户id
     * @param file 头像
     * @return 用户头像url
     */
    User uploadAvatar(Long id, MultipartFile file);

    /**
     * 删除当前登录用户的头像
     *
     * @param id 用户id
     */
    User deeleteAvatar(Long id);

} 