<template>
  <div class="entity-container">
    <div class="entity-header">
      <h1 class="page-title">物品结构</h1>
      <div class="action-buttons">
        <el-button type="primary" @click="openAddEntityDialog">
          <el-icon><Plus /></el-icon>添加物品
        </el-button>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧树形结构 -->
      <div class="tree-container">
        <el-card class="tree-card">
          <template #header>
            <div class="card-header">
              <span>物品结构</span>
            </div>
          </template>
          <div v-if="loading" class="tree-loading">
            <el-skeleton :rows="10" animated />
          </div>
          <el-empty v-else-if="treeData.length === 0" description="暂无物品结构数据" />
          <el-tree
            v-else
            ref="entityTreeRef"
            :data="treeData"
            :props="defaultProps"
            node-key="id"
            highlight-current
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
            default-expand-all
          >
            <template #default="{ node }">
              <div class="custom-tree-node">
                <span>
                  <el-icon><Goods /></el-icon>
                  {{ node.label }}
                </span>
              </div>
            </template>
          </el-tree>
        </el-card>
      </div>
      
      <!-- 右侧详细信息 -->
      <div class="detail-container">
        <!-- 编辑或添加物品表单 -->
        <el-card v-if="isEditing || isAdding" class="detail-card">
          <template #header>
            <div class="card-header">
              <span>{{ isAdding ? '添加物品' : '编辑物品' }}</span>
              <div class="header-actions">
                <el-button type="primary" @click="saveEntity" :loading="saving">
                  <el-icon><Check /></el-icon>保存
                </el-button>
                <el-button @click="cancelEditOrAdd">
                  <el-icon><Close /></el-icon>取消
                </el-button>
              </div>
            </div>
          </template>
          
          <el-form :model="entityForm" ref="entityFormRef" :rules="formRules" label-position="top" class="entity-form">
            <el-row :gutter="20">
              <el-col :span="16">
                <div class="form-section">
                  <h3>基本信息</h3>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="物品名称" prop="name">
                        <el-input v-model="entityForm.name" placeholder="请输入物品名称" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="物品类型" prop="type">
                        <el-select v-model="entityForm.type" placeholder="选择物品类型" style="width: 100%">
                          <el-option label="电子设备" value="电子设备" />
                          <el-option label="家具" value="家具" />
                          <el-option label="厨房用品" value="厨房用品" />
                          <el-option label="衣物" value="衣物" />
                          <el-option label="书籍" value="书籍" />
                          <el-option label="其他" value="其他" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="规格型号" prop="specification">
                        <el-input v-model="entityForm.specification" placeholder="请输入规格型号" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="价格" prop="price">
                        <el-input-number v-model="entityForm.price" :min="0" :precision="2" style="width: 100%" placeholder="请输入价格" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="购买日期" prop="purchaseDate">
                        <el-date-picker 
                          v-model="entityForm.purchaseDate" 
                          type="date" 
                          placeholder="选择购买日期" 
                          style="width: 100%" 
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="到期日期" prop="expirationDate">
                        <el-date-picker 
                          v-model="entityForm.expirationDate" 
                          type="date" 
                          placeholder="选择到期日期" 
                          style="width: 100%" 
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="状态" prop="status">
                        <el-select v-model="entityForm.status" placeholder="选择状态" style="width: 100%">
                          <el-option label="正常" value="normal"></el-option>
                          <el-option label="损坏" value="damaged"></el-option>
                          <el-option label="丢弃" value="discarded"></el-option>
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="使用频率" prop="usageFrequency">
                        <el-select v-model="entityForm.usageFrequency" placeholder="选择使用频率" style="width: 100%">
                          <el-option label="从不" value="never"></el-option>
                          <el-option label="很少" value="rarely"></el-option>
                          <el-option label="偶尔" value="occasionally"></el-option>
                          <el-option label="经常" value="frequently"></el-option>
                          <el-option label="每天" value="daily"></el-option>
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-form-item label="所属位置" prop="parentId">
                    <el-tree
                      v-model:current-node-key="entityForm.parentId"
                      :data="locationOptions"
                      :props="{ label: 'name', children: 'children' }"
                      node-key="id"
                      highlight-current
                      @current-change="handleLocationSelect"
                      default-expand-all
                      :expand-on-click-node="false"
                      class="location-tree"
                    >
                      <template #default="{ node, data }">
                        <div class="custom-tree-node">
                          <span>
                            <el-icon v-if="data.type === 'space'"><Folder /></el-icon>
                            <el-icon v-else><Goods /></el-icon>
                            {{ node.label }}
                          </span>
                        </div>
                      </template>
                    </el-tree>
                  </el-form-item>
                  
                  <el-form-item label="标签" prop="tags">
                    <el-select
                      v-model="entityForm.tags"
                      multiple
                      filterable
                      allow-create
                      default-first-option
                      placeholder="请选择或创建标签"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="tag in tagOptions"
                        :key="tag.id"
                        :label="tag.name"
                        :value="tag.id"
                        :style="{ backgroundColor: tag.color, color: getContrastColor(tag.color) }"
                      />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="描述" prop="description">
                    <el-input 
                      v-model="entityForm.description" 
                      type="textarea" 
                      :rows="4" 
                      placeholder="请输入物品描述信息" 
                    />
                  </el-form-item>
                </div>
              </el-col>
              
              <el-col :span="8">
                <div class="form-section">
                  <h3>物品图片</h3>
                  <div class="upload-container">
                    <el-upload
                      action="#"
                      list-type="picture-card"
                      :auto-upload="false"
                      :file-list="[]"
                      :on-change="handleImageChange"
                      :limit="5"
                    >
                      <el-icon><Plus /></el-icon>
                    </el-upload>
                  </div>
                  
                  <div class="image-preview" v-if="entityForm.images && entityForm.images.length > 0">
                    <div v-for="(image, index) in entityForm.images" :key="index" class="image-item">
                      <el-image :src="image.url || (image as any).imagePath" fit="cover" />
                      <div class="image-actions">
                        <el-button type="danger" circle icon="Delete" @click="removeImage(index)"></el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <!-- 物品详情显示 -->
        <el-card v-else-if="currentEntity" class="detail-card">
          <template #header>
            <div class="card-header">
              <span>物品详情</span>
              <div class="header-actions">
                <el-button type="primary" link @click="handleEdit(currentEntity)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-popconfirm 
                  title="确定删除此物品吗？" 
                  @confirm="handleDelete(currentEntity)"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                >
                  <template #reference>
                    <el-button type="danger" link>
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </template>
          
          <div class="entity-detail">
            <div class="detail-section">
              <h3>基本信息</h3>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="名称">{{ currentEntity.name }}</el-descriptions-item>
                <el-descriptions-item label="类型">
                  <el-tag :type="currentEntity.type === 'item' ? 'success' : 'primary'">
                    {{ currentEntity.type === 'item' ? '物品' : '空间' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="规格">{{ currentEntity.specification || '-' }}</el-descriptions-item>
                <el-descriptions-item label="价格">{{ currentEntity.price ? '¥' + currentEntity.price.toFixed(2) : '-' }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="getStatusType(currentEntity.status)">
                    {{ getStatusText(currentEntity.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="使用频率">{{ getUsageFrequencyText(currentEntity.usageFrequency) }}</el-descriptions-item>
                <el-descriptions-item label="购买日期">{{ currentEntity.purchaseDate ? formatDate(currentEntity.purchaseDate) : '-' }}</el-descriptions-item>
                <el-descriptions-item label="到期日期">{{ currentEntity.expirationDate ? formatDate(currentEntity.expirationDate) : '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
            
            <div class="detail-section">
              <h3>标签</h3>
              <div class="tags-container">
                <el-tag 
                  v-for="tag in currentEntity.tags" 
                  :key="tag.id" 
                  :style="{ backgroundColor: tag.color, color: getContrastColor(tag.color) }"
                  class="tag-item"
                >
                  {{ tag.name }}
                </el-tag>
                <span v-if="!currentEntity.tags || currentEntity.tags.length === 0">暂无标签</span>
              </div>
            </div>
            
            <div class="detail-section">
              <h3>描述</h3>
              <p class="description">{{ currentEntity.description || '暂无描述' }}</p>
            </div>
            
            <div class="detail-section" v-if="currentEntity.images && currentEntity.images.length > 0">
              <h3>图片</h3>
              <el-carousel height="200px" indicator-position="outside">
                <el-carousel-item v-for="(image, index) in currentEntity.images" :key="index">
                  <el-image 
                    :src="image.imagePath" 
                    fit="contain"
                    class="detail-image"
                  />
                </el-carousel-item>
              </el-carousel>
            </div>
          </div>
        </el-card>
        
        <el-empty v-else description="请选择左侧物品查看详情" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { 
  deleteEntity,
  addEntity as createEntity, 
  getEntitiesByUser,
  getEntityTree,
  updateEntity
} from '@/api/entity';
import { getAllTags as listTags } from '@/api/tag';
import { Entity, Tag } from '@/types/entity';
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

// 定义新增物品表单类型
interface EntityForm {
  id?: number;
  name: string;
  type: string;
  entityClassification: 'item' | 'space';
  specification: string;
  price?: number;
  purchaseDate: string;
  expirationDate: string;
  status: string;
  usageFrequency: string;
  parentId: string;
  tags: number[];
  description: string;
  images: {file: File, url: string}[];
  userId?: number;
}

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
    const authStore = useAuthStore();
    
    const loading = ref(false);
    const saving = ref(false);
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
    
    // 新增物品表单
    const entityForm = reactive<EntityForm>({
      name: '',
      type: '',
      entityClassification: 'item',
      specification: '',
      price: undefined,
      purchaseDate: '',
      expirationDate: '',
      status: 'normal',
      usageFrequency: 'medium',
      parentId: '0',
      tags: [],
      description: '',
      images: [],
      userId: authStore.currentUser?.id
    });
    
    // 表单验证规则
    const formRules = {
      name: [
        { required: true, message: '请输入物品名称', trigger: 'blur' },
        { min: 1, max: 50, message: '名称长度应在1-50个字符之间', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择物品类型', trigger: 'change' }
      ]
    };
    
    // 树形结构相关
    const entityTreeRef = ref<any>(null);
    const treeData = ref<any[]>([]);
    const currentEntity = ref<Entity | null>(null);
    const defaultProps = {
      children: 'children',
      label: 'name'
    };
    
    // 新增状态
    const isEditing = ref(false);
    const isAdding = ref(false);
    


    // 处理编辑实体
    const handleEdit = (row: Entity) => {
      // 将实体数据填充到表单
      Object.assign(entityForm, {
        id: row.id,
        name: row.name,
        type: row.type,
        entityClassification: 'item',
        specification: row.specification || '',
        price: row.price,
        purchaseDate: (row as any).purchaseDate || '',
        expirationDate: (row as any).expirationDate || '',
        status: (row as any).status || 'normal',
        usageFrequency: (row as any).usageFrequency || 'medium',
        parentId: row.parentId ? String(row.parentId) : '0',
        tags: row.tags ? row.tags.map(tag => tag.id) : [],
        description: row.description || '',
        images: (row as any).images || [],
        userId: row.userId || authStore.currentUser?.id
      });
      
      // 加载标签和位置选项
      loadTags();
      loadLocationOptions();
      // 设置已选位置名称
      if (row.parentId) {
        const parent = treeData.value.find(tree => tree.id === row.parentId);
        if (parent) {
          selectedLocationName.value = parent.name;
        } else {
          selectedLocationName.value = '根空间';
        }
      } else {
        selectedLocationName.value = '根空间';
      }
      
      // 设置编辑状态
      isEditing.value = true;
      isAdding.value = false;
      currentEntity.value = row;
    };
    
    // 处理删除实体
    const handleDelete = async (row: Entity) => {
      if (!row.id) return;
      
      try {
        const response = await deleteEntity(row.id.toString());
        
        if (response.data) {
          ElMessage.success('删除成功');
          loadTreeData();
        } else {
          ElMessage.error(`删除失败: ${response.msg}`);
        }
      } catch (error) {
        console.error('删除实体失败:', error);
        ElMessage.error('删除失败，请检查网络连接');
      }
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
    
    // 打开添加物品表单
    const openAddEntityDialog = () => {
      // 重置表单
      Object.assign(entityForm, {
        name: '',
        type: '',
        entityClassification: 'item',
        specification: '',
        price: undefined,
        purchaseDate: '',
        expirationDate: '',
        status: 'normal',
        usageFrequency: 'medium',
        parentId: '0',
        tags: [],
        description: '',
        images: [],
        userId: authStore.currentUser?.id
      });
      
      // 重置已选位置名称
      selectedLocationName.value = '根空间';
      
      // 加载标签和位置选项
      loadTags();
      loadLocationOptions();
      
      // 设置添加状态
      isAdding.value = true;
      isEditing.value = false;
      currentEntity.value = null;
    };
    
    // 取消编辑或添加
    const cancelEditOrAdd = () => {
      ElMessageBox.confirm('确定要取消吗？未保存的数据将会丢失', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        isEditing.value = false;
        isAdding.value = false;
      }).catch(() => {});
    };
    
    // 关闭添加物品对话框 - 保留以兼容现有代码
    const handleCloseAddDialog = () => {
      // 调用取消编辑或添加
      cancelEditOrAdd();
    };
    
    // 保存物品
    const saveEntity = async () => {
      if (!entityFormRef.value) return;
      
      await entityFormRef.value.validate(async (valid: boolean) => {
        if (!valid) {
          ElMessage.error('请检查表单填写是否正确');
          return;
        }
        
        // 确保有用户ID
        if (!authStore.currentUser?.id) {
          ElMessage.error('用户未登录，请先登录');
          return;
        }
        
        saving.value = true;
        
        try {
          // 使用类型断言解决类型不匹配问题
          const submitData = {
            ...JSON.parse(JSON.stringify(entityForm)), // 深拷贝避免直接修改引用
            status: entityForm.status as 'normal' | 'damaged' | 'discarded',
            usageFrequency: entityForm.usageFrequency as 'high' | 'medium' | 'low' | 'rare',
            userId: authStore.currentUser.id
          };
          
          // 特殊处理根空间的情况
          if (submitData.parentId === '0') {
            // @ts-ignore - 忽略类型检查，因为服务端需要接收null值
            submitData.parentId = null; // 发送null表示没有父节点
          }
          
          let response;
          if (isAdding.value) {
            response = await createEntity(submitData as any);
          } else {
            response = await updateEntity(entityForm.id?.toString() || '', submitData as any);
          }
          
          if (response.data) {
            ElMessage.success(isAdding.value ? '添加物品成功' : '更新物品成功');
            isAdding.value = false;
            isEditing.value = false;

            loadTreeData();

          } else {
            ElMessage.error(isAdding.value ? '添加物品失败，请重试' : '更新物品失败，请重试');
          }
        } catch (error: any) {
          console.error('操作实体出错:', error);
          ElMessage.error(isAdding.value ? '添加物品失败，请检查网络连接' : '更新物品失败，请检查网络连接');
        } finally {
          saving.value = false;
        }
      });
    };
    
    // 处理图片变更
    const handleImageChange = (file: any) => {
      if (!file) return;
      
      // 检查文件类型
      const isImage = file.raw.type.startsWith('image/');
      if (!isImage) {
        ElMessage.error('只能上传图片文件');
        return;
      }
      
      // 检查文件大小
      const isLt2M = file.raw.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过2MB');
        return;
      }
      
      // 创建预览并添加到表单
      const reader = new FileReader();
      reader.readAsDataURL(file.raw);
      reader.onload = () => {
        entityForm.images.push({
          file: file.raw,
          url: reader.result as string
        });
      };
    };
    
    // 移除图片
    const removeImage = (index: number) => {
      entityForm.images.splice(index, 1);
    };
    
    // 加载标签选项
    const loadTags = async () => {
      try {
        if (!authStore.currentUser?.id) return;
        const response = await listTags(authStore.currentUser.id);
        if (response.data) {
          tagOptions.value = response.data || [];
        }
      } catch (error) {
        console.error('加载标签失败:', error);
      }
    };
    
    // 加载位置选项
    const loadLocationOptions = async () => {
      try {
        if (!authStore.currentUser?.id) return;
        const userId = authStore.currentUser.id;
        const response = await getEntitiesByUser(userId);
        if (response.data) {
          // 构建级联选择器需要的树形结构
          locationOptions.value = buildLocationTree(response.data || []);
        }
      } catch (error) {
        console.error('加载位置选项失败:', error);
      }
    };
    
    // 构建位置树
    const buildLocationTree = (spaces: Entity[]) => {
      // 将空间列表转换为树形结构
      const result: any[] = [];
      const map = new Map();
      
      // 添加一个根空间作为顶级目录
      const rootSpace: {
        id: string;
        name: string;
        type: string;
        children: any[];
      } = {
        id: '0',
        name: '根空间',
        type: 'space',
        children: []
      };
      
      // 添加根空间到结果中
      result.push(rootSpace);
      map.set('0', rootSpace);
      
      // 先创建映射
      spaces.forEach(space => {
        map.set(space.id, { ...space, children: [] });
      });
      
      // 然后构建树
      spaces.forEach(space => {
        const node = map.get(space.id);
        if (space.parentId) {
          const parent = map.get(space.parentId);
          if (parent) {
            parent.children.push(node);
          } else {
            // 如果找不到父节点，默认放到根空间下
            rootSpace.children.push(node);
          }
        } else {
          // 没有父节点的空间，作为根空间的子节点
          rootSpace.children.push(node);
        }
      });
      
      return result;
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
    
    // 加载树形结构数据
    const loadTreeData = async () => {
      if (!authStore.currentUser?.id) return;
      
      loading.value = true;
      try {
        const response = await getEntityTree(authStore.currentUser.id);
        if (response.data) {
          treeData.value = response.data;
          if (treeData.value.length === 0) {
            console.info('物品结构树为空，请添加物品或空间');
          }
        } else {
          console.error('加载树形结构返回的数据为空');
          ElMessage.warning('物品结构数据为空，请先添加物品或空间');
        }
      } catch (error) {
        console.error('加载树形结构失败:', error);
        ElMessage.error('加载树形结构失败，请检查网络连接');
      } finally {
        loading.value = false;
      }
    };
    
    // 处理树节点点击
    const handleNodeClick = (data: any) => {
      currentEntity.value = data;
      
    };
    
    // 在组件挂载时加载数据
    onMounted(() => {
      // 加载空间列表用于筛选
      loadTreeData();
    });

    // 新增两个新变量和两个新方法
    const selectedLocationName = ref<string>('');

    // 处理位置选择
    const handleLocationSelect = (data: any, node: any) => {
      if (data && data.id) {
        if (entityForm.parentId === data.id) {
          // 如果再次点击同一项，则取消选择，并默认选择根空间
          entityForm.parentId = '0';
          selectedLocationName.value = '根空间';
        } else {
          entityForm.parentId = data.id;
          selectedLocationName.value = data.name;
        }
      }
    };

    // 清除选中的位置（设置为根空间）
    const clearSelectedLocation = () => {
      entityForm.parentId = '0';
      selectedLocationName.value = '根空间';
    };

    return {
      loading,
      saving,
      pagination,
      searchForm,
      entityForm,
      formRules,
      entityFormRef,
      addEntityDialogVisible,
      imageDialogVisible,
      tempImages,
      tagOptions,
      locationOptions,
      handleEdit,
      handleDelete,
      formatDate,
      getStatusType,
      getStatusText,
      getUsageFrequencyText,
      getContrastColor,
      openAddEntityDialog,
      handleCloseAddDialog,
      saveEntity,
      handleImageChange,
      removeImage,
      entityTreeRef,
      treeData,
      currentEntity,
      defaultProps,
      handleNodeClick,
      isEditing,
      isAdding,
      cancelEditOrAdd,
      selectedLocationName,
      handleLocationSelect,
      clearSelectedLocation
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