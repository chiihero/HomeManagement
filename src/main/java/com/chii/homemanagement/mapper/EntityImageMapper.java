package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.EntityImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;

import java.util.List;

/**
 * 实体图片Mapper接口
 */
@Mapper
public interface EntityImageMapper extends BaseMapper<EntityImage> {

    /**
     * 获取实体的所有图片
     *
     * @param entityId 实体ID
     * @return 图片列表
     */
    @Select("SELECT * FROM entity_image WHERE entity_id = #{entityId} ORDER BY sort_order ASC")
    List<EntityImage> listByEntityId(@Param("entityId") Long entityId);

    /**
     * 根据实体ID删除所有图片
     *
     * @param entityId 实体ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM entity_image WHERE entity_id = #{entityId}")
    int deleteByEntityId(@Param("entityId") Long entityId);
} 