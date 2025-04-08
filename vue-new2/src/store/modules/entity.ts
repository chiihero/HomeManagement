import { defineStore } from 'pinia'
import type { Entity, EntityFormData, EntityQueryParams, EntityStats } from '@/types/entity'
import {
  getEntities,
  getEntityTree,
  getEntity,
  createEntity,
  updateEntity,
  deleteEntity,
  getEntityStats,
  getAllTags,
  getAllTypes,
  getAllLocations,
  batchUpdateStatus,
  batchDeleteEntities,
  searchEntities,
  getEntityHistory,
  restoreEntityVersion
} from '@/api/entity'

interface EntityState {
  loading: boolean
  treeData: Entity[]
  currentEntity: Entity | null
  entityList: Entity[]
  total: number
  stats: EntityStats | null
  tags: string[]
  types: string[]
  locations: string[]
  history: {
    id: string
    entityId: string
    action: string
    changes: Record<string, any>
    operator: string
    createdAt: string
  }[]
}

export const useEntityStore = defineStore('entity', {
  state: (): EntityState => ({
    loading: false,
    treeData: [],
    currentEntity: null,
    entityList: [],
    total: 0,
    stats: null,
    tags: [],
    types: [],
    locations: [],
    history: []
  }),

  getters: {
    // 获取物品树
    getTreeData: (state) => state.treeData,
    // 获取当前物品
    getCurrentEntity: (state) => state.currentEntity,
    // 获取物品列表
    getEntityList: (state) => state.entityList,
    // 获取物品总数
    getTotal: (state) => state.total,
    // 获取物品统计
    getStats: (state) => state.stats,
    // 获取所有标签
    getTags: (state) => state.tags,
    // 获取所有类型
    getTypes: (state) => state.types,
    // 获取所有位置
    getLocations: (state) => state.locations,
    // 获取物品历史
    getHistory: (state) => state.history
  },

  actions: {
    // 设置加载状态
    setLoading(loading: boolean) {
      this.loading = loading
    },

    // 设置当前物品
    setCurrentEntity(entity: Entity | null) {
      this.currentEntity = entity
    },

    // 获取物品树
    async fetchTreeData() {
      this.setLoading(true)
      try {
        const res = await getEntityTree()
        this.treeData = res.data
      } catch (error) {
        console.error('获取物品树失败:', error)
      } finally {
        this.setLoading(false)
      }
    },

    // 获取物品列表
    async fetchEntityList(params: EntityQueryParams) {
      this.setLoading(true)
      try {
        const res = await getEntities(params)
        this.entityList = res.data.list
        this.total = res.data.total
      } catch (error) {
        console.error('获取物品列表失败:', error)
      } finally {
        this.setLoading(false)
      }
    },

    // 获取物品详情
    async fetchEntityDetail(id: string) {
      this.setLoading(true)
      try {
        const res = await getEntity(id)
        this.currentEntity = res.data
      } catch (error) {
        console.error('获取物品详情失败:', error)
      } finally {
        this.setLoading(false)
      }
    },

    // 创建物品
    async createEntity(data: EntityFormData) {
      this.setLoading(true)
      try {
        const res = await createEntity(data)
        this.currentEntity = res.data
        await this.fetchTreeData()
        return res.data
      } catch (error) {
        console.error('创建物品失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    },

    // 更新物品
    async updateEntity(id: string, data: EntityFormData) {
      this.setLoading(true)
      try {
        const res = await updateEntity(id, data)
        this.currentEntity = res.data
        await this.fetchTreeData()
        return res.data
      } catch (error) {
        console.error('更新物品失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    },

    // 删除物品
    async deleteEntity(id: string) {
      this.setLoading(true)
      try {
        await deleteEntity(id)
        if (this.currentEntity?.id === id) {
          this.currentEntity = null
        }
        await this.fetchTreeData()
      } catch (error) {
        console.error('删除物品失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    },

    // 获取物品统计
    async fetchEntityStats() {
      this.setLoading(true)
      try {
        const res = await getEntityStats()
        this.stats = res.data
      } catch (error) {
        console.error('获取物品统计失败:', error)
      } finally {
        this.setLoading(false)
      }
    },

    // 获取所有标签
    async fetchAllTags() {
      try {
        const res = await getAllTags()
        this.tags = res.data
      } catch (error) {
        console.error('获取标签列表失败:', error)
      }
    },

    // 获取所有类型
    async fetchAllTypes() {
      try {
        const res = await getAllTypes()
        this.types = res.data
      } catch (error) {
        console.error('获取类型列表失败:', error)
      }
    },

    // 获取所有位置
    async fetchAllLocations() {
      try {
        const res = await getAllLocations()
        this.locations = res.data
      } catch (error) {
        console.error('获取位置列表失败:', error)
      }
    },

    // 批量更新状态
    async updateBatchStatus(ids: string[], status: string) {
      this.setLoading(true)
      try {
        await batchUpdateStatus(ids, status)
        await this.fetchTreeData()
      } catch (error) {
        console.error('批量更新状态失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    },

    // 批量删除
    async deleteBatchEntities(ids: string[]) {
      this.setLoading(true)
      try {
        await batchDeleteEntities(ids)
        if (this.currentEntity && ids.includes(this.currentEntity.id)) {
          this.currentEntity = null
        }
        await this.fetchTreeData()
      } catch (error) {
        console.error('批量删除失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    },

    // 搜索物品
    async searchEntities(keyword: string) {
      this.setLoading(true)
      try {
        const res = await searchEntities(keyword)
        this.entityList = res.data
      } catch (error) {
        console.error('搜索物品失败:', error)
      } finally {
        this.setLoading(false)
      }
    },

    // 获取物品历史
    async fetchEntityHistory(id: string) {
      this.setLoading(true)
      try {
        const res = await getEntityHistory(id)
        this.history = res.data
      } catch (error) {
        console.error('获取物品历史失败:', error)
      } finally {
        this.setLoading(false)
      }
    },

    // 恢复历史版本
    async restoreVersion(id: string, historyId: string) {
      this.setLoading(true)
      try {
        const res = await restoreEntityVersion(id, historyId)
        this.currentEntity = res.data
        await this.fetchTreeData()
        return res.data
      } catch (error) {
        console.error('恢复历史版本失败:', error)
        throw error
      } finally {
        this.setLoading(false)
      }
    }
  }
}) 