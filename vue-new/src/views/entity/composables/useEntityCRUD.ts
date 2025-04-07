import { ref, Ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { 
  deleteEntity, 
  addEntity as createEntity, 
  updateEntity, 
  getEntityTree 
} from '@/api/entity';
import { Entity } from '@/types/entity';

export function useEntityCRUD() {
  const authStore = useAuthStore();
  
  const loading = ref(false);
  const saving = ref(false);
  const treeData = ref<any[]>([]);
  const currentEntity = ref<Entity | null>(null);
  const isEditing = ref(false);
  const isAdding = ref(false);
  
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
    isEditing.value = false;
    isAdding.value = false;
  };
  
  // 处理删除实体
  const handleDelete = async (entity: Entity) => {
    if (!entity.id) return;
    
    try {
      const response = await deleteEntity(entity.id.toString());
      
      if (response.data) {
        ElMessage.success('删除成功');
        currentEntity.value = null;
        loadTreeData();
      } else {
        ElMessage.error(`删除失败: ${response.msg}`);
      }
    } catch (error) {
      console.error('删除实体失败:', error);
      ElMessage.error('删除失败，请检查网络连接');
    }
  };
  
  // 打开添加实体表单
  const openAddEntityForm = () => {
    isAdding.value = true;
    isEditing.value = false;
    currentEntity.value = null;
  };
  
  // 打开编辑实体表单
  const openEditEntityForm = (entity: Entity) => {
    currentEntity.value = entity;
    isEditing.value = true;
    isAdding.value = false;
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
  
  // 保存实体
  const saveEntity = async (formData: any, formRef: any) => {
    if (!formRef) return;
    
    await formRef.validate(async (valid: boolean) => {
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
          ...JSON.parse(JSON.stringify(formData)), // 深拷贝避免直接修改引用
          status: formData.status as 'normal' | 'damaged' | 'discarded',
          usageFrequency: formData.usageFrequency as 'high' | 'medium' | 'low' | 'rare',
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
          response = await updateEntity(formData.id?.toString() || '', submitData as any);
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
  
  return {
    loading,
    saving,
    treeData,
    currentEntity,
    isEditing,
    isAdding,
    loadTreeData,
    handleNodeClick,
    handleDelete,
    openAddEntityForm,
    openEditEntityForm,
    cancelEditOrAdd,
    saveEntity
  };
} 