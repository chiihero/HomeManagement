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
import com.chii.homemanagement.exception.BusinessException;
import com.chii.homemanagement.common.ErrorCode;
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
 * 提供实体（物品和空间）的增删改查、树形结构处理等功能
 * 
 * @author chii
 * @since 1.0.0
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

    /**
     * 分页查询实体
     *
     * @param page 分页参数
     * @param entity 查询条件
     * @param userId 用户ID
     * @return 分页结果
     */
    @Override
    public IPage<Entity> pageEntities(Page<Entity> page, Entity entity, Long userId) {
        if (userId == null) {
            log.error("分页查询实体时用户ID为空");
            throw new BusinessException(ErrorCode.PARAM_NOT_VALID.getCode(), "用户ID不能为空");
        }
        
        log.info("开始分页查询实体: userId={}, page={}, size={}", userId, page.getCurrent(), page.getSize());
        
        // 默认分页参数
        page = Optional.ofNullable(page).orElse(new Page<>(1, 10));
        
        // 构建查询条件
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<Entity>()
                .eq(Entity::getUserId, userId)
                .orderByDesc(Entity::getCreateTime);
        
        // 添加查询条件（如果有）
        if (entity != null) {
            queryWrapper.like(StringUtils.hasText(entity.getName()), Entity::getName, entity.getName())
                       .eq(StringUtils.hasText(entity.getType()), Entity::getType, entity.getType())
                       .eq(entity.getParentId() != null, Entity::getParentId, entity.getParentId())
                       .eq(StringUtils.hasText(entity.getStatus()), Entity::getStatus, entity.getStatus());
        }
        
        IPage<Entity> result = page(page, queryWrapper);
        log.info("分页查询实体完成: 总记录数={}, 总页数={}", result.getTotal(), result.getPages());
        
        return result;
    }

    /**
     * 获取实体详情
     *
     * @param id 实体ID
     * @return 实体详情
     */
    @Override
    public Entity getEntityDetail(Long id) {
        if (id == null) {
            log.error("获取实体详情时ID为空");
            throw new BusinessException(ErrorCode.PARAM_NOT_VALID.getCode(), "实体ID不能为空");
        }
        
        log.info("开始获取实体详情: id={}", id);
        
        Entity entity = getById(id);
        if (entity == null) {
            log.warn("实体不存在: id={}", id);
            return null;
        }
        
        log.debug("获取到实体基本信息: id={}, name={}, type={}", id, entity.getName(), entity.getType());
        
        // 并行加载所有相关数据
        // 1. 加载子实体列表
        List<Entity> children = entityMapper.listChildren(id, entity.getUserId());
        if (!children.isEmpty()) {
            entity.setChildren(children);
            log.debug("加载子实体列表: id={}, 子实体数量={}", id, children.size());
        }
        
        // 2. 加载父实体信息
        if (entity.getParentId() != null) {
            Entity parent = getById(entity.getParentId());
            if (parent != null) {
                entity.setParentName(parent.getName());
                log.debug("加载父实体信息: id={}, parentId={}, parentName={}", 
                          id, entity.getParentId(), parent.getName());
            }
        }
        
        // 3. 加载标签
        List<Tag> tags = entityTagService.getTagsByEntityId(id);
        if (!tags.isEmpty()) {
            entity.setTags(tags);
            log.debug("加载标签: id={}, 标签数量={}", id, tags.size());
        }
        
        // 4. 加载图片
        entity.setImages(entityImageService.getImagesByEntityId(id));
        log.debug("加载图片: id={}, 图片数量={}", id, 
                 entity.getImages() != null ? entity.getImages().size() : 0);
        
        // 5. 加载使用人信息
        if (entity.getUserId() != null) {
            User user = userService.getUserById(entity.getUserId());
            if (user != null) {
                entity.setUserName(user.getUsername());
                log.debug("加载使用人信息: id={}, userId={}, userName={}", 
                         id, entity.getUserId(), user.getUsername());
            }
        }
        
        log.info("获取实体详情完成: id={}, name={}", id, entity.getName());
        return entity;
    }

    /**
     * 添加实体
     *
     * @param entity 实体信息
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addEntity(Entity entity) {
        if (entity == null) {
            log.error("添加实体时实体对象为空");
            return false;
        }
        
        log.info("开始添加实体: name={}, type={}, userId={}", 
                entity.getName(), entity.getType(), entity.getUserId());
        
        // 设置创建用户ID（如果未设置）
        if (entity.getCreateUserId() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                User user = userService.getUserByUsername(username);
                entity.setCreateUserId(user != null ? user.getUserId() : 1L);
                log.debug("设置创建用户ID: name={}, createUserId={}", entity.getName(), entity.getCreateUserId());
            } else {
                entity.setCreateUserId(1L);
                log.debug("未获取到认证信息，使用默认用户ID: name={}, createUserId=1", entity.getName());
            }
        }
        
        // 处理实体的通用和特有字段
        processEntityFields(entity);
        
        boolean result = save(entity);
        log.info("添加实体结果: id={}, name={}, success={}", entity.getId(), entity.getName(), result);
        
        return result;
    }

    /**
     * 处理实体特有字段
     * 设置保修期、状态、层级和路径等信息
     * 
     * @param entity 实体对象
     */
    private void processEntityFields(Entity entity) {
        log.debug("开始处理实体字段: name={}", entity.getName());
        
        // 如果设置了保修期和购买日期，但未设置保修截止日期，则自动计算
        if (entity.getWarrantyPeriod() != null && entity.getWarrantyPeriod() > 0 
                && entity.getPurchaseDate() != null && entity.getWarrantyEndDate() == null) {
            entity.setWarrantyEndDate(entity.getPurchaseDate().plusMonths(entity.getWarrantyPeriod()));
            log.debug("设置保修截止日期: name={}, 购买日期={}, 保修期={}个月, 保修截止日期={}", 
                     entity.getName(), entity.getPurchaseDate(), 
                     entity.getWarrantyPeriod(), entity.getWarrantyEndDate());
        }
        
        // 确保设置了默认状态
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("normal");
            log.debug("设置默认状态: name={}, status=normal", entity.getName());
        }
        
        // 计算层级和路径
        if (entity.getParentId() != null) {
            Entity parentEntity = getById(entity.getParentId());
            if (parentEntity != null) {
                entity.setLevel(parentEntity.getLevel() + 1);
                entity.setPath(StringUtils.hasText(parentEntity.getPath()) ? 
                    parentEntity.getPath() + "," + parentEntity.getId() : parentEntity.getId().toString());
                log.debug("设置层级和路径: name={}, parentId={}, level={}, path={}", 
                         entity.getName(), entity.getParentId(), entity.getLevel(), entity.getPath());
            } else {
                log.warn("未找到父实体: name={}, parentId={}", entity.getName(), entity.getParentId());
            }
        } else {
            entity.setLevel(0);
            entity.setPath("");
            log.debug("根节点实体: name={}, level=0, path=''", entity.getName());
        }
        
        log.debug("实体字段处理完成: name={}", entity.getName());
    }

    /**
     * 更新实体
     *
     * @param entity 实体信息
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEntity(Entity entity) {
        if (entity == null || entity.getId() == null) {
            log.error("更新实体时实体对象为空或ID为空");
            return false;
        }
        
        log.info("开始更新实体: id={}, name={}", entity.getId(), entity.getName());
        
        // 获取原有实体信息
        Entity existingEntity = getById(entity.getId());
        if (existingEntity == null) {
            log.warn("更新失败，实体不存在: id={}", entity.getId());
            return false;
        }
        
        // 处理实体的通用和特有字段
        processEntityFields(entity);
        
        boolean result = updateById(entity);
        log.info("更新实体结果: id={}, name={}, success={}", entity.getId(), entity.getName(), result);
        
        return result;
    }

    /**
     * 删除实体
     *
     * @param id 实体ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEntity(Long id) {
        log.info("开始删除实体: id={}", id);
        
        Entity entity = getById(id);
        if (entity == null) {
            log.warn("删除失败，实体不存在: id={}", id);
            return false;
        }
        
        // 处理子实体
        List<Entity> children = entityMapper.listChildren(id, entity.getUserId());
        if (!children.isEmpty()) {
            // 将子实体的父ID设为null
            log.info("处理子实体: id={}, 子实体数量={}", id, children.size());
            children.forEach(child -> {
                child.setParentId(null);
                updateById(child);
                log.debug("更新子实体父ID: childId={}, childName={}", child.getId(), child.getName());
            });
        }
        
        // 删除关联数据
        log.info("开始删除关联数据: id={}", id);
        entityTagService.removeEntityTags(id);
        entityImageService.deleteByEntityId(id);
        
        boolean result = removeById(id);
        log.info("删除实体结果: id={}, name={}, success={}", id, entity.getName(), result);
        
        return result;
    }

    /**
     * 批量删除实体
     *
     * @param ids 实体ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteEntities(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.error("批量删除实体时ID列表为空");
            return false;
        }
        
        log.info("开始批量删除实体: ids数量={}", ids.size());
        
        boolean result = ids.stream()
                 .map(this::deleteEntity)
                 .allMatch(Boolean::booleanValue);
        
        log.info("批量删除实体结果: ids数量={}, success={}", ids.size(), result);
        
        return result;
    }

    /**
     * 获取所有者的所有实体
     *
     * @param userId 用户ID
     * @return 实体列表
     */
    @Override
    public List<Entity> getEntitiesByUserId(Long userId) {
        if (userId == null) {
            log.error("获取用户实体时用户ID为空");
            throw new BusinessException(ErrorCode.PARAM_NOT_VALID.getCode(), "用户ID不能为空");
        }
        
        log.info("开始获取用户所有实体: userId={}", userId);
        
        List<Entity> entities = list(new LambdaQueryWrapper<Entity>()
                .eq(Entity::getUserId, userId));
        
        log.info("获取用户所有实体完成: userId={}, 实体数量={}", userId, entities.size());
        
        return entities;
    }

    @Override
    public List<Entity> getEntitiesByType(Long userId, String type) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getUserId, userId)
                   .eq(StringUtils.hasText(type), Entity::getType, type);
        
        // 根据类型进行不同处理
        // 物品类型需排除已丢弃的
        if ("item".equals(type)) {
            queryWrapper.ne(Entity::getStatus, "discarded");
        }
        
        // 根据条件查询
        queryWrapper.eq(Entity::getUserId, userId);
        
        // 排除已丢弃的实体
        queryWrapper.ne(Entity::getStatus, "discarded");
        
        return list(queryWrapper);
    }

    @Override
    public List<Entity> listChildEntities(Long parentId, Long userId) {
        if (parentId == null || userId == null) {
            return Collections.emptyList();
        }
        return entityMapper.listChildren(parentId, userId);
    }

    @Override
    public List<Entity> listEntitiesByUser(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getUserId, userId)
                   .ne(Entity::getStatus, "discarded");
        
        return list(queryWrapper);
    }

    @Override
    public List<Entity> listEntitiesByStatus(String status, Long userId) {
        if (!StringUtils.hasText(status) || userId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getUserId, userId)
                   .eq(Entity::getStatus, status);
        
        return list(queryWrapper);
    }

    @Override
    public List<Entity> listExpiringEntities(Integer days, Long userId) {
        if (days == null || userId == null) {
            return new ArrayList<>();
        }
        
        LocalDate currentDate = LocalDate.now();
        LocalDate expiryThreshold = currentDate.plusDays(days);
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getUserId, userId)
                   .ne(Entity::getStatus, "discarded")
                   .isNotNull(Entity::getWarrantyEndDate)
                   .between(Entity::getWarrantyEndDate, currentDate, expiryThreshold);
        
        return list(queryWrapper);
    }

    @Override
    public List<Entity> listExpiredEntities(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        LocalDate currentDate = LocalDate.now();
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getUserId, userId)
                   .ne(Entity::getStatus, "discarded")
                   .isNotNull(Entity::getWarrantyEndDate)
                   .lt(Entity::getWarrantyEndDate, currentDate);
        
        return list(queryWrapper);
    }

    @Override
    public double sumEntitiesValue(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        List<Entity> entities = list(new LambdaQueryWrapper<Entity>()
                .eq(Entity::getUserId, userId)
                .ne(Entity::getStatus, "discarded")
                .isNotNull(Entity::getPrice));
        
        return entities.stream()
                .filter(e -> e.getPrice() != null && e.getQuantity() != null)
                .map(e -> e.getPrice().multiply(new BigDecimal(e.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    @Override
    public List<Object> statEntitiesByParent(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        List<Object> result = new ArrayList<>();
        
        // 获取所有实体
        List<Entity> allEntities = getEntitiesByUserId(userId);
        
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
    public List<Object> statEntitiesByTag(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        // 获取所有者下的所有标签
        List<Tag> tags = tagService.getTagsByUserId(userId);
        
        return tags.stream().map(tag -> {
            // 获取该标签下的所有实体
            List<Entity> entities = listEntitiesByTag(tag.getId(), userId);
            
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
    public List<Object> statEntitiesByUsageFrequency(Long userId) {
        if (userId == null) {
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
        queryWrapper.eq(Entity::getUserId, userId)
                   .ne(Entity::getStatus, "discarded");
        
        // 统计每种使用频率的实体数量
        for (String frequency : frequencyNameMap.keySet()) {
            long countValue = count(new LambdaQueryWrapper<Entity>()
                    .eq(Entity::getUserId, userId)
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
                .eq(Entity::getUserId, userId)
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
    public List<Entity> listEntitiesByPurchaseDateRange(LocalDate startDate, LocalDate endDate, Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getUserId, userId)
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
    public List<Entity> listEntitiesByPriceRange(Double minPrice, Double maxPrice, Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getUserId, userId)
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
    public List<Entity> listEntitiesByTag(Long tagId, Long userId) {
        if (tagId == null || userId == null) {
            return new ArrayList<>();
        }
        
        return entityMapper.listEntitiesByTagId(tagId, userId);
    }
    
    @Override
    public List<Entity> getEntityTree(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        // 获取所有有效实体
        List<Entity> allEntities = list(new LambdaQueryWrapper<Entity>()
                .eq(Entity::getUserId, userId)
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
                .filter(entity -> entity.getParentId() == 0)
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

    @Override
    public List<Entity> getRecentEntities(Long userId, Integer limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getUserId, userId)
                   .ne(Entity::getStatus, "discarded")  // 排除已丢弃的实体
                   .orderByDesc(Entity::getCreateTime)  // 按创建时间降序排列
                   .last("LIMIT " + limit);  // 限制结果数量
        
        List<Entity> entities = list(queryWrapper);
        
        // 为每个实体加载关联的标签和图片
        entities.forEach(entity -> {
            // 加载标签
            List<Tag> tags = entityTagService.getTagsByEntityId(entity.getId());
            entity.setTags(tags);
            
            // 加载图片（仅加载URL，不加载二进制数据）
            entity.setImages(entityImageService.getImagesByEntityId(entity.getId()));
            
            // 如果有父实体，加载父实体名称
            if (entity.getParentId() != null) {
                Entity parent = getById(entity.getParentId());
                if (parent != null) {
                    entity.setParentName(parent.getName());
                }
            }
        });
        
        return entities;
    }

    @Override
    public List<Entity> getRecentEntitiesByDays(Long userId, Integer days) {
        if (userId == null || days == null || days <= 0) {
            return new ArrayList<>();
        }
        
        // 计算开始日期
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entity::getUserId, userId)
                   .ne(Entity::getStatus, "discarded")
                   .ge(Entity::getCreateTime, startDate)
                   .orderByDesc(Entity::getCreateTime);
        
        return list(queryWrapper);
    }

    /**
     * 根据关键词搜索实体
     *
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 实体列表
     */
    @Override
    public List<Entity> searchEntities(Long userId, String keyword) {
        if (userId == null) {
            log.error("搜索实体时用户ID为空");
            throw new BusinessException(ErrorCode.PARAM_NOT_VALID.getCode(), "用户ID不能为空");
        }
        
        if (!StringUtils.hasText(keyword)) {
            log.warn("搜索关键词为空，返回空结果");
            return Collections.emptyList();
        }
        
        log.info("开始搜索实体: userId={}, keyword={}", userId, keyword);
        
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<Entity>()
            .eq(Entity::getUserId, userId)
            .and(wrapper -> wrapper
                .like(Entity::getName, keyword)
                .or()
                .like(Entity::getCode, keyword)
                .or()
                .like(Entity::getDescription, keyword)
                .or()
                .like(Entity::getSpecification, keyword)
            )
            .ne(Entity::getStatus, "discarded")
            .orderByDesc(Entity::getCreateTime);
        
        List<Entity> entities = list(queryWrapper);
        log.info("搜索实体完成: userId={}, keyword={}, 结果数量={}", userId, keyword, entities.size());
        
        // 加载其他相关信息（标签、图片等）
        enrichEntityDetails(entities);
        
        return entities;
    }
    
    @Override
    public Entity getEntityByBarcode(String barcode, Long userId) {
        if (userId == null) {
            log.error("根据条形码查询实体时用户ID为空");
            throw new BusinessException(ErrorCode.PARAM_NOT_VALID.getCode(), "用户ID不能为空");
        }
        
        if (!StringUtils.hasText(barcode)) {
            log.warn("条形码为空，返回空结果");
            return null;
        }
        
        log.info("开始查询条形码对应的实体: barcode={}, userId={}", barcode, userId);
        
        // 构建查询条件
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<Entity>()
                .eq(Entity::getBarcode, barcode)
                .eq(Entity::getUserId, userId)
                .ne(Entity::getStatus, "discarded")
                .last("LIMIT 1");
        
        // 查询实体
        Entity entity = getOne(queryWrapper);
        
        if (entity != null) {
            log.info("找到条形码对应的实体: barcode={}, entityId={}, name={}", barcode, entity.getId(), entity.getName());
            // 获取实体详情（加载标签、图片等信息）
            return getEntityDetail(entity.getId());
        } else {
            log.warn("未找到条形码对应的实体: barcode={}", barcode);
            return null;
        }
    }
    
    @Override
    public Entity getEntityByQRCode(String qrcode, Long userId) {
        if (userId == null) {
            log.error("根据二维码查询实体时用户ID为空");
            throw new BusinessException(ErrorCode.PARAM_NOT_VALID.getCode(), "用户ID不能为空");
        }
        
        if (!StringUtils.hasText(qrcode)) {
            log.warn("二维码为空，返回空结果");
            return null;
        }
        
        log.info("开始查询二维码对应的实体: qrcode={}, userId={}", qrcode, userId);
        
        // 构建查询条件
        LambdaQueryWrapper<Entity> queryWrapper = new LambdaQueryWrapper<Entity>()
                .eq(Entity::getQrcode, qrcode)
                .eq(Entity::getUserId, userId)
                .ne(Entity::getStatus, "discarded")
                .last("LIMIT 1");
        
        // 查询实体
        Entity entity = getOne(queryWrapper);
        
        if (entity != null) {
            log.info("找到二维码对应的实体: qrcode={}, entityId={}, name={}", qrcode, entity.getId(), entity.getName());
            // 获取实体详情（加载标签、图片等信息）
            return getEntityDetail(entity.getId());
        } else {
            log.warn("未找到二维码对应的实体: qrcode={}", qrcode);
            return null;
        }
    }
} 