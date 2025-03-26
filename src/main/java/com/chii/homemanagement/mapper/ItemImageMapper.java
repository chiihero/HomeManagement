package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.ItemImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物品图片Mapper接口
 */
@Mapper
public interface ItemImageMapper extends BaseMapper<ItemImage> {

    /**
     * 查询指定物品的所有图片
     *
     * @param itemId 物品ID
     * @return 图片列表
     */
    List<ItemImage> findByItemId(@Param("itemId") Long itemId);

    /**
     * 查询指定物品的指定类型图片
     *
     * @param itemId 物品ID
     * @param type   图片类型
     * @return 图片列表
     */
    List<ItemImage> findByItemIdAndType(@Param("itemId") Long itemId, @Param("type") String type);

    /**
     * 删除指定物品的所有图片
     *
     * @param itemId 物品ID
     * @return 影响行数
     */
    int deleteByItemId(@Param("itemId") Long itemId);
} 