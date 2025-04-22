<template>
  <div class="bg-gray-50 min-h-screen">
    <!-- 头部 -->
    <el-card class="mb-4 shadow-sm border-0">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <h1 class="text-xl md:text-2xl font-bold text-gray-800 m-0">
          <el-icon class="mr-2 text-primary"><Search /></el-icon>物品搜索
        </h1>
      </div>
    </el-card>

    <!-- 搜索条件 -->
    <el-card class="mb-4 shadow-sm border-0">
      <template #header>
        <div class="flex items-center">
          <span class="text-gray-700 font-medium">搜索条件</span>
        </div>
      </template>
      <el-form :model="searchForm" label-width="100px" @submit.prevent>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="名称">
              <el-input
                v-model="searchForm.name"
                placeholder="请输入名称"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="类型">
              <el-select
                v-model="searchForm.type"
                placeholder="请选择类型"
                clearable
                class="w-full"
              >
                <el-option label="物品" value="item" />
                <el-option label="空间" value="space" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.status"
                placeholder="请选择状态"
                clearable
                class="w-full"
              >
                <el-option label="正常" value="normal" />
                <el-option label="损坏" value="damaged" />
                <el-option label="丢弃" value="discarded" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="使用频率">
              <el-select
                v-model="searchForm.usageFrequency"
                placeholder="请选择使用频率"
                clearable
                class="w-full"
              >
                <el-option label="高" value="high" />
                <el-option label="中" value="medium" />
                <el-option label="低" value="low" />
                <el-option label="很少" value="rare" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="所在空间">
              <el-select
                v-model="searchForm.parentId"
                placeholder="请选择所在空间"
                clearable
                class="w-full"
              >
                <el-option
                  v-for="space in spaceList"
                  :key="space.id"
                  :label="space.name"
                  :value="space.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="flex justify-end">
          <el-button type="primary" size="large" class="mr-2" @click="handleSearch">
            <el-icon class="mr-1"><Search /></el-icon>搜索
          </el-button>
          <el-button size="large" @click="resetSearch">
            <el-icon class="mr-1"><Refresh /></el-icon>重置
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="shadow-sm border-0">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-gray-700 font-medium">物品列表</span>
          <div class="text-sm text-gray-500">共 {{ paginationConfig.total }} 项</div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="entityList"
        border
        stripe
        max-height="350"
        class="w-full"
        @row-click="handleRowClick"
      >
        <el-table-column type="index" label="序号" align="center" width="50" />
        <el-table-column
          prop="name"
          label="名称"
          min-width="150"
          show-overflow-tooltip
        >
        </el-table-column>
        <el-table-column prop="type" align="center" label="类型" width="100">
          <template #default="{ row }">
            <el-tag
              class="primary"
              size="small"
            >
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" align="center" label="价格" width="120">
          <template #default="{ row }">
            {{ row.price ? "¥" + row.price.toFixed(2) : "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="status" align="center" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status] || 'info'" size="small">
              {{ statusTextMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usageFrequency" align="center" label="使用频率" width="100">
          <template #default="{ row }">
            {{ usageFrequencyMap[row.usageFrequency] || row.usageFrequency }}
          </template>
        </el-table-column>
        <el-table-column
          prop="tags"
          label="标签"
          align="center"
          min-width="160"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag
                v-for="tag in row.tags"
                :key="tag.id"
                :style="{
                  backgroundColor: tag.color,
                  color: getContrastColor(tag.color)
                }"
                size="small"
              >
                {{ tag.name }}
              </el-tag>
              <span
                v-if="!row.tags || row.tags.length === 0"
                class="text-gray-400"
                >-</span
              >
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="expirationDate" align="center" label="过保日期" width="120">
          <template #default="{ row }">
            {{ row.expirationDate ? formatDate(row.expirationDate) : "-" }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" align="center" label="操作" width="130">
          <template #default="{ row }">
            <el-button type="primary" link size="small"  @click.stop="handleViewDetail(row)">
              查看
            </el-button>
            <el-button type="primary" link size="small"  @click.stop="handleEdit(row)">
              编辑
            </el-button>
            <el-popconfirm
              title="确定删除此实体吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small"  @click.stop>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-end mt-4">
        <el-pagination
          :current-page="paginationConfig.current"
          :page-size="paginationConfig.size"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="paginationConfig.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="物品详情"
      width="90%"
      :destroy-on-close="true"
      :close-on-click-modal="false"
    >
      <entity-detail
        :loading="detailLoading"
        :entity="currentEntity"
        :tree-data="treeData"
      />
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleEdit(currentEntity)">编辑</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="currentEntity ? '编辑物品' : '添加物品'"
      width="90%"
      :destroy-on-close="true"
      :close-on-click-modal="false"
      :before-close="handleFormDialogClose"
    >
      <entity-form
        v-if="formDialogVisible"
        :entity="currentEntity || null"
        :tree-data="treeData"
        :existing-tags="entityTags"
        :is-editing="isEditing"
        :saving="saving"
        @submit="saveEntity"
        @cancel="cancelEditOrAdd"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useUserStoreHook } from "@/store/modules/user";
import { getEntitiesByUser } from "@/api/entity";
import { getContrastColor  } from "@/utils/color";
import type { Entity } from "@/types/entity";
import {
  Search,
  Refresh,
  Edit,
  Delete,
  View
} from "@element-plus/icons-vue";

// 导入组件和组合函数
import EntityDetail from "@/views/entity/components/EntityDetail.vue";
import EntityForm from "@/views/entity/components/EntityForm.vue";
import { useEntityCRUD } from "@/views/entity/composables/useEntityCRUD";
import { formatDate } from "@/utils/date";

const userStore = useUserStoreHook();
const spaceList = ref<Entity[]>([]);

// 初始化搜索参数
const initialSearchForm = {
  name: "",
  type: "",
  specification: "",
  status: "",
  usageFrequency: "",
  userId: userStore.userId || "",
  parentId: "" as string | undefined
};

// 分页初始配置
const initialPagination = {
  current: 1,
  size: 10,
  total: 0
};

// 使用通用的实体CRUD逻辑
const {
  loading,
  treeData,
  currentEntity,
  isEditing,
  saving,
  detailLoading,
  entityList,
  searchForm,
  paginationConfig,
  formDialogVisible,
  detailDialogVisible,
  entityTags,
  loadTreeData,
  loadAllTags,
  loadEntityList,
  handleViewDetail,
  handleEdit,
  handleDelete,
  saveEntity,
  handleSearch,
  resetSearch,
  handleSizeChange,
  handleCurrentChange,
  handleFormDialogClose,
  cancelEditOrAdd
} = useEntityCRUD({
  isSearchMode: true,
  pagination: initialPagination,
  initialSearchForm
});


// 状态映射
const statusTypeMap = {
  normal: "success",
  damaged: "warning",
  discarded: "danger",
  expired: "danger"
} as const;

const statusTextMap = {
  normal: "正常",
  damaged: "损坏",
  discarded: "丢弃",
  expired: "过期"
};

// 使用频率映射
const usageFrequencyMap = {
  daily: "每天",
  weekly: "每周",
  monthly: "每月",
  rarely: "很少"
};



// 加载搜索的空间列表
const loadSpaceList = async () => {
  if (!userStore.userId) return;

  try {
    const response = await getEntitiesByUser(userStore.userId);
    if (response.data) {
      spaceList.value = response.data;
    }
  } catch (error) {
    console.error("加载空间列表失败:", error);
  }
};

// 处理表格行点击
const handleRowClick = (row: Entity) => {
  handleViewDetail(row);
};



// 在组件挂载时加载数据
onMounted(() => {
  // 加载实体列表
  loadEntityList();
  // 加载标签数据
  loadAllTags();
  // 加载树形结构数据
  loadTreeData();
  // 加载空间列表用于筛选
  loadSpaceList();
});
</script>
<style scoped>
.el-table .cell {
  box-sizing: border-box;
  line-height: 23px;
  overflow: hidden;
  overflow-wrap: break-word;
  text-overflow: ellipsis;
  white-space: normal;
}
.el-button+.el-button {
    margin-left: 4px;
}
</style>