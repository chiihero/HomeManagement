<template>
  <div class="page-container">
    <div class="page-header mb-base">
      <h1 class="page-title m-0">标签管理</h1>
      <div>
        <el-button type="primary" @click="openTagForm(null)">
          <el-icon><Plus /></el-icon>添加标签
        </el-button>
      </div>
    </div>
    
    <!-- 表格 -->
    <el-card shadow="hover">
      <el-table
        v-loading="loading"
        :data="tagList"
        border
        stripe
        class="w-100"
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
            <div class="color-box rounded-small" :style="{ backgroundColor: row.color }"></div>
          </template>
        </el-table-column>
        <el-table-column prop="entityCount" label="使用数量" width="120" />
        <el-table-column prop="createdTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
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
      <div class="mt-medium flex justify-end">
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
          <el-color-picker v-model="tagForm.color" show-alpha></el-color-picker>
          <div class="color-preview rounded-small ml-small" :style="{ backgroundColor: tagForm.color }"></div>
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

<script lang="ts">
import { defineComponent, ref, reactive, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { Tag } from '@/types/entity';
import { PageResult, ResponseResult } from '@/types/common';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import moment from 'moment';
import { getTags, createTag, updateTag, deleteTag } from '@/api/tag';

export default defineComponent({
  name: 'TagList',
  components: {
    Plus,
    Edit,
    Delete
  },
  setup() {
    const authStore = useAuthStore();
    
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
      if (!authStore.currentUser?.id) {
        console.error('用户未登录或缺少ID');
        return;
      }
      
      loading.value = true;
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          userId: authStore.currentUser.id
        };
        
        console.log('请求标签列表，参数:', params);
        // 使用真实API
        const response = await getTags(params);
        console.log('标签列表响应:', response);
        
        // 检查响应格式，适配不同情况
        if (response.code === 200 && response.data) {
          if (Array.isArray(response.data)) {
            // 如果直接返回数组
            tagList.value = response.data as Tag[];
            pagination.total = response.data.length;
          } else if (typeof response.data === 'object') {
            // 根据实际返回的数据结构进行处理
            if ('records' in response.data) {
              // 后端返回的可能是records字段
              const records = (response.data as any).records;
              tagList.value = Array.isArray(records) ? records : [];
              pagination.total = (response.data as any).total || 0;
            } else if ('list' in response.data) {
              // 如果使用common.ts中定义的PageResult<T>接口
              const pageData = response.data as PageResult<Tag>;
              tagList.value = pageData.list || [];
              pagination.total = pageData.total || 0;
            } else {
              console.error('无法识别的响应数据格式', response.data);
              tagList.value = [];
              pagination.total = 0;
            }
          } else {
            console.error('无法识别的响应数据格式', response.data);
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
      tagForm.userId = authStore.currentUser?.id;
      dialogVisible.value = true;
    };
    
    // 提交标签表单
    const submitTagForm = async () => {
      if (!tagFormRef.value) return;
      
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
    
    // 格式化日期时间
    const formatDateTime = (dateTime: string) => {
      return moment(dateTime).format('YYYY-MM-DD HH:mm');
    };
    
    // 获取标签文字颜色
    const getContrastColor = (color: string) => {
      // 简单的对比度算法，根据背景色计算应该使用黑色还是白色文字
      const rgb = hexToRgb(color);
      if (!rgb) return '#ffffff';
      
      const brightness = (rgb.r * 299 + rgb.g * 587 + rgb.b * 114) / 1000;
      return brightness > 128 ? '#000000' : '#ffffff';
    };
    
    // 将十六进制颜色转换为RGB
    const hexToRgb = (hex: string) => {
      // 处理rgba格式
      if (hex.startsWith('rgba')) {
        const matches = hex.match(/rgba\((\d+),\s*(\d+),\s*(\d+),\s*[\d.]+\)/);
        if (matches) {
          return {
            r: parseInt(matches[1], 10),
            g: parseInt(matches[2], 10),
            b: parseInt(matches[3], 10)
          };
        }
        return null;
      }
      
      // 处理十六进制格式
      const shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
      hex = hex.replace(shorthandRegex, (m, r, g, b) => r + r + g + g + b + b);
      
      const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
      return result
        ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16)
          }
        : null;
    };
    
    // 监听用户登录状态变化
    watch(
      () => authStore.isAuthenticated,
      (isAuthenticated) => {
        if (isAuthenticated) {
          loadTagList();
        }
      }
    );
    
    onMounted(() => {
      if (authStore.isAuthenticated) {
        loadTagList();
      }
    });
    
    return {
      loading,
      tagList,
      pagination,
      dialogVisible,
      formType,
      tagForm,
      tagFormRef,
      rules,
      openTagForm,
      submitTagForm,
      handleDelete,
      handleCurrentChange,
      handleSizeChange,
      formatDateTime,
      getContrastColor
    };
  }
});
</script>

<style scoped>
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

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-small);
  }
  
  .el-pagination {
    justify-content: center;
  }
}

@media screen and (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }
  
  .el-dialog {
    width: 90% !important;
  }
  
  .color-preview {
    width: 60px;
  }
}
</style> 