<template>
  <div class="tag-container bg-gray-50 min-h-screen p-4 md:p-6">
    <el-card class="header-card mb-6 border-0 shadow-sm">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <h1 class="text-xl md:text-2xl font-bold text-gray-800 m-0">
          <el-icon class="mr-2 text-primary"><Promotion /></el-icon>标签管理
        </h1>
        <div class="flex items-center gap-2">
          <el-button type="primary" class="flex items-center gap-2" @click="openTagForm(null)">
            <el-icon><Plus /></el-icon>添加标签
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 标签表格 -->
    <el-card class="table-card border-0 shadow-sm">
      <el-table
        v-loading="loading"
        :data="tagList"
        border
        stripe
        class="w-full"
      >
        <el-table-column type="index" width="50" />
        <el-table-column prop="name" label="标签名称" min-width="150">
          <template #default="{ row }">
            <el-tag
              :style="{ backgroundColor: row.color, color: getContrastColor(row.color) }"
            >
              {{ row.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="color" label="颜色" width="100">
          <template #default="{ row }">
            <div class="color-box rounded" :style="{ backgroundColor: row.color }"></div>
          </template>
        </el-table-column>
        <el-table-column prop="entityCount" label="使用数量" width="120" />
        <el-table-column prop="createdTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openTagForm(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-popconfirm
              title="确定删除此标签吗？"
              @confirm="handleDelete(row)"
              confirm-button-text="确定"
              cancel-button-text="取消"
            >
              <template #reference>
                <el-button type="danger" link>
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 标签表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formType === 'add' ? '添加标签' : '编辑标签'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="tagFormRef"
        :model="tagForm"
        :rules="rules"
        label-width="80px"
        @submit.prevent
      >
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称"></el-input>
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <div class="flex items-center">
            <el-color-picker v-model="tagForm.color"></el-color-picker>
            <div class="color-preview rounded ml-3" :style="{ backgroundColor: tagForm.color }"></div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTagForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Edit, Delete, DataAnalysis, Promotion, Refresh } from '@element-plus/icons-vue';
import { Tag } from '@/types/entity';
import { useUserStore } from '@/store/modules/user';
import { getTags, createTag, updateTag, deleteTag } from '@/api/tag';
import { getContrastColor,  rgbaToHex  } from "@/utils/color";
import { formatDateTime } from "@/utils/date";

const userStore = useUserStore();

const loading = ref(false);
const tagList = ref<Tag[]>([]);
const dialogVisible = ref(false);
const formType = ref<'add' | 'edit'>('add');
const tagFormRef = ref();

// 分页参数
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 标签表单
const tagForm = reactive({
  id: undefined as number | undefined,
  name: '',
  color: '#409EFF',
  userId: undefined as number | undefined
});

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请选择标签颜色', trigger: 'change' }
  ]
};

// 加载标签列表
const loadTagList = async () => {
  if (!userStore.currentUser?.id) {
    console.error('用户未登录或缺少ID');
    return;
  }
  
  loading.value = true;
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      userId: userStore.currentUser.id
    };
    
    console.log('请求标签列表，参数:', params);
    const response = await getTags(params);
    console.log('标签列表响应:', response);
    
    if (response.code === 200 && response.data) {
      if (response.data.records) {
        tagList.value = response.data.records;
        pagination.total = response.data.total;
      } else if (Array.isArray(response.data)) {
        tagList.value = response.data;
        pagination.total = response.data.length;
      } else {
        tagList.value = [];
        pagination.total = 0;
      }
      
      if (tagList.value.length === 0) {
        ElMessage.info('暂无标签数据，请添加');
      }
    } else {
      console.error('加载标签列表失败', response);
      ElMessage.error(response.message || '加载标签列表失败');
      tagList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('加载标签列表失败', error);
    ElMessage.error('加载标签列表失败，请检查网络连接');
    tagList.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 打开标签表单
const openTagForm = (tag: Tag | null) => {
  if (tag) {
    formType.value = 'edit';
    Object.assign(tagForm, tag);
  } else {
    formType.value = 'add';
    tagForm.id = undefined;
    tagForm.name = '';
    tagForm.color = '#409EFF';
  }
  tagForm.userId = userStore.currentUser?.id;
  dialogVisible.value = true;
};

// 提交标签表单
const submitTagForm = async () => {
  if (!tagFormRef.value) return;
  
  // 确保颜色格式为十六进制
  if (tagForm.color.startsWith('rgba')) {
    tagForm.color = rgbaToHex(tagForm.color);
  }
  
  await tagFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (formType.value === 'add') {
          // 确保userId不为undefined
          if (tagForm.userId) {
            const tagData = {
              name: tagForm.name,
              color: tagForm.color,
              userId: tagForm.userId
            };
            const response = await createTag(tagData);
            if (response.code === 200) {
              ElMessage.success('添加成功');
              dialogVisible.value = false;
              loadTagList();
            } else {
              ElMessage.error(response.message || '添加失败');
            }
          } else {
            ElMessage.error('用户信息缺失');
          }
        } else {
          // 更新标签
          if (tagForm.id) {
            const response = await updateTag(String(tagForm.id), {
              name: tagForm.name,
              color: tagForm.color
            });
            if (response.code === 200) {
              ElMessage.success('更新成功');
              dialogVisible.value = false;
              loadTagList();
            } else {
              ElMessage.error(response.message || '更新失败');
            }
          }
        }
      } catch (error) {
        console.error('提交表单失败', error);
        ElMessage.error('操作失败');
      }
    }
  });
};

// 删除标签
const handleDelete = async (tag: Tag) => {
  try {
    const response = await deleteTag(String(tag.id));
    if (response.code === 200) {
      ElMessage.success('删除成功');
      loadTagList();
    } else {
      ElMessage.error(response.message || '删除失败');
    }
  } catch (error) {
    console.error('删除标签失败', error);
    ElMessage.error('删除失败');
  }
};

// 处理页码改变
const handleCurrentChange = (current: number) => {
  pagination.current = current;
  loadTagList();
};

// 处理每页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size;
  pagination.current = 1;
  loadTagList();
};





// 监听用户登录状态变化
watch(
  () => userStore.isAuthenticated,
  (isAuthenticated) => {
    if (isAuthenticated) {
      loadTagList();
    }
  }
);

onMounted(() => {
  if (userStore.isAuthenticated) {
    loadTagList();
  }
});
</script>

<style scoped>
.tag-container {
  width: 100%;
}

.header-card {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(5px);
}

.table-card {
  background-color: white;
  transition: all 0.3s ease;
}

.table-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.color-box {
  width: 20px;
  height: 20px;
  display: inline-block;
}

.color-preview {
  width: 80px;
  height: 30px;
  display: inline-block;
  vertical-align: middle;
}

:deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

/* 自定义滚动条样式 */
.table-card :deep(.el-card__body) {
  max-height: calc(100vh - 270px);
  overflow-y: auto;
}

.table-card :deep(.el-card__body::-webkit-scrollbar) {
  width: 4px;
}

.table-card :deep(.el-card__body::-webkit-scrollbar-thumb) {
  background-color: #e4e4e7;
  border-radius: 4px;
}

.table-card :deep(.el-card__body::-webkit-scrollbar-track) {
  background-color: #f8fafc;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .el-pagination {
    justify-content: center;
  }
}

@media screen and (max-width: 480px) {
  .color-preview {
    width: 60px;
  }
}
</style> 