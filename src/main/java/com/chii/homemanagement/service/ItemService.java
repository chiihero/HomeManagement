package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.Item;

import java.time.LocalDate;
import java.util.List;

/**
 * 物品服务接口
 */
public interface ItemService extends IService<Item> {

    /**
     * 分页查询物品列表
     *
     * @param page     分页参数
     * @param item     查询条件
     * @param familyId 家庭ID
     * @return 分页结果
     */
    IPage<Item> pageItems(Page<Item> page, Item item, Long familyId);

    /**
     * 根据ID查询物品详情
     *
     * @param id 物品ID
     * @return 物品详情
     */
    Item getItemDetail(Long id);

    /**
     * 新增物品
     *
     * @param item 物品信息
     * @return 是否成功
     */
    boolean addItem(Item item);

    /**
     * 更新物品
     *
     * @param item 物品信息
     * @return 是否成功
     */
    boolean updateItem(Item item);

    /**
     * 删除物品
     *
     * @param id 物品ID
     * @return 是否成功
     */
    boolean deleteItem(Long id);

    /**
     * 批量删除物品
     *
     * @param ids 物品ID列表
     * @return 是否成功
     */
    boolean batchDeleteItems(List<Long> ids);

    /**
     * 获取指定家庭的所有物品
     *
     * @param familyId 家庭ID
     * @return 物品列表
     */
    List<Item> getItemsByFamilyId(Long familyId);

    /**
     * 根据分类ID查询物品列表
     *
     * @param categoryId 分类ID
     * @param familyId   家庭ID
     * @return 物品列表
     */
    List<Item> listItemsByCategory(Long categoryId, Long familyId);

    /**
     * 根据空间ID查询物品列表
     *
     * @param spaceId  空间ID
     * @param familyId 家庭ID
     * @return 物品列表
     */
    List<Item> listItemsBySpace(Long spaceId, Long familyId);

    /**
     * 根据使用人ID查询物品列表
     *
     * @param userId   使用人ID
     * @param familyId 家庭ID
     * @return 物品列表
     */
    List<Item> listItemsByUser(Long userId, Long familyId);

    /**
     * 根据状态查询物品列表
     *
     * @param status   状态
     * @param familyId 家庭ID
     * @return 物品列表
     */
    List<Item> listItemsByStatus(String status, Long familyId);

    /**
     * 查询即将过期的物品列表
     *
     * @param days     天数
     * @param familyId 家庭ID
     * @return 物品列表
     */
    List<Item> listExpiringItems(Integer days, Long familyId);

    /**
     * 查询已过期的物品列表
     *
     * @param familyId 家庭ID
     * @return 物品列表
     */
    List<Item> listExpiredItems(Long familyId);

    /**
     * 统计物品总价值
     *
     * @param familyId 家庭ID
     * @return 总价值
     */
    double sumItemsValue(Long familyId);

    /**
     * 统计各分类物品数量与价值
     *
     * @param familyId 家庭ID
     * @return 统计结果
     */
    List<Object> statItemsByCategory(Long familyId);

    /**
     * 统计各位置物品分布
     *
     * @param familyId 家庭ID
     * @return 统计结果
     */
    List<Object> statItemsBySpace(Long familyId);

    /**
     * 统计使用频率分析
     *
     * @param familyId 家庭ID
     * @return 统计结果
     */
    List<Object> statItemsByUsageFrequency(Long familyId);

    /**
     * 根据购买日期范围查询物品列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param familyId  家庭ID
     * @return 物品列表
     */
    List<Item> listItemsByPurchaseDateRange(LocalDate startDate, LocalDate endDate, Long familyId);

    /**
     * 根据价格区间查询物品列表
     *
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param familyId 家庭ID
     * @return 物品列表
     */
    List<Item> listItemsByPriceRange(Double minPrice, Double maxPrice, Long familyId);
}