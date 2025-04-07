<template>
  <div class="entity-form">
    <el-card shadow="hover" class="h-full">
      <template #header>
        <div class="flex justify-between items-center">
          <span>{{ isEditing ? '编辑物品' : '添加物品' }}</span>
          <div class="actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="saving">保存</el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="form-content"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物品名称" />
        </el-form-item>

        <el-form-item label="类型" prop="type">
          <el-input v-model="form.type" placeholder="请输入物品类型" />
        </el-form-item>

        <el-form-item label="父级物品" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeData"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择父级物品"
            clearable
            check-strictly
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="已处置" value="DISPOSED" />
          </el-select>
        </el-form-item>

        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入物品位置" />
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :precision="2"
            :step="0.1"
            :min="0"
            placeholder="请输入价格"
          />
        </el-form-item>

        <el-form-item label="购买日期" prop="purchaseDate">
          <el-date-picker
            v-model="form.purchaseDate"
            type="date"
            placeholder="请选择购买日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="保修期" prop="warrantyPeriod">
          <el-input-number
            v-model="form.warrantyPeriod"
            :min="0"
            :max="120"
            placeholder="请输入保修期（月）"
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入物品描述"
          />
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入标签"
          >
            <el-option
              v-for="tag in existingTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="图片" prop="images">
          <el-upload
            v-model:file-list="imageList"
            action="/api/upload"
            list-type="picture-card"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
            :before-upload="beforeImageUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <el-dialog v-model="dialogVisible">
            <img w-full :src="dialogImageUrl" alt="Preview Image" />
          </el-dialog>
        </el-form-item>

        <el-form-item label="附件" prop="attachments">
          <el-upload
            v-model:file-list="attachmentList"
            action="/api/upload"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :before-upload="beforeAttachmentUpload"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持任意文件类型，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, UploadProps, UploadFile } from 'element-plus'
import type { Entity, EntityFormData } from '@/types/entity'

interface Props {
  entity: Entity | null
  treeData: Entity[]
  existingTags: string[]
  isEditing: boolean
  saving: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'submit', form: EntityFormData): void
  (e: 'cancel'): void
}>()

const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const imageList = ref<UploadFile[]>([])
const attachmentList = ref<UploadFile[]>([])

// 表单数据
const form = ref<EntityFormData>({
  name: '',
  type: '',
  parentId: '',
  status: 'AVAILABLE',
  location: '',
  price: 0,
  purchaseDate: '',
  warrantyPeriod: 0,
  description: '',
  tags: [],
  images: [],
  attachments: []
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入物品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请输入物品类型', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  purchaseDate: [
    { required: true, message: '请选择购买日期', trigger: 'change' }
  ]
}

// 处理图片预览
const handlePictureCardPreview: UploadProps['onPreview'] = (file) => {
  dialogImageUrl.value = file.url!
  dialogVisible.value = true
}

// 处理文件预览
const handlePreview: UploadProps['onPreview'] = (file) => {
  window.open(file.url)
}

// 处理文件移除
const handleRemove: UploadProps['onRemove'] = (file) => {
  if (file.raw?.type.startsWith('image/')) {
    const index = imageList.value.findIndex(item => item.uid === file.uid)
    if (index !== -1) {
      imageList.value.splice(index, 1)
    }
  } else {
    const index = attachmentList.value.findIndex(item => item.uid === file.uid)
    if (index !== -1) {
      attachmentList.value.splice(index, 1)
    }
  }
}

// 图片上传前检查
const beforeImageUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 附件上传前检查
const beforeAttachmentUpload: UploadProps['beforeUpload'] = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      // 更新图片和附件列表
      form.value.images = imageList.value.map(file => file.url!)
      form.value.attachments = attachmentList.value.map(file => ({
        name: file.name,
        url: file.url!
      }))
      
      emit('submit', form.value)
    }
  })
}

// 处理取消
const handleCancel = () => {
  emit('cancel')
}

// 监听实体变化
watch(() => props.entity, (newEntity) => {
  if (newEntity) {
    form.value = {
      name: newEntity.name,
      type: newEntity.type,
      parentId: newEntity.parentId,
      status: newEntity.status,
      location: newEntity.location,
      price: newEntity.price,
      purchaseDate: newEntity.purchaseDate,
      warrantyPeriod: newEntity.warrantyPeriod,
      description: newEntity.description,
      tags: newEntity.tags || [],
      images: newEntity.images || [],
      attachments: newEntity.attachments || []
    }
    
    // 更新图片和附件列表
    imageList.value = newEntity.images?.map(url => ({
      name: url.split('/').pop() || '',
      url
    })) || []
    
    attachmentList.value = newEntity.attachments?.map(attachment => ({
      name: attachment.name,
      url: attachment.url
    })) || []
  } else {
    // 重置表单
    form.value = {
      name: '',
      type: '',
      parentId: '',
      status: 'AVAILABLE',
      location: '',
      price: 0,
      purchaseDate: '',
      warrantyPeriod: 0,
      description: '',
      tags: [],
      images: [],
      attachments: []
    }
    imageList.value = []
    attachmentList.value = []
  }
}, { immediate: true })
</script>

<style scoped>
.entity-form {
  flex: 1;
  height: 100%;
  margin-left: 16px;
}

.h-full {
  height: 100%;
}

.form-content {
  padding: 16px;
  max-width: 800px;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style> 