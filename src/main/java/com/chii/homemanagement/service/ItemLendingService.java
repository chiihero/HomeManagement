package com.chii.homemanagement.service;

import com.chii.homemanagement.entity.ItemLending;

import java.time.LocalDate;
import java.util.List;

/**
 * 物品借用服务接口
 */
public interface ItemLendingService {

    /**
     * 创建借用记录
     *
     * @param itemLending 借用记录
     * @return 创建后的借用记录
     */
    ItemLending createItemLending(ItemLending itemLending);

    /**
     * 更新借用记录
     *
     * @param itemLending 借用记录
     * @return 更新后的借用记录
     */
    ItemLending updateItemLending(ItemLending itemLending);

    /**
     * 删除借用记录
     *
     * @param id 借用记录ID
     * @return 是否成功
     */
    boolean deleteItemLending(Long id);

    /**
     * 根据ID查询借用记录
     *
     * @param id 借用记录ID
     * @return 借用记录
     */
    ItemLending getItemLending(Long id);

    /**
     * 获取物品的借用记录
     *
     * @param itemId 物品ID
     * @return 借用记录列表
     */
    List<ItemLending> getItemLendingsByItemId(Long itemId);

    /**
     * 获取指定状态的借用记录
     *
     * @param status 状态
     * @return 借用记录列表
     */
    List<ItemLending> getItemLendingsByStatus(String status);

    /**
     * 获取未归还的借用记录
     *
     * @return 借用记录列表
     */
    List<ItemLending> getUnreturnedItemLendings();

    /**
     * 记录归还
     *
     * @param id               借用记录ID
     * @param actualReturnDate 实际归还日期
     * @return 更新后的借用记录
     */
    ItemLending returnItem(Long id, LocalDate actualReturnDate);

    /**
     * 检查并更新借用状态
     */
    void checkAndUpdateLendingStatus();
} 