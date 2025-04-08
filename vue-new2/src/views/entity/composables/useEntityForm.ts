import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Entity, EntityFormData, EntityStatus } from '@/types/entity'

export function useEntityForm() {
  // 表单引用
  const entityFormRef = ref<FormInstance>()

  // 表单数据
  const entityForm = reactive<EntityFormData>({
    name: '',
    type: 'item',
    description: '',
    parentId: null,
    price: 0,
    purchaseDate: '',
    warrantyPeriod: 0,
    status: 'AVAILABLE' as EntityStatus,
    location: '',
    tags: [],
    images: [],
  })

  // 表单验证规则
  const rules = reactive<FormRules>({
    name: [
      { required: true, message: '请输入物品名称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    type: [
      { required: true, message: '请选择物品类型', trigger: 'change' }
    ],
    status: [
      { required: true, message: '请选择物品状态', trigger: 'change' }
    ],
    price: [
      { required: true, message: '请输入物品价格', trigger: 'blur' },
      { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
    ],
    purchaseDate: [
      { required: true, message: '请选择购买日期', trigger: 'change' }
    ]
  })

  // 重置表单
  const resetForm = () => {
    if (entityFormRef.value) {
      entityFormRef.value.resetFields()
    }
    Object.assign(entityForm, {
      name: '',
      type: 'item',
      description: '',
      parentId: null,
      price: 0,
      purchaseDate: '',
      warrantyPeriod: 0,
      status: 'AVAILABLE' as EntityStatus,
      location: '',
      tags: [],
      images: [],
      attachments: []
    })
  }

  // 使用实体数据填充表单
  const fillFormWithEntity = (entity: Entity) => {
    Object.assign(entityForm, {
      name: entity.name,
      type: entity.type,
      description: entity.description || '',
      parentId: entity.parentId || null,
      price: entity.price || 0,
      purchaseDate: entity.purchaseDate || '',
      warrantyPeriod: entity.warrantyPeriod || 0,
      status: entity.status as EntityStatus,
      location: entity.location || '',
      tags: entity.tags || [],
      images: entity.images || [],
      attachments: entity.attachments || []
    })
  }

  // 验证表单
  const validateForm = async (): Promise<boolean> => {
    if (!entityFormRef.value) return false
    try {
      await entityFormRef.value.validate()
      return true
    } catch (error) {
      return false
    }
  }

  // 获取表单数据
  const getFormData = (): EntityFormData => {
    return { ...entityForm }
  }

  return {
    entityFormRef,
    entityForm,
    rules,
    resetForm,
    fillFormWithEntity,
    validateForm,
    getFormData
  }
} 