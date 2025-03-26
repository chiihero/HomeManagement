package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.Family;
import com.chii.homemanagement.entity.FamilyMember;
import com.chii.homemanagement.entity.User;

import java.util.List;

/**
 * 家庭服务接口
 */
public interface FamilyService extends IService<Family> {

    /**
     * 创建家庭
     *
     * @param family    家庭信息
     * @param creatorId 创建者用户ID
     * @return 创建后的家庭
     */
    Family createFamily(Family family, Long creatorId);

    /**
     * 更新家庭信息
     *
     * @param family 家庭信息
     * @return 更新后的家庭
     */
    Family updateFamily(Family family);

    /**
     * 删除家庭
     *
     * @param id 家庭ID
     * @return 是否成功
     */
    boolean deleteFamily(Long id);

    /**
     * 获取家庭详情
     *
     * @param id 家庭ID
     * @return 家庭详情
     */
    Family getFamilyById(Long id);

    /**
     * 获取用户的所有家庭
     *
     * @param userId 用户ID
     * @return 家庭列表
     */
    List<Family> getUserFamilies(Long userId);

    /**
     * 添加家庭成员
     *
     * @param familyId 家庭ID
     * @param userId   用户ID
     * @param role     角色
     * @return 添加的家庭成员
     */
    FamilyMember addFamilyMember(Long familyId, Long userId, String role);

    /**
     * 移除家庭成员
     *
     * @param familyId 家庭ID
     * @param userId   用户ID
     * @return 是否成功
     */
    boolean removeFamilyMember(Long familyId, Long userId);

    /**
     * 获取家庭成员列表
     *
     * @param familyId 家庭ID
     * @return 成员列表
     */
    List<User> getFamilyMembers(Long familyId);
} 