<template>
  <el-card class="detail-card">
    <template #header>
      <div class="card-header">
        <span>{{ isAdding ? '添加物品' : '编辑物品' }}</span>
        <div class="header-actions">
          <el-button type="primary" @click="handleSave" :loading="saving">
            <el-icon><Check /></el-icon>保存
          </el-button>
          <el-button @click="$emit('cancel')">
            <el-icon><Close /></el-icon>取消
          </el-button>
        </div>
      </div>
    </template>
    
    <el-form :model="entityForm" ref="formRef" :rules="formRules" label-position="top" class="entity-form">
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
            
            <LocationSelector 
              v-model:parentId="entityForm.parentId"
              :locationOptions="locationOptions"
            />
            
            <EntityTags 
              v-model:tags="entityForm.tags"
              :tagOptions="tagOptions"
            />
            
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
          <EntityImageUpload 
            v-model:images="entityForm.images"
          />
        </el-col>
      </el-row>
    </el-form>
  </el-card>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, computed, watch } from 'vue';
import { Check, Close } from '@element-plus/icons-vue';
import { useEntityForm } from '../composables/useEntityForm';
import LocationSelector from './LocationSelector.vue';
import EntityTags from './EntityTags.vue';
import EntityImageUpload from './EntityImageUpload.vue';

export default defineComponent({
  name: 'EntityForm',
  components: {
    Check,
    Close,
    LocationSelector,
    EntityTags,
    EntityImageUpload
  },
  props: {
    modelValue: {
      type: Object,
      required: true
    },
    isAdding: {
      type: Boolean,
      default: false
    },
    saving: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'save', 'cancel'],
  setup(props, { emit }) {
    const {
      entityForm,
      formRules,
      tagOptions,
      locationOptions,
      loadTags,
      loadLocationOptions
    } = useEntityForm();
    
    const formRef = ref<any>(null);
    
    // 监听modelValue变化，更新表单
    const updateForm = () => {
      if (props.modelValue) {
        Object.assign(entityForm, props.modelValue);
      }
    };
    
    // 添加对modelValue的watch
    watch(() => props.modelValue, () => {
      updateForm();
    }, { deep: true });
    
    // 处理保存
    const handleSave = () => {
      formRef.value?.validate((valid: boolean) => {
        if (valid) {
          emit('save', entityForm, formRef.value);
        }
      });
    };
    
    // 加载初始数据
    onMounted(() => {
      updateForm();
      loadTags();
      loadLocationOptions();
    });
    
    return {
      entityForm,
      formRef,
      formRules,
      tagOptions,
      locationOptions,
      handleSave
    };
  }
});
</script>

<style scoped>
.detail-card {
  min-height: 400px;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.entity-form {
  padding: 16px 0;
}

.form-section {
  margin-bottom: 20px;
}

.form-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 500;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

@media screen and (max-width: 768px) {
  .detail-card .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-card .header-actions {
    margin-top: 10px;
    width: 100%;
  }
}
</style> 