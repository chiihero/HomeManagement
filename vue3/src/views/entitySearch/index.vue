<template>
  <div class="bg-gray-50 min-h-screen p-4 md:p-6">
    <!-- 头部 -->
    <el-card class="mb-6 shadow-sm border-0">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <h1 class="text-xl md:text-2xl font-bold text-gray-800 m-0">
          <el-icon class="mr-2 text-primary"><Search /></el-icon>物品搜索
        </h1>
      </div>
    </el-card>

    <!-- 搜索条件 -->
    <el-card class="mb-6 shadow-sm border-0">
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
          <el-button type="primary" class="mr-2" @click="handleSearch">
            <el-icon class="mr-1"><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">
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
          <div class="text-sm text-gray-500">共 {{ pagination.total }} 项</div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="entityList"
        border
        stripe
        class="w-full"
        @row-click="handleRowClick"
      >
        <el-table-column type="index" width="50" />
        <el-table-column
          prop="name"
          label="名称"
          min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div class="flex items-center">
              <el-image
                v-if="row.images && row.images[0]"
                :src="imageUrlCache[row.images[0].id]"
                fit="cover"
                class="w-8 h-8 rounded mr-2 object-cover"
              >
                <template #error>
                  <div
                    class="w-8 h-8 rounded mr-2 bg-gray-100 flex items-center justify-center text-gray-400"
                  >
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div
                v-else
                class="w-8 h-8 rounded mr-2 bg-gray-100 flex items-center justify-center text-gray-400"
              >
                <el-icon><Document /></el-icon>
              </div>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.type === 'item' ? 'success' : 'primary'"
              size="small"
            >
              {{ row.type === "item" ? "物品" : "空间" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="specification"
          label="规格"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            {{ row.price ? "¥" + row.price.toFixed(2) : "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status] || 'info'" size="small">
              {{ statusTextMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usageFrequency" label="使用频率" width="100">
          <template #default="{ row }">
            {{ usageFrequencyMap[row.usageFrequency] || row.usageFrequency }}
          </template>
        </el-table-column>
        <el-table-column
          prop="tags"
          label="标签"
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
        <el-table-column prop="expirationDate" label="过保日期" width="120">
          <template #default="{ row }">
            {{ row.expirationDate ? formatDate(row.expirationDate) : "-" }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="handleViewDetail(row)">
              <el-icon class="mr-1"><View /></el-icon>查看
            </el-button>
            <el-button type="primary" link size="small" @click.stop="handleEdit(row)">
              <el-icon class="mr-1"><Edit /></el-icon>编辑
            </el-button>
            <el-popconfirm
              title="确定删除此实体吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small" @click.stop>
                  <el-icon class="mr-1"><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="物品详情"
      width="650px"
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
      :title="编辑物品"
      width="750px"
      :destroy-on-close="true"
      :close-on-click-modal="false"
      :before-close="handleFormDialogClose"
    >
      <entity-form
        v-if="formDialogVisible"
        :entity="currentEntity"
        :tree-data="treeData"
        :existing-tags="tagList"
        :is-editing="true"
        :saving="formSaving"
        @submit="handleFormSubmit"
        @cancel="formDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { pageEntities, getEntitiesByUser, getEntity, deleteEntity, updateEntity, createEntity } from "@/api/entity";
import { getImageData } from "@/api/image";
import { getAllTags } from "@/api/tag";

import type { Entity, Tag } from "@/types/entity";
import {
  Search,
  Refresh,
  Edit,
  Delete,
  View,
  Picture,
  Document,
  Plus
} from "@element-plus/icons-vue";
import moment from "moment";

// 导入组件
import EntityDetail from "@/views/entity/components/EntityDetail.vue";
import EntityForm from "@/views/entity/components/EntityForm.vue";

const router = useRouter();
const userStore = useUserStoreHook();

const loading = ref(false);
const entityList = ref<Entity[]>([]);
const spaceList = ref<Entity[]>([]);
const imageUrlCache = ref<Record<number, string>>({});

// 分页参数
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 搜索表单
const searchForm = reactive({
  name: "",
  type: "",
  specification: "",
  status: "",
  usageFrequency: "",
  userId: userStore.currentUser.id || "",
  parentId: "" as string | undefined
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
  high: "高",
  medium: "中",
  low: "低",
  rare: "很少",
  never: "从不",
  rarely: "很少",
  occasionally: "偶尔",
  frequently: "经常",
  daily: "每天"
};

// 弹窗相关状态
const detailDialogVisible = ref(false);
const formDialogVisible = ref(false);
const detailLoading = ref(false);
const formSaving = ref(false);
const currentEntity = ref<Entity | null>(null);
const treeData = ref<Entity[]>([]);
const tagList = ref<Tag[]>([]);

// 获取图片URL
const getImageUrl = async (imageId: number) => {
  if (imageUrlCache.value[imageId]) {
    return imageUrlCache.value[imageId];
  }

  try {
    // 根据API需要转换为字符串类型
    const blob = await getImageData(String(imageId));
    const url = URL.createObjectURL(blob);
    imageUrlCache.value[imageId] = url;
    return url;
  } catch (error) {
    console.error("获取图片数据失败:", error);
    return "";
  }
};

// 加载实体图片
const loadEntityImage = async (entity: Entity) => {
  if (entity.images && entity.images[0] && entity.images[0].id) {
    await getImageUrl(entity.images[0].id);
  }
};

// 加载实体列表
const loadEntityList = async () => {
  if (!userStore.currentUser?.id) return;

  loading.value = true;
  try {
    const response = await pageEntities({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    });

    if (response.data) {
      // 适配不同的API返回结构
      entityList.value = response.data.records || response.data.list || [];
      pagination.total = response.data.total || 0;

      // 加载每个实体的第一张图片
      for (const entity of entityList.value) {
        await loadEntityImage(entity);
      }
    }
  } catch (error) {
    console.error("加载实体列表失败:", error);
    ElMessage.error("加载实体列表失败，请检查网络连接");
  } finally {
    loading.value = false;
  }
};

// 加载搜索的空间列表
const loadSpaceList = async () => {
  if (!userStore.currentUser?.id) return;

  try {
    const response = await getEntitiesByUser(userStore.currentUser.id);

    if (response.data) {
      spaceList.value = response.data;
    }
  } catch (error) {
    console.error("加载空间列表失败:", error);
  }
};

// 处理搜索
const handleSearch = () => {
  pagination.current = 1;
  loadEntityList();
};

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    name: "",
    type: "",
    specification: "",
    status: "",
    usageFrequency: "",
    parentId: ""
  });
  pagination.current = 1;
  loadEntityList();
};

// 查看详情
const handleViewDetail = async (row: Entity) => {
  detailLoading.value = true;
  currentEntity.value = null;
  detailDialogVisible.value = true;
  
  try {
    const response = await getEntity(row.id);
    if (response.data) {
      // 确保类型正确
      currentEntity.value = response.data as unknown as Entity;
    }
  } catch (error) {
    console.error("获取实体详情失败:", error);
    ElMessage.error("获取实体详情失败");
  } finally {
    detailLoading.value = false;
  }
};

// 编辑实体
const handleEdit = async (row: Entity) => {
  if (detailDialogVisible.value) {
    detailDialogVisible.value = false;
    await nextTick();
  }
  
  formSaving.value = false;
  currentEntity.value = null;
  
  try {
    // 获取最新实体数据
    const entityResponse = await getEntity(row.id);
    if (entityResponse.data) {
      // 确保类型正确
      currentEntity.value = entityResponse.data as unknown as Entity;
    }
    
    // 获取树形数据和标签列表
    await Promise.all([loadTreeData(), loadTagList()]);
    
    formDialogVisible.value = true;
  } catch (error) {
    console.error("获取编辑数据失败:", error);
    ElMessage.error("获取编辑数据失败");
  }
};

// 加载树形数据
const loadTreeData = async () => {
  if (!userStore.currentUser?.id) return;
  
  try {
    const response = await getEntitiesByUser(userStore.currentUser.id);
    if (response.data) {
      treeData.value = response.data;
    }
  } catch (error) {
    console.error("加载树形数据失败:", error);
  }
};

// 加载标签列表
const loadTagList = async () => {
  if (!userStore.currentUser?.id) return;
  
  try {
    // 使用getAllTags获取标签列表
    const response = await getAllTags(userStore.currentUser.id);
    if (response.data) {
      tagList.value = response.data;
    }
  } catch (error) {
    console.error("加载标签列表失败:", error);
  }
};

// 处理表单提交
const handleFormSubmit = async (form: Entity) => {
  formSaving.value = true;
  
  try {
    let response;
    if (currentEntity.value?.id) {
      response = await updateEntity(currentEntity.value?.id, form);
    }
    
    if (response && response.data) {
      ElMessage.success( "更新成功" );
      formDialogVisible.value = false;
      loadEntityList(); // 刷新列表
    }
  } catch (error) {
    console.error("更新失败:", error);
    ElMessage.error("更新失败");
  } finally {
    formSaving.value = false;
  }
};

// 处理删除
const handleDelete = async (row: Entity) => {
  try {
    await deleteEntity(row.id);
    ElMessage.success("删除成功");
    loadEntityList(); // 刷新列表
  } catch (error) {
    console.error("删除失败:", error);
    ElMessage.error("删除失败");
  }
};

// 关闭编辑弹窗前确认
const handleFormDialogClose = (done: () => void) => {
  if (formSaving.value) {
    return;
  }
  
  ElMessageBox.confirm("确定要关闭吗？未保存的数据将会丢失", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  })
    .then(() => {
      done();
    })
    .catch(() => {});
};

// 处理表格行点击
const handleRowClick = (row: Entity) => {
  handleViewDetail(row);
};

// 处理分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size;
  loadEntityList();
};

// 处理当前页改变
const handleCurrentChange = (current: number) => {
  pagination.current = current;
  loadEntityList();
};

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return "-";
  return moment(date).format("YYYY-MM-DD");
};

// 获取标签文字颜色
const getContrastColor = (bgColor: string) => {
  if (!bgColor) return "#ffffff";

  // 将十六进制颜色转换为RGB
  let color = bgColor.charAt(0) === "#" ? bgColor.substring(1) : bgColor;
  let r = parseInt(color.substr(0, 2), 16);
  let g = parseInt(color.substr(2, 2), 16);
  let b = parseInt(color.substr(4, 2), 16);

  // 计算亮度
  let yiq = (r * 299 + g * 587 + b * 114) / 1000;

  // 如果亮度高于128，返回黑色，否则返回白色
  return yiq >= 128 ? "#000000" : "#ffffff";
};

// 新增物品
const handleAddEntity = async () => {
  isEditing.value = false;
  formSaving.value = false;
  currentEntity.value = null;
  
  try {
    // 获取树形数据和标签列表
    await Promise.all([loadTreeData(), loadTagList()]);
    
    formDialogVisible.value = true;
  } catch (error) {
    console.error("获取新增数据失败:", error);
    ElMessage.error("获取新增数据失败");
  }
};

// 在组件挂载时加载数据
onMounted(() => {
  loadEntityList();
  // 加载空间列表用于筛选
  loadSpaceList();
});
</script>
