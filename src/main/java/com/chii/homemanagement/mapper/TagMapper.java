package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT t.* FROM tag t " +
            "INNER JOIN item_tag it ON t.id = it.tag_id " +
            "WHERE it.item_id = #{itemId}")
    List<Tag> getTagsByItemId(@Param("itemId") Long itemId);
    
    /**
     * 查询所有者的所有标签
     * 
     * @param ownerId 所有者ID
     * @return 标签列表
     */
    @Select("SELECT * FROM tag WHERE owner_id = #{ownerId}")
    List<Tag> getTagsByOwnerId(@Param("ownerId") Long ownerId);
} 