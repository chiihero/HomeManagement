package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.Entity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 实体Mapper接口
 */
@Mapper
public interface EntityMapper extends BaseMapper<Entity> {

    /**
     * 查询实体的子实体列表
     *
     * @param parentId 父实体ID
     * @param userId 用户ID
     * @return 子实体列表
     */
    @Select("SELECT * FROM entity WHERE parent_id = #{parentId} AND user_id = #{userId}")
    List<Entity> listChildren(@Param("parentId") Long parentId, @Param("userId") Long userId);
    
    /**
     * 根据标签ID查询实体列表
     *
     * @param tagId 标签ID
     * @param userId 用户ID
     * @return 实体列表
     */
    @Select("SELECT e.* FROM entity e " +
            "INNER JOIN entity_tag et ON e.id = et.entity_id " +
            "WHERE et.tag_id = #{tagId} AND e.user_id = #{userId}")
    List<Entity> listEntitiesByTagId(@Param("tagId") Long tagId, @Param("userId") Long userId);
} 