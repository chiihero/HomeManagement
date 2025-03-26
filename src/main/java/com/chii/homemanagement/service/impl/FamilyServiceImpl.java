package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Family;
import com.chii.homemanagement.entity.FamilyMember;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.exception.BusinessException;
import com.chii.homemanagement.mapper.FamilyMapper;
import com.chii.homemanagement.mapper.FamilyMemberMapper;
import com.chii.homemanagement.mapper.UserMapper;
import com.chii.homemanagement.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 家庭服务实现类
 */
@Service
public class FamilyServiceImpl extends ServiceImpl<FamilyMapper, Family> implements FamilyService {

    @Autowired
    private FamilyMapper familyMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Family createFamily(Family family, Long creatorId) {
        // 检查创建者是否存在
        User creator = userMapper.selectById(creatorId);
        if (creator == null) {
            throw new BusinessException("创建者用户不存在");
        }

        // 设置创建时间
        family.setCreateTime(LocalDateTime.now());
        family.setUpdateTime(LocalDateTime.now());

        // 保存家庭信息
        save(family);

        // 将创建者添加为家庭成员（管理员角色）
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(creatorId);
        member.setRole("admin");
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        familyMemberMapper.insert(member);

        return family;
    }

    @Override
    @Transactional
    public Family updateFamily(Family family) {
        Family existingFamily = getById(family.getId());
        if (existingFamily == null) {
            throw new BusinessException("家庭不存在");
        }

        family.setUpdateTime(LocalDateTime.now());
        updateById(family);

        return family;
    }

    @Override
    @Transactional
    public boolean deleteFamily(Long id) {
        // 检查家庭是否存在
        if (!checkFamilyExists(id)) {
            throw new BusinessException("家庭不存在");
        }

        // 删除家庭成员关联
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getFamilyId, id);
        familyMemberMapper.delete(memberWrapper);

        // 删除家庭
        return removeById(id);
    }

    @Override
    public Family getFamilyById(Long id) {
        Family family = getById(id);
        if (family == null) {
            throw new BusinessException("家庭不存在");
        }

        return family;
    }

    @Override
    public List<Family> getUserFamilies(Long userId) {
        // 查询用户的家庭成员记录
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        List<FamilyMember> members = familyMemberMapper.selectList(memberWrapper);

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取家庭ID列表
        List<Long> familyIds = members.stream()
                .map(FamilyMember::getFamilyId)
                .collect(Collectors.toList());

        // 查询家庭信息
        LambdaQueryWrapper<Family> familyWrapper = new LambdaQueryWrapper<>();
        familyWrapper.in(Family::getId, familyIds);

        return list(familyWrapper);
    }

    @Override
    @Transactional
    public FamilyMember addFamilyMember(Long familyId, Long userId, String role) {
        // 检查家庭是否存在
        if (!checkFamilyExists(familyId)) {
            throw new BusinessException("家庭不存在");
        }

        // 检查用户是否存在
        if (!checkUserExists(userId)) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户是否已经是家庭成员
        LambdaQueryWrapper<FamilyMember> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FamilyMember::getFamilyId, familyId).eq(FamilyMember::getUserId, userId);
        if (familyMemberMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("用户已经是该家庭成员");
        }

        // 添加家庭成员
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        member.setRole(role);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        familyMemberMapper.insert(member);

        return member;
    }

    @Override
    @Transactional
    public boolean removeFamilyMember(Long familyId, Long userId) {
        // 检查家庭是否存在
        if (!checkFamilyExists(familyId)) {
            throw new BusinessException("家庭不存在");
        }

        // 检查是否是最后一个管理员
        LambdaQueryWrapper<FamilyMember> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(FamilyMember::getFamilyId, familyId).eq(FamilyMember::getRole, "admin");
        List<FamilyMember> admins = familyMemberMapper.selectList(adminWrapper);

        if (admins.size() == 1 && admins.get(0).getUserId().equals(userId)) {
            throw new BusinessException("不能移除最后一个管理员");
        }

        // 删除家庭成员
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getFamilyId, familyId).eq(FamilyMember::getUserId, userId);

        return familyMemberMapper.delete(memberWrapper) > 0;
    }

    @Override
    public List<User> getFamilyMembers(Long familyId) {
        // 检查家庭是否存在
        if (!checkFamilyExists(familyId)) {
            throw new BusinessException("家庭不存在");
        }

        // 查询家庭成员ID
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getFamilyId, familyId);
        List<FamilyMember> members = familyMemberMapper.selectList(memberWrapper);

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取用户ID列表
        List<Long> userIds = members.stream()
                .map(FamilyMember::getUserId)
                .collect(Collectors.toList());

        // 查询用户信息
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(User::getId, userIds);

        return userMapper.selectList(userWrapper);
    }

    /**
     * 检查家庭是否存在
     */
    private boolean checkFamilyExists(Long familyId) {
        return getById(familyId) != null;
    }

    /**
     * 检查用户是否存在
     */
    private boolean checkUserExists(Long userId) {
        return userMapper.selectById(userId) != null;
    }
} 