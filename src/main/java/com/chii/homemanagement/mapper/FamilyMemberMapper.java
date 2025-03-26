package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 家庭成员Mapper接口
 */
@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {

    /**
     * 查询家庭成员
     *
     * @param familyId 家庭ID
     * @return 成员列表
     */
    List<FamilyMember> findByFamilyId(@Param("familyId") Long familyId);

    /**
     * 查询用户所属的家庭成员记录
     *
     * @param userId 用户ID
     * @return 成员列表
     */
    List<FamilyMember> findByUserId(@Param("userId") Long userId);
} 