package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;

import java.util.List;

/**
 * 标签Mapper接口
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
   
    /**
     * 查询物品关联的标签
     * 
     * @param itemId 物品ID
     * @return 标签列表
     */
    @Select("SELECT t.id, t.name, t.color, t.user_id, t.create_user_id, t.create_time, t.update_time " +
            "FROM tag t " +
            "INNER JOIN item_tag it ON t.id = it.tag_id " +
            "WHERE it.item_id = #{itemId}")
    List<Tag> getTagsByItemId(@Param("itemId") Long itemId);
    
    /**
     * 查询所有者的所有标签
     *
     * @param userId 用户ID
     * @return 标签列表
     */
    @Select("SELECT id, name, color, user_id, create_user_id, create_time, update_time " +
            "FROM tag " +
            "WHERE user_id = #{userId} " +
            "ORDER BY id")
    List<Tag> getTagsByUserId(@Param("userId") Long userId);
} 