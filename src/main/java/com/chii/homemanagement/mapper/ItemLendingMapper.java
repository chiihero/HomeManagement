package com.chii.homemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chii.homemanagement.entity.ItemLending;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物品借用记录Mapper接口
 */
@Mapper
public interface ItemLendingMapper extends BaseMapper<ItemLending> {

    /**
     * 查询指定物品的借用记录
     *
     * @param itemId 物品ID
     * @return 借用记录列表
     */
    List<ItemLending> findByItemId(@Param("itemId") Long itemId);

    /**
     * 查询指定状态的借用记录
     *
     * @param status 状态
     * @return 借用记录列表
     */
    List<ItemLending> findByStatus(@Param("status") String status);

    /**
     * 查询未归还记录
     *
     * @return 未归还记录列表
     */
    List<ItemLending> findUnreturnedItems();

    /**
     * 更新借用记录状态
     *
     * @param id     借用记录ID
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
} 