package com.chii.homemanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chii.homemanagement.entity.Space;

import java.util.List;

/**
 * 空间服务接口
 */
public interface SpaceService extends IService<Space> {

    /**
     * 分页查询空间列表
     *
     * @param page  分页参数
     * @param space 查询条件
     * @return 分页结果
     */
    IPage<Space> pageSpaces(Page<Space> page, Space space);

    /**
     * 新增空间
     *
     * @param space 空间信息
     * @return 是否成功
     */
    boolean saveSpace(Space space);

    /**
     * 更新空间
     *
     * @param space 空间信息
     * @return 是否成功
     */
    boolean updateSpace(Space space);

    /**
     * 删除空间
     *
     * @param id 空间ID
     * @return 是否成功
     */
    boolean removeSpace(Long id);

    /**
     * 获取空间树结构
     *
     * @param familyId 家庭ID
     * @return 空间树结构
     */
    List<Space> getSpaceTree(Long familyId);

    /**
     * 获取子空间列表
     *
     * @param parentId 父空间ID
     * @param familyId 家庭ID
     * @return 子空间列表
     */
    List<Space> getChildrenSpaces(Long parentId, Long familyId);

    /**
     * 获取根空间列表
     *
     * @param familyId 家庭ID
     * @return 根空间列表
     */
    List<Space> getRootSpaces(Long familyId);
}