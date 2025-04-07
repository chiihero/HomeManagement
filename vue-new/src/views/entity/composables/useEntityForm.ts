import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { getAllTags as listTags } from '@/api/tag';
import { getEntitiesByUser } from '@/api/entity';
import { Entity, Tag } from '@/types/entity';
import moment from 'moment';

// 定义实体表单类型
export interface EntityForm {
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
  images: {file: File, url: string}[] | any[];
  userId?: number;
}

export function useEntityForm() {
  const authStore = useAuthStore();
  
  // 标签选项
  const tagOptions = ref<Tag[]>([]);
  // 位置选项
  const locationOptions = ref<any[]>([]);
  // 选择的位置名称
  const selectedLocationName = ref<string>('根空间');
  // 表单引用
  const entityFormRef = ref<any>(null);
  
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
  
  // 创建默认表单数据
  const createDefaultForm = (): EntityForm => {
    return {
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
    };
  };
  
  // 表单数据
  const entityForm = reactive<EntityForm>(createDefaultForm());
  
  // 重置表单
  const resetForm = () => {
    Object.assign(entityForm, createDefaultForm());
    selectedLocationName.value = '根空间';
  };
  
  // 填充表单数据
  const fillFormWithEntity = (entity: Entity) => {
    Object.assign(entityForm, {
      id: entity.id,
      name: entity.name,
      type: entity.type,
      entityClassification: 'item',
      specification: entity.specification || '',
      price: entity.price,
      purchaseDate: (entity as any).purchaseDate || '',
      expirationDate: (entity as any).expirationDate || '',
      status: (entity as any).status || 'normal',
      usageFrequency: (entity as any).usageFrequency || 'medium',
      parentId: entity.parentId ? String(entity.parentId) : '0',
      tags: entity.tags ? entity.tags.map(tag => tag.id) : [],
      description: entity.description || '',
      images: (entity as any).images || [],
      userId: entity.userId || authStore.currentUser?.id
    });
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
  
  // 处理位置选择
  const handleLocationSelect = (data: any) => {
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
  
  return {
    entityForm,
    entityFormRef,
    formRules,
    tagOptions,
    locationOptions,
    selectedLocationName,
    resetForm,
    fillFormWithEntity,
    loadTags,
    loadLocationOptions,
    handleLocationSelect,
    getContrastColor,
    handleImageChange,
    removeImage,
    formatDate,
    getStatusType,
    getStatusText,
    getUsageFrequencyText
  };
} 