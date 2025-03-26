package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Item;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.mapper.ItemMapper;
import com.chii.homemanagement.service.ItemService;
import com.chii.homemanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物品服务实现类
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserService userService;

    @Override
    public IPage<Item> pageItems(Page<Item> page, Item item, Long familyId) {
        // 检查输入参数
        if (page == null) {
            page = new Page<>(1, 10);
        }
        
        if (familyId == null) {
            throw new IllegalArgumentException("家庭ID不能为空");
        }
        
        // 使用MP内置的方法代替
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getFamilyId, familyId);

        // 添加查询条件
        if (item != null) {
            if (StringUtils.hasText(item.getName())) {
                queryWrapper.like(Item::getName, item.getName());
            }
            if (item.getSpaceId() != null) {
                queryWrapper.eq(Item::getSpaceId, item.getSpaceId());
            }
            if (StringUtils.hasText(item.getStatus())) {
                queryWrapper.eq(Item::getStatus, item.getStatus());
            }
        }

        queryWrapper.orderByDesc(Item::getCreateTime);
        return page(page, queryWrapper);
    }

    @Override
    public Item getItemDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("物品ID不能为空");
        }
        
        Item item = getById(id);
        if (item == null) {
            return null;
        }
        
        // 这里可以加载关联数据，如分类信息、空间信息、用户信息等
        // 例如：item.setCategoryName(categoryService.getNameById(item.getCategoryId()));
        // 例如：item.setSpaceName(spaceService.getNameById(item.getSpaceId()));
        // 例如：item.setUserName(userService.getNameById(item.getUserId()));
        
        return item;
    }

    @Override
    @Transactional
    public boolean addItem(Item item) {
        if (item == null) {
            return false;
        }
        
        // 设置创建用户ID，如果未设置的话
        if (item.getCreateUserId() == null) {
            // 从Spring Security获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                // 使用userService获取用户信息
                User user = userService.getUserByUsername(username);
                item.setCreateUserId(user != null ? user.getId() : 1L);
            } else {
                // 如果未认证，使用默认值如1
                item.setCreateUserId(1L);
            }
        }
        
        // 如果设置了保修期，但未设置保修截止日期，自动计算保修截止日期
        if (item.getWarrantyPeriod() != null && item.getWarrantyPeriod() > 0 
                && item.getPurchaseDate() != null && item.getWarrantyEndDate() == null) {
            item.setWarrantyEndDate(item.getPurchaseDate().plusMonths(item.getWarrantyPeriod()));
        }
        
        // 确保设置了默认状态
        if (item.getStatus() == null || item.getStatus().isEmpty()) {
            item.setStatus("normal");
        }
        
        return save(item);
    }

    @Override
    @Transactional
    public boolean updateItem(Item item) {
        if (item == null || item.getId() == null) {
            return false;
        }
        
        // 获取原始记录以确保存在
        Item existingItem = getById(item.getId());
        if (existingItem == null) {
            return false;
        }
        
        // 如果设置了保修期，但未设置保修截止日期，自动计算保修截止日期
        if (item.getWarrantyPeriod() != null && item.getWarrantyPeriod() > 0 
                && item.getPurchaseDate() != null && item.getWarrantyEndDate() == null) {
            item.setWarrantyEndDate(item.getPurchaseDate().plusMonths(item.getWarrantyPeriod()));
        }
        
        // 在实际应用中，这里还应该添加权限检查，确保只有创建者或有权限的用户才能修改物品
        // 例如：检查当前用户是否有权限修改该物品
        
        return updateById(item);
    }

    @Override
    @Transactional
    public boolean deleteItem(Long id) {
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeleteItems(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<Item> getItemsByFamilyId(Long familyId) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getFamilyId, familyId);
        return list(queryWrapper);
    }

    @Override
    public List<Item> listItemsByCategory(Long categoryId, Long familyId) {
        // 这个需要多表联查，暂时使用模拟数据
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getFamilyId, familyId);
        // 实际应该通过item_category关联表查询
        return list(queryWrapper);
    }

    @Override
    public List<Item> listItemsBySpace(Long spaceId, Long familyId) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getSpaceId, spaceId)
                .eq(Item::getFamilyId, familyId);
        return list(queryWrapper);
    }

    @Override
    public List<Item> listItemsByUser(Long userId, Long familyId) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getUserId, userId)
                .eq(Item::getFamilyId, familyId);
        return list(queryWrapper);
    }

    @Override
    public List<Item> listItemsByStatus(String status, Long familyId) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getStatus, status)
                .eq(Item::getFamilyId, familyId);
        return list(queryWrapper);
    }

    @Override
    public List<Item> listExpiringItems(Integer days, Long familyId) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getFamilyId, familyId)
                .isNotNull(Item::getWarrantyEndDate)
                .le(Item::getWarrantyEndDate, targetDate);
        return list(queryWrapper);
    }

    @Override
    public List<Item> listExpiredItems(Long familyId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getFamilyId, familyId)
                .isNotNull(Item::getWarrantyEndDate)
                .lt(Item::getWarrantyEndDate, today);
        return list(queryWrapper);
    }

    @Override
    public double sumItemsValue(Long familyId) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getFamilyId, familyId)
                .isNotNull(Item::getPrice);
        List<Item> items = list(queryWrapper);
        BigDecimal totalValue = items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalValue.doubleValue();
    }

    @Override
    public List<Object> statItemsByCategory(Long familyId) {
        // 使用模拟数据
        List<Object> result = new ArrayList<>();
        Map<String, Object> data1 = new HashMap<>();
        data1.put("category", "电子产品");
        data1.put("count", 15);
        data1.put("value", 12000.00);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("category", "家具");
        data2.put("count", 10);
        data2.put("value", 8000.00);

        Map<String, Object> data3 = new HashMap<>();
        data3.put("category", "厨房用品");
        data3.put("count", 8);
        data3.put("value", 3000.00);

        result.add(data1);
        result.add(data2);
        result.add(data3);

        return result;
    }

    @Override
    public List<Object> statItemsBySpace(Long familyId) {
        // 使用模拟数据
        List<Object> result = new ArrayList<>();
        Map<String, Object> data1 = new HashMap<>();
        data1.put("space", "客厅");
        data1.put("count", 12);
        data1.put("value", 10000.00);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("space", "卧室");
        data2.put("count", 18);
        data2.put("value", 7000.00);

        Map<String, Object> data3 = new HashMap<>();
        data3.put("space", "厨房");
        data3.put("count", 15);
        data3.put("value", 5000.00);

        result.add(data1);
        result.add(data2);
        result.add(data3);

        return result;
    }

    @Override
    public List<Object> statItemsByUsageFrequency(Long familyId) {
        // 使用模拟数据
        List<Object> result = new ArrayList<>();
        Map<String, Object> data1 = new HashMap<>();
        data1.put("frequency", "daily");
        data1.put("count", 20);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("frequency", "weekly");
        data2.put("count", 15);

        Map<String, Object> data3 = new HashMap<>();
        data3.put("frequency", "monthly");
        data3.put("count", 10);

        Map<String, Object> data4 = new HashMap<>();
        data4.put("frequency", "rarely");
        data4.put("count", 5);

        result.add(data1);
        result.add(data2);
        result.add(data3);
        result.add(data4);

        return result;
    }

    @Override
    public List<Item> listItemsByPurchaseDateRange(LocalDate startDate, LocalDate endDate, Long familyId) {
        if (familyId == null) {
            throw new IllegalArgumentException("家庭ID不能为空");
        }
        
        // 如果开始日期为空，设置为一个很早的日期
        LocalDate effectiveStartDate = startDate;
        if (effectiveStartDate == null) {
            effectiveStartDate = LocalDate.of(1900, 1, 1);
        }
        
        // 如果结束日期为空，设置为当前日期
        LocalDate effectiveEndDate = endDate;
        if (effectiveEndDate == null) {
            effectiveEndDate = LocalDate.now();
        }
        
        // 如果开始日期晚于结束日期，交换它们
        if (effectiveStartDate.isAfter(effectiveEndDate)) {
            LocalDate temp = effectiveStartDate;
            effectiveStartDate = effectiveEndDate;
            effectiveEndDate = temp;
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getFamilyId, familyId)
                .ge(Item::getPurchaseDate, effectiveStartDate)
                .le(Item::getPurchaseDate, effectiveEndDate);
        
        return list(queryWrapper);
    }

    @Override
    public List<Item> listItemsByPriceRange(Double minPrice, Double maxPrice, Long familyId) {
        if (familyId == null) {
            throw new IllegalArgumentException("家庭ID不能为空");
        }
        
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getFamilyId, familyId);
        
        // 安全处理价格范围
        if (minPrice != null && minPrice >= 0) {
            queryWrapper.ge(Item::getPrice, new BigDecimal(minPrice));
        }
        
        if (maxPrice != null && maxPrice > 0) {
            queryWrapper.le(Item::getPrice, new BigDecimal(maxPrice));
        }
        
        // 如果设置了最小价格和最大价格，确保最小价格不大于最大价格
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            // 交换最小价格和最大价格
            Double temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;
            
            // 重新设置查询条件
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Item::getFamilyId, familyId)
                    .ge(Item::getPrice, new BigDecimal(minPrice))
                    .le(Item::getPrice, new BigDecimal(maxPrice));
        }
        
        return list(queryWrapper);
    }
} 