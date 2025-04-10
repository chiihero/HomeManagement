package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.Entity;

import java.time.LocalDate;
import java.util.List;

/**
 * 实体服务接口
 */
public interface EntityService extends IService<Entity> {

    /**
     * 分页查询实体
     *
     * @param page 分页参数
     * @param entity 查询条件
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<Entity> pageEntities(Page<Entity> page, Entity entity, Long userId);

    /**
     * 获取实体详情
     *
     * @param id 实体ID
     * @return 实体详情
     */
    Entity getEntityDetail(Long id);

    /**
     * 添加实体
     *
     * @param entity 实体信息
     * @return 是否成功
     */
    boolean addEntity(Entity entity);

    /**
     * 更新实体
     *
     * @param entity 实体信息
     * @return 是否成功
     */
    boolean updateEntity(Entity entity);

    /**
     * 删除实体
     *
     * @param id 实体ID
     * @return 是否成功
     */
    boolean deleteEntity(Long id);

    /**
     * 批量删除实体
     *
     * @param ids 实体ID列表
     * @return 是否成功
     */
    boolean batchDeleteEntities(List<Long> ids);

    /**
     * 获取所有者的所有实体
     *
     * @param userId 用户ID
     * @return 实体列表
     */
    List<Entity> getEntitiesByUserId(Long userId);

    /**
     * 获取所有者指定类型的实体
     *
     * @param userId 用户ID
     * @param type 实体类型
     * @return 实体列表
     */
    List<Entity> getEntitiesByType(Long userId, String type);

    /**
     * 根据父实体ID获取子实体列表
     *
     * @param parentId 父实体ID
     * @param userId 用户ID
     * @return 子实体列表
     */
    List<Entity> listChildEntities(Long parentId, Long userId);

    /**
     * 根据用户ID获取物品列表
     *
     * @param userId 用户ID
     * @return 物品列表
     */
    List<Entity> listEntitiesByUser(Long userId);

    /**
     * 根据状态获取物品列表
     *
     * @param status 状态
     * @param userId 用户ID
     * @return 物品列表
     */
    List<Entity> listEntitiesByStatus(String status, Long userId);

    /**
     * 获取即将过保的物品列表
     *
     * @param days 天数
     * @param userId 用户ID
     * @return 物品列表
     */
    List<Entity> listExpiringEntities(Integer days, Long userId);

    /**
     * 获取已过保的物品列表
     *
     * @param userId 用户ID
     * @return 物品列表
     */
    List<Entity> listExpiredEntities(Long userId);

    /**
     * 统计所有者物品总价值
     *
     * @param userId 用户ID
     * @return 总价值
     */
    double sumEntitiesValue(Long userId);

    /**
     * 根据父实体统计子实体数量和价值
     *
     * @param userId 用户ID
     * @return 统计结果列表
     */
    List<Object> statEntitiesByParent(Long userId);

    /**
     * 根据标签统计物品数量和价值
     *
     * @param userId 用户ID
     * @return 统计结果列表
     */
    List<Object> statEntitiesByTag(Long userId);

    /**
     * 根据使用频率统计物品数量
     *
     * @param userId 用户ID
     * @return 统计结果列表
     */
    List<Object> statEntitiesByUsageFrequency(Long userId);
    
    /**
     * 根据购买日期范围查询物品
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 用户ID
     * @return 物品列表
     */
    List<Entity> listEntitiesByPurchaseDateRange(LocalDate startDate, LocalDate endDate, Long userId);
    
    /**
     * 根据价格范围查询物品
     *
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param userId 用户ID
     * @return 物品列表
     */
    List<Entity> listEntitiesByPriceRange(Double minPrice, Double maxPrice, Long userId);
    
    /**
     * 根据标签ID查询实体
     *
     * @param tagId 标签ID
     * @param userId 用户ID
     * @return 实体列表
     */
    List<Entity> listEntitiesByTag(Long tagId, Long userId);
    
    /**
     * 获取最近添加的实体
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 实体列表
     */
    List<Entity> getRecentEntities(Long userId, Integer limit);
    
    /**
     * 获取实体树
     *
     * @param userId 用户ID
     * @return 根实体列表
     */
    List<Entity> getEntityTree(Long userId);

    /**
     * 获取最近添加的实体列表
     *
     * @param userId 用户ID
     * @param days 天数
     * @return 实体列表
     */
    List<Entity> getRecentEntitiesByDays(Long userId, Integer days);
    
    /**
     * 根据关键词搜索实体
     *
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 实体列表
     */
    List<Entity> searchEntities(Long userId, String keyword);
} 