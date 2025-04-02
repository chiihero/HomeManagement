package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Tag;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.mapper.EntityMapper;
import com.chii.homemanagement.service.EntityImageService;
import com.chii.homemanagement.service.EntityService;
import com.chii.homemanagement.service.EntityTagService;
import com.chii.homemanagement.service.TagService;
import com.chii.homemanagement.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 实体服务实现类
 */
@Service
@Slf4j
public class EntityServiceImpl extends ServiceImpl<EntityMapper, Entity> implements EntityService {

    @Autowired
    private EntityMapper entityMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityTagService entityTagService;

    @Autowired
    private EntityImageService entityImageService;

    @Autowired
    private TagService tagService;

    @Override
    public IPage<Entity> pageEntities(Page<Entity> page, Entity entity, Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("所有者ID不能为空");
        }
        
        // 默认分页参数
        page = Optional.ofNullable(page).orElse(new Page<>(1, 10));
        
        // 构建查询条件
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<Entity>()
                .eq(Entity::getOwnerId, ownerId)
                // 排除已丢弃的实体
                .ne(Entity::getStatus, "discarded")
                .orderByDesc(Entity::getCreateTime);
        
        // 添加查询条件（如果有）
        if (entity != null) {
            queryWrapper.like(StringUtils.hasText(entity.getName()), Entity::getName, entity.getName())
                       .eq(StringUtils.hasText(entity.getType()), Entity::getType, entity.getType())
                       .eq(entity.getParentId() != null, Entity::getParentId, entity.getParentId())
                       .eq(StringUtils.hasText(entity.getStatus()), Entity::getStatus, entity.getStatus());
        }
        
        return page(page, queryWrapper);
    }

    @Override
    public Entity getEntityDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("实体ID不能为空");
        }
        
        Entity entity = getById(id);
        if (entity == null) {
            return null;
        }
        
        // 并行加载所有相关数据
        // 1. 加载子实体列表
        List<Entity> children = entityMapper.listChildren(id, entity.getOwnerId());
        if (!children.isEmpty()) {
            entity.setChildren(children);
        }
        
        // 2. 加载父实体信息
        if (entity.getParentId() != null) {
            Entity parent = getById(entity.getParentId());
            if (parent != null) {
                entity.setParentName(parent.getName());
            }
        }
        
        // 3. 加载标签
        List<Tag> tags = entityTagService.getTagsByEntityId(id);
        if (!tags.isEmpty()) {
            entity.setTags(tags);
        }
        
        // 4. 加载图片
        entity.setImages(entityImageService.getImagesByEntityId(id));
        
        // 5. 加载使用人信息
        if (entity.getUserId() != null) {
            User user = userService.getUserById(entity.getUserId());
            if (user != null) {
                entity.setUserName(user.getUsername());
            }
        }
        
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addEntity(Entity entity) {
        if (entity == null) {
            return false;
        }
        
        // 设置创建用户ID（如果未设置）
        if (entity.getCreateUserId() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                User user = userService.getUserByUsername(username);
                entity.setCreateUserId(user != null ? user.getId() : 1L);
            } else {
                entity.setCreateUserId(1L);
            }
        }
        
        // 处理实体的通用和特有字段
        processEntityFields(entity);
        
        return save(entity);
    }

    /**
     * 处理实体特有字段
     */
    private void processEntityFields(Entity entity) {
        // 如果设置了保修期和购买日期，但未设置保修截止日期，则自动计算
        if (entity.getWarrantyPeriod() != null && entity.getWarrantyPeriod() > 0 
                && entity.getPurchaseDate() != null && entity.getWarrantyEndDate() == null) {
            entity.setWarrantyEndDate(entity.getPurchaseDate().plusMonths(entity.getWarrantyPeriod()));
        }
        
        // 确保设置了默认状态
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("normal");
        }
        
        // 计算层级和路径
        if (entity.getParentId() != null) {
            Entity parentEntity = getById(entity.getParentId());
            if (parentEntity != null) {
                entity.setLevel(parentEntity.getLevel() + 1);
                entity.setPath(StringUtils.hasText(parentEntity.getPath()) ? 
                    parentEntity.getPath() + "," + parentEntity.getId() : parentEntity.getId().toString());
            }
        } else {
            entity.setLevel(0);
            entity.setPath("");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEntity(Entity entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }
        
        // 获取原有实体信息
        Entity existingEntity = getById(entity.getId());
        if (existingEntity == null) {
            return false;
        }
        
        // 处理实体的通用和特有字段
        processEntityFields(entity);
        
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEntity(Long id) {
        Entity entity = getById(id);
        if (entity == null) {
            return false;
        }
        
        // 处理子实体
        List<Entity> children = entityMapper.listChildren(id, entity.getOwnerId());
        if (!children.isEmpty()) {
            // 将子实体的父ID设为null
            children.forEach(child -> {
                child.setParentId(null);
                updateById(child);
            });
        }
        
        // 删除关联数据
        entityTagService.removeEntityTags(id);
        entityImageService.deleteByEntityId(id);
        
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteEntities(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        return ids.stream()
                 .map(this::deleteEntity)
                 .allMatch(Boolean::booleanValue);
    }


    @Override
    public List<Entity> getEntitiesByOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("所有者ID不能为空");
        }
        
        return list(new LambdaQueryWrapper<Entity>()
                .eq(Entity::getOwnerId, ownerId));
    }

    @Override
    public List<Entity> getEntitiesByType(Long ownerId, String type) {
        if (ownerId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getOwnerId, ownerId)
                   .eq(StringUtils.hasText(type), Entity::getType, type);
        
        // 根据类型进行不同处理
        // 物品类型需排除已丢弃的
        if ("item".equals(type)) {
            queryWrapper.ne(Entity::getStatus, "discarded");
        }
        
        // 根据条件查询
        queryWrapper.eq(Entity::getOwnerId, ownerId);
        
        // 排除已丢弃的实体
        queryWrapper.ne(Entity::getStatus, "discarded");
        
        return list(queryWrapper);
    }

    @Override
    public List<Entity> listChildEntities(Long parentId, Long ownerId) {
        if (parentId == null || ownerId == null) {
            return Collections.emptyList();
        }
        return entityMapper.listChildren(parentId, ownerId);
    }

    @Override
    public List<Entity> listEntitiesByUser(Long userId, Long ownerId) {
        if (userId == null || ownerId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getOwnerId, ownerId)
                   .eq(Entity::getUserId, userId)
                   .ne(Entity::getStatus, "discarded");
        
        return list(queryWrapper);
    }

    @Override
    public List<Entity> listEntitiesByStatus(String status, Long ownerId) {
        if (!StringUtils.hasText(status) || ownerId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getOwnerId, ownerId)
                   .eq(Entity::getStatus, status);
        
        return list(queryWrapper);
    }

    @Override
    public List<Entity> listExpiringEntities(Integer days, Long ownerId) {
        if (days == null || ownerId == null) {
            return new ArrayList<>();
        }
        
        LocalDate currentDate = LocalDate.now();
        LocalDate expiryThreshold = currentDate.plusDays(days);
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getOwnerId, ownerId)
                   .ne(Entity::getStatus, "discarded")
                   .isNotNull(Entity::getWarrantyEndDate)
                   .between(Entity::getWarrantyEndDate, currentDate, expiryThreshold);
        
        return list(queryWrapper);
    }

    @Override
    public List<Entity> listExpiredEntities(Long ownerId) {
        if (ownerId == null) {
            return new ArrayList<>();
        }
        
        LocalDate currentDate = LocalDate.now();
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getOwnerId, ownerId)
                   .ne(Entity::getStatus, "discarded")
                   .isNotNull(Entity::getWarrantyEndDate)
                   .lt(Entity::getWarrantyEndDate, currentDate);
        
        return list(queryWrapper);
    }

    @Override
    public double sumEntitiesValue(Long ownerId) {
        if (ownerId == null) {
            return 0;
        }
        
        List<Entity> entities = list(new LambdaQueryWrapper<Entity>()
                .eq(Entity::getOwnerId, ownerId)
                .ne(Entity::getStatus, "discarded")
                .isNotNull(Entity::getPrice));
        
        return entities.stream()
                .filter(e -> e.getPrice() != null && e.getQuantity() != null)
                .map(e -> e.getPrice().multiply(new BigDecimal(e.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    @Override
    public List<Object> statEntitiesByParent(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("所有者ID不能为空");
        }
        
        List<Object> result = new ArrayList<>();
        
        // 获取所有实体
        List<Entity> allEntities = getEntitiesByOwnerId(ownerId);
        
        // 构建父ID到子实体的映射
        Map<Long, List<Entity>> parentChildMap = allEntities.stream()
                .filter(e -> e.getParentId() != null)
                .collect(Collectors.groupingBy(Entity::getParentId));
        
        // 统计每个父实体的子实体数量和总价值
        for (Entity parent : allEntities) {
            List<Entity> children = parentChildMap.getOrDefault(parent.getId(), Collections.emptyList());
            
            // 过滤掉已丢弃的物品
            children = children.stream()
                    .filter(child -> !("item".equals(child.getType()) && "discarded".equals(child.getStatus())))
                    .collect(Collectors.toList());
            
            if (!children.isEmpty()) {
                // 计算物品总数和总价值
                BigDecimal totalValue = children.stream()
                        .filter(e -> "item".equals(e.getType()) && e.getPrice() != null && e.getQuantity() != null)
                        .map(e -> e.getPrice().multiply(new BigDecimal(e.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Map<String, Object> parentData = new HashMap<>();
                parentData.put("id", parent.getId());
                parentData.put("name", parent.getName());
                parentData.put("type", parent.getType());
                parentData.put("count", children.size());
                parentData.put("value", totalValue.doubleValue());
                result.add(parentData);
            }
        }
        
        // 处理未分类的实体
        List<Entity> noParentEntities = allEntities.stream()
                .filter(e -> e.getParentId() == null)
                .filter(e -> !("item".equals(e.getType()) && "discarded".equals(e.getStatus())))
                .collect(Collectors.toList());
        
        if (!noParentEntities.isEmpty()) {
            BigDecimal totalValue = noParentEntities.stream()
                    .filter(e -> "item".equals(e.getType()) && e.getPrice() != null && e.getQuantity() != null)
                    .map(e -> e.getPrice().multiply(new BigDecimal(e.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> noParentData = new HashMap<>();
            noParentData.put("id", null);
            noParentData.put("name", "未分类");
            noParentData.put("type", "none");
            noParentData.put("count", noParentEntities.size());
            noParentData.put("value", totalValue.doubleValue());
            result.add(noParentData);
        }
        
        return result;
    }

    @Override
    public List<Object> statEntitiesByTag(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("所有者ID不能为空");
        }
        
        // 获取所有者下的所有标签
        List<Tag> tags = tagService.getTagsByOwnerId(ownerId);
        
        return tags.stream().map(tag -> {
            // 获取该标签下的所有实体
            List<Entity> entities = listEntitiesByTag(tag.getId(), ownerId);
            
            // 过滤有效物品
            List<Entity> validItems = entities.stream()
                    .filter(e -> "item".equals(e.getType()) && !"discarded".equals(e.getStatus()))
                    .collect(Collectors.toList());
            
            if (validItems.isEmpty()) {
                return null;
            }
            
            // 计算总价值
            BigDecimal totalValue = validItems.stream()
                    .filter(e -> e.getPrice() != null && e.getQuantity() != null)
                    .map(e -> e.getPrice().multiply(new BigDecimal(e.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> tagData = new HashMap<>();
            tagData.put("tagId", tag.getId());
            tagData.put("tagName", tag.getName());
            tagData.put("tagColor", tag.getColor());
            tagData.put("count", validItems.size());
            tagData.put("value", totalValue.doubleValue());
            return tagData;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

    @Override
    public List<Object> statEntitiesByUsageFrequency(Long ownerId) {
        if (ownerId == null) {
            return new ArrayList<>();
        }
        
        List<Object> result = new ArrayList<>();
        
        // 定义使用频率类型
        Map<String, String> frequencyNameMap = Map.of(
            "daily", "每天",
            "weekly", "每周",
            "monthly", "每月",
            "rarely", "很少"
        );
        
        // 查询所有实体
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getOwnerId, ownerId)
                   .ne(Entity::getStatus, "discarded");
        
        // 统计每种使用频率的实体数量
        for (String frequency : frequencyNameMap.keySet()) {
            long countValue = count(new LambdaQueryWrapper<Entity>()
                    .eq(Entity::getOwnerId, ownerId)
                    .eq(Entity::getUsageFrequency, frequency)
                    .ne(Entity::getStatus, "discarded"));
            
            if (countValue > 0) {
                Map<String, Object> frequencyData = new HashMap<>();
                frequencyData.put("frequency", frequency);
                frequencyData.put("frequencyName", frequencyNameMap.get(frequency));
                frequencyData.put("count", countValue);
                result.add(frequencyData);
            }
        }
        
        // 未指定使用频率的实体
        long noFrequencyCount = count(new LambdaQueryWrapper<Entity>()
                .isNull(Entity::getUsageFrequency)
                .eq(Entity::getOwnerId, ownerId)
                .ne(Entity::getStatus, "discarded"));
        
        if (noFrequencyCount > 0) {
            Map<String, Object> noFrequencyData = new HashMap<>();
            noFrequencyData.put("frequency", "unknown");
            noFrequencyData.put("frequencyName", "未指定");
            noFrequencyData.put("count", noFrequencyCount);
            result.add(noFrequencyData);
        }
        
        return result;
    }
    
    @Override
    public List<Entity> listEntitiesByPurchaseDateRange(LocalDate startDate, LocalDate endDate, Long ownerId) {
        if (ownerId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getOwnerId, ownerId)
                   .ne(Entity::getStatus, "discarded");
        
        if (startDate != null && endDate != null) {
            queryWrapper.between(Entity::getPurchaseDate, startDate, endDate);
        } else if (startDate != null) {
            queryWrapper.ge(Entity::getPurchaseDate, startDate);
        } else if (endDate != null) {
            queryWrapper.le(Entity::getPurchaseDate, endDate);
        }
        
        return list(queryWrapper);
    }
    
    @Override
    public List<Entity> listEntitiesByPriceRange(Double minPrice, Double maxPrice, Long ownerId) {
        if (ownerId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getOwnerId, ownerId)
                   .ne(Entity::getStatus, "discarded");
        
        if (minPrice != null) {
            queryWrapper.ge(Entity::getPrice, new BigDecimal(minPrice.toString()));
        }
        
        if (maxPrice != null) {
            queryWrapper.le(Entity::getPrice, new BigDecimal(maxPrice.toString()));
        }
        
        return list(queryWrapper);
    }
    
    @Override
    public List<Entity> listEntitiesByTag(Long tagId, Long ownerId) {
        if (tagId == null || ownerId == null) {
            return new ArrayList<>();
        }
        
        return entityMapper.listEntitiesByTagId(tagId, ownerId);
    }
    
    @Override
    public List<Entity> getEntityTree(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("所有者ID不能为空");
        }
        
        // 获取所有有效实体
        List<Entity> allEntities = list(new LambdaQueryWrapper<Entity>()
                .eq(Entity::getOwnerId, ownerId)
                .and(wrapper -> wrapper.ne(Entity::getStatus, "discarded")
                                      .or()
                                      .ne(Entity::getType, "item")));
        
        // 丰富实体信息
        enrichEntityDetails(allEntities);
        
        // 构建父子关系Map
        Map<Long, List<Entity>> parentChildMap = allEntities.stream()
                .filter(e -> e.getParentId() != null)
                .collect(Collectors.groupingBy(Entity::getParentId));
        
        // 过滤出根实体（没有父实体的实体）
        List<Entity> rootEntities = allEntities.stream()
                .filter(entity -> entity.getParentId() == null)
                .collect(Collectors.toList());
        
        // 为每个根实体设置子实体
        rootEntities.forEach(root -> setChildEntities(root, parentChildMap));
        
        return rootEntities;
    }
    
    /**
     * 丰富实体详细信息
     */
    private void enrichEntityDetails(List<Entity> entities) {
        if (entities.isEmpty()) {
            return;
        }
        
        // 用户ID映射
        Map<Long, User> userMap = entities.stream()
                .filter(e -> e.getUserId() != null)
                .map(Entity::getUserId)
                .distinct()
                .map(userService::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(User::getId, u -> u));
        
        // 实体ID到实体的映射（用于查找父实体）
        Map<Long, Entity> entityMap = entities.stream()
                .collect(Collectors.toMap(Entity::getId, e -> e));
        
        // 批量加载所有实体的标签
        entities.forEach(entity -> {
            // 设置使用人信息
            if (entity.getUserId() != null && userMap.containsKey(entity.getUserId())) {
                entity.setUserName(userMap.get(entity.getUserId()).getUsername());
            }
            
            // 设置父实体信息
            if (entity.getParentId() != null && entityMap.containsKey(entity.getParentId())) {
                entity.setParentName(entityMap.get(entity.getParentId()).getName());
            }
            
            // 加载标签（这里需要分别查询，考虑到标签数量通常不多，性能影响有限）
            List<Tag> tags = entityTagService.getTagsByEntityId(entity.getId());
            if (!tags.isEmpty()) {
                entity.setTags(tags);
            }
        });
    }
    
    /**
     * 递归设置子实体
     */
    private void setChildEntities(Entity parent, Map<Long, List<Entity>> parentChildMap) {
        List<Entity> children = parentChildMap.getOrDefault(parent.getId(), Collections.emptyList());
        
        if (!children.isEmpty()) {
            parent.setChildren(children);
            
            // 递归设置子实体的子实体
            children.forEach(child -> setChildEntities(child, parentChildMap));
        } else {
            parent.setChildren(Collections.emptyList());
        }
    }
} 