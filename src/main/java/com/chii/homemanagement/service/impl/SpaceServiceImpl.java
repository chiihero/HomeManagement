package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Space;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.mapper.SpaceMapper;
import com.chii.homemanagement.service.SpaceService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 空间服务实现类
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Override
    public IPage<Space> pageSpaces(Page<Space> page, Space space) {
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (space != null) {
            // 按名称模糊查询
            if (StringUtils.hasText(space.getName())) {
                queryWrapper.like(Space::getName, space.getName());
            }

            // 按父ID查询
            if (space.getParentId() != null) {
                queryWrapper.eq(Space::getParentId, space.getParentId());
            }

            // 按家庭ID查询
            if (space.getFamilyId() != null) {
                queryWrapper.eq(Space::getFamilyId, space.getFamilyId());
            }
        }

        // 按排序字段排序
        queryWrapper.orderByAsc(Space::getSort);

        return page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSpace(Space space) {
        // 设置创建者ID
        if (space.getCreateUserId() == null) {
            // 从Spring Security获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                try {
                    // 通过反射获取SecurityContextHolder中的用户实体
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof User) {
                        User user = (User) principal;
                        space.setCreateUserId(user.getId());
                    } else {
                        // 默认ID
                        space.setCreateUserId(1L);
                    }
                } catch (Exception e) {
                    // 异常情况下使用默认ID
                    space.setCreateUserId(1L);
                }
            } else {
                // 如果未认证，使用默认ID
                space.setCreateUserId(1L);
            }
        }

        // 设置创建时间
        space.setCreateTime(LocalDateTime.now());
        space.setUpdateTime(LocalDateTime.now());

        // 设置层级
        if (space.getParentId() == null || space.getParentId() == 0) {
            // 根节点
            space.setLevel(1);
            space.setPath("");
        } else {
            // 子节点，查询父节点
            Space parentSpace = getById(space.getParentId());
            if (parentSpace != null) {
                space.setLevel(parentSpace.getLevel() + 1);
            } else {
                // 父节点不存在，设为根节点
                space.setParentId(0L);
                space.setLevel(1);
                space.setPath("");
            }
        }

        // 保存空间
        boolean result = save(space);

        // 更新路径
        if (result && (space.getParentId() == null || space.getParentId() == 0)) {
            // 根节点路径为自身ID
            space.setPath(String.valueOf(space.getId()));
        } else if (result) {
            // 子节点路径为父节点路径,自身ID
            Space parentSpace = getById(space.getParentId());
            if (parentSpace != null) {
                space.setPath(parentSpace.getPath() + "," + space.getId());
            } else {
                space.setPath(String.valueOf(space.getId()));
            }
        }

        // 更新路径
        return result && updateById(space);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSpace(Space space) {
        // 设置更新时间
        space.setUpdateTime(LocalDateTime.now());

        // 获取原空间信息
        Space oldSpace = getById(space.getId());
        if (oldSpace == null) {
            return false;
        }

        // 父节点发生变化，需要更新层级和路径
        if (!Objects.equals(oldSpace.getParentId(), space.getParentId())) {
            // 设置层级
            if (space.getParentId() == null || space.getParentId() == 0) {
                // 变为根节点
                space.setLevel(1);
                space.setPath(String.valueOf(space.getId()));
            } else {
                // 变为子节点，查询新父节点
                Space parentSpace = getById(space.getParentId());
                if (parentSpace != null) {
                    space.setLevel(parentSpace.getLevel() + 1);
                    space.setPath(parentSpace.getPath() + "," + space.getId());
                } else {
                    // 父节点不存在，设为根节点
                    space.setParentId(0L);
                    space.setLevel(1);
                    space.setPath(String.valueOf(space.getId()));
                }
            }

            // 更新子节点的层级和路径
            updateChildrenLevelAndPath(space.getId(), space.getLevel(), space.getPath());
        }

        return updateById(space);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeSpace(Long id) {
        // 查询是否有子空间
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Space::getParentId, id);
        long count = count(queryWrapper);

        // 有子空间，不允许删除
        if (count > 0) {
            return false;
        }

        // 删除空间
        return removeById(id);
    }

    @Override
    public List<Space> getSpaceTree(Long familyId) {
        // 查询家庭下所有空间
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Space::getFamilyId, familyId);
        queryWrapper.orderByAsc(Space::getSort);

        return list(queryWrapper);
    }

    @Override
    public List<Space> getChildrenSpaces(Long parentId, Long familyId) {
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Space::getParentId, parentId);
        queryWrapper.eq(Space::getFamilyId, familyId);
        queryWrapper.orderByAsc(Space::getSort);

        return list(queryWrapper);
    }

    @Override
    public List<Space> getRootSpaces(Long familyId) {
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Space::getFamilyId, familyId);
        queryWrapper.and(wrapper -> wrapper.isNull(Space::getParentId).or().eq(Space::getParentId, 0));
        queryWrapper.orderByAsc(Space::getSort);

        return list(queryWrapper);
    }

    /**
     * 更新子节点的层级和路径
     *
     * @param parentId    父节点ID
     * @param parentLevel 父节点层级
     * @param parentPath  父节点路径
     */
    private void updateChildrenLevelAndPath(Long parentId, Integer parentLevel, String parentPath) {
        // 查询所有子节点
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Space::getParentId, parentId);
        List<Space> children = list(queryWrapper);

        if (children.isEmpty()) {
            return;
        }

        // 更新子节点
        for (Space child : children) {
            // 更新层级
            child.setLevel(parentLevel + 1);
            // 更新路径
            child.setPath(parentPath + "," + child.getId());
            // 更新子节点
            updateById(child);

            // 递归更新子节点的子节点
            updateChildrenLevelAndPath(child.getId(), child.getLevel(), child.getPath());
        }
    }
}