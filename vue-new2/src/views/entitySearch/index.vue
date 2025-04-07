<template>
  <div class="bg-gray-50 min-h-screen p-4 md:p-6">
    <!-- 头部 -->
    <el-card class="mb-6 border-0 shadow-sm">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <h1 class="text-xl md:text-2xl font-bold text-gray-800 m-0">
          <el-icon class="mr-2 text-primary"><Search /></el-icon>物品搜索
        </h1>
      </div>
    </el-card>
    
    <!-- 搜索条件 -->
    <el-card class="mb-6 border-0 shadow-sm">
      <template #header>
        <div class="flex items-center">
          <span class="text-gray-700 font-medium">搜索条件</span>
        </div>
      </template>
      <el-form :model="searchForm" label-width="100px" @submit.prevent>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="名称">
              <el-input v-model="searchForm.name" placeholder="请输入名称" clearable @keyup.enter="handleSearch"></el-input>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="类型">
              <el-select v-model="searchForm.type" placeholder="请选择类型" clearable class="w-full">
                <el-option label="物品" value="item"></el-option>
                <el-option label="空间" value="space"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable class="w-full">
                <el-option label="正常" value="normal"></el-option>
                <el-option label="损坏" value="damaged"></el-option>
                <el-option label="丢弃" value="discarded"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="使用频率">
              <el-select v-model="searchForm.usageFrequency" placeholder="请选择使用频率" clearable class="w-full">
                <el-option label="高" value="high"></el-option>
                <el-option label="中" value="medium"></el-option>
                <el-option label="低" value="low"></el-option>
                <el-option label="很少" value="rare"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="所在空间">
              <el-select v-model="searchForm.parentId" placeholder="请选择所在空间" clearable class="w-full">
                <el-option
                  v-for="space in spaceList"
                  :key="space.id"
                  :label="space.name"
                  :value="space.id"
                ></el-option>
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
    <el-card class="border-0 shadow-sm">
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
        <el-table-column prop="name" label="名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="flex items-center">
              <el-image 
                v-if="row.images && row.images[0]" 
                :src="row.images[0].imagePath" 
                fit="cover"
                class="w-8 h-8 rounded mr-2 object-cover"
              >
                <template #error>
                  <div class="w-8 h-8 rounded mr-2 bg-gray-100 flex items-center justify-center text-gray-400">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="w-8 h-8 rounded mr-2 bg-gray-100 flex items-center justify-center text-gray-400">
                <el-icon><Document /></el-icon>
              </div>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'item' ? 'success' : 'primary'" size="small">
              {{ row.type === 'item' ? '物品' : '空间' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格" min-width="120" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            {{ row.price ? '¥' + row.price.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usageFrequency" label="使用频率" width="100">
          <template #default="{ row }">
            {{ getUsageFrequencyText(row.usageFrequency) }}
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag 
                v-for="tag in row.tags" 
                :key="tag.id" 
                :style="{ backgroundColor: tag.color, color: getContrastColor(tag.color) }"
                size="small"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="!row.tags || row.tags.length === 0" class="text-gray-400">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="expirationDate" label="过保日期" width="120">
          <template #default="{ row }">
            {{ row.expirationDate ? formatDate(row.expirationDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link size="small">
              <el-icon class="mr-1"><View /></el-icon>查看
            </el-button>
            <el-button type="primary" link size="small">
              <el-icon class="mr-1"><Edit /></el-icon>编辑
            </el-button>
            <el-popconfirm 
              title="确定删除此实体吗？" 
              confirm-button-text="确定"
              cancel-button-text="取消"
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
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { 
  getEntities as pageEntities,
  getEntitiesByUser,
} from '@/api/entity';
import { Entity,Tag } from '@/types/entity';
import { 
  Plus, 
  Search, 
  Refresh, 
  Edit, 
  Delete, 
  View, 
  Picture, 
  Document,
  UploadFilled,
  Folder,
  Goods,
  Check,
  Close
} from '@element-plus/icons-vue';
import moment from 'moment';


export default defineComponent({
  name: 'EntityList',
  components: {
    Plus,
    Search,
    Refresh,
    Edit,
    Delete,
    View,
    Picture,
    Document,
    UploadFilled,
    Folder,
    Goods,
    Check,
    Close
  },
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    
    const loading = ref(false);
    const saving = ref(false);
    const entityList = ref<Entity[]>([]);
    const spaceList = ref<Entity[]>([]);
    const tagOptions = ref<Tag[]>([]);
    const locationOptions = ref<any[]>([]);
    const addEntityDialogVisible = ref(false);
    const imageDialogVisible = ref(false);
    const entityFormRef = ref<any>(null);
    const tempImages = ref<{file: File, url: string}[]>([]);
    
    // 分页参数
    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    });
    
    // 搜索表单
    const searchForm = reactive({
      name: '',
      type: '',
      specification: '',
      status: '',
      usageFrequency: '',
      parentId: '' as string | undefined
    });
    


    
    // 新增状态
    const isEditing = ref(false);
    const isAdding = ref(false);
    
    // 加载实体列表
    const loadEntityList = async () => {
      if (!authStore.currentUser?.id) return;
      
      loading.value = true;
      try {
        const response = await pageEntities({
          current: pagination.current,
          size: pagination.size,
          ...searchForm,
          userId: authStore.currentUser.id
        });
        
        if (response.data && response.data.records) {
          entityList.value = response.data.records;
          pagination.total = response.data.total;
        }
      } catch (error) {
        console.error('加载实体列表失败:', error);
        ElMessage.error('加载实体列表失败，请检查网络连接');
      } finally {
        loading.value = false;
      }
    };
    
    // 加载搜索的空间列表
    const loadSpaceList = async () => {
      if (!authStore.currentUser?.id) return;
      
      try {
        const response = await getEntitiesByUser(authStore.currentUser.id);
        
        if (response.data) {
          spaceList.value = response.data;
        }
      } catch (error) {
        console.error('加载空间列表失败:', error);
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
        name: '',
        type: '',
        specification: '',
        status: '',
        usageFrequency: '',
        parentId: ''
      });
      pagination.current = 1;
      loadEntityList();
    };
    
    // 处理表格行点击
    const handleRowClick = (row: Entity) => {
      router.push(`/entity/${row.id}`);
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
      if (!date) return '-';
      return moment(date).format('YYYY-MM-DD');
    };
    
    // 获取状态对应的类型
    const getStatusType = (status: string) => {
      const statusMap: Record<string, string> = {
        'normal': 'success',
        'damaged': 'warning',
        'discarded': 'danger',
        'expired': 'danger'
      };
      return statusMap[status] || 'info';
    };
    
    // 获取状态对应的文本
    const getStatusText = (status: string) => {
      const statusMap: Record<string, string> = {
        'normal': '正常',
        'damaged': '损坏',
        'discarded': '丢弃',
        'expired': '过期'
      };
      return statusMap[status] || status;
    };
    
    // 获取使用频率文本
    const getUsageFrequencyText = (usageFrequency: string) => {
      const usageMap: Record<string, string> = {
        'high': '高',
        'medium': '中',
        'low': '低',
        'rare': '很少',
        'never': '从不',
        'rarely': '很少',
        'occasionally': '偶尔',
        'frequently': '经常',
        'daily': '每天'
      };
      return usageMap[usageFrequency] || usageFrequency;
    };

    // 获取标签文字颜色
    const getContrastColor = (bgColor: string) => {
      if (!bgColor) return '#ffffff';
      
      // 将十六进制颜色转换为RGB
      let color = bgColor.charAt(0) === '#' ? bgColor.substring(1) : bgColor;
      let r = parseInt(color.substr(0, 2), 16);
      let g = parseInt(color.substr(2, 2), 16);
      let b = parseInt(color.substr(4, 2), 16);
      
      // 计算亮度
      let yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
      
      // 如果亮度高于128，返回黑色，否则返回白色
      return (yiq >= 128) ? '#000000' : '#ffffff';
    };
    

    // 在组件挂载时加载数据
    onMounted(() => {
      loadEntityList();
      // 加载空间列表用于筛选
      loadSpaceList();
    });

    // 新增两个新变量和两个新方法
    const selectedLocationName = ref<string>('');


    return {
      loading,
      saving,
      entityList,
      spaceList,
      pagination,
      searchForm,
      entityFormRef,
      addEntityDialogVisible,
      imageDialogVisible,
      tempImages,
      tagOptions,
      locationOptions,
      handleSearch,
      resetSearch,
      handleRowClick,
      handleSizeChange,
      handleCurrentChange,
      formatDate,
      getStatusType,
      getStatusText,
      getUsageFrequencyText,
      getContrastColor,
      isEditing,
      isAdding,
      selectedLocationName,
    };
  }
});
</script>

<style scoped>
.entity-container {
  width: 100%;
  padding: 0;
}

.entity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 500;
  margin: 0;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  padding: 10px 0;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
}

.table-card {
  margin-bottom: 16px;
}

.entity-name {
  display: flex;
  align-items: center;
}

.entity-image {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  margin-right: 8px;
  object-fit: cover;
}

.entity-image-placeholder,
.image-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  margin-right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.tag-item {
  margin-right: 4px;
  margin-bottom: 4px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 添加物品对话框样式 */
.entity-form {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-container {
  height: 300px;
  margin-bottom: 20px;
}

.image-item {
  height: 100%;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-item img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.image-actions {
  position: absolute;
  right: 10px;
  bottom: 10px;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.upload-box {
  width: 100%;
  margin-top: 20px;
}

.upload-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.preview-item {
  width: 120px;
  height: 120px;
  border: 1px solid #eee;
  position: relative;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-actions {
  position: absolute;
  right: 5px;
  top: 5px;
}

.main-content {
  display: flex;
  gap: 20px;
  width: 100%;
}

.tree-container {
  width: 25%;
  min-width: 200px;
  transition: all 0.3s ease;
}

.tree-card {
  height: 100%;
  min-height: 400px;
}

.detail-container {
  flex: 1;
}

.detail-card {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.entity-detail {
  padding: 16px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 500;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.description {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.detail-image {
  width: 100%;
  height: 100%;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.tree-loading {
  padding: 20px;
}

/* 位置树样式 */
.location-tree {
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background-color: #fff;
}

.location-tree .el-tree-node__content {
  height: 32px;
}

.selected-location {
  margin-top: 8px;
  padding: 8px 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 响应式设计 */
@media screen and (max-width: 992px) {
  .main-content {
    flex-direction: column;
  }
  
  .tree-container {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .entity-form {
    padding: 0;
  }
  
  .info-item {
    flex: 1 0 100%;
  }
}

@media screen and (max-width: 768px) {
  .entity-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .detail-image-container {
    height: 150px;
  }
  
  .el-form-item {
    margin-bottom: 18px;
  }
}

@media screen and (max-width: 480px) {
  .detail-card .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-card .header-actions {
    margin-top: 10px;
  }
}
</style> 