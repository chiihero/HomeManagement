<template>
  <div class="page-container">
    <div class="page-header mb-base">
      <h1 class="page-title m-0">系统设置</h1>
    </div>
    
    <el-card shadow="hover" class="mb-medium">
      <template #header>
        <div class="flex justify-between align-center">
          <span>系统设置</span>
        </div>
      </template>
      
      <el-form 
        ref="systemFormRef"
        :model="systemForm"
        label-width="120px"
        class="form-container"
      >
        <el-form-item label="系统名称">
          <el-input v-model="systemForm.systemName" placeholder="请输入系统名称" />
        </el-form-item>
        
        <el-form-item label="系统Logo">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleLogoSuccess"
            :before-upload="beforeLogoUpload"
          >
            <img v-if="systemForm.logo" :src="systemForm.logo" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="form-hint text-secondary mt-mini">建议尺寸：200x200px，支持jpg、png格式</div>
        </el-form-item>
        
        <el-form-item label="系统描述">
          <el-input
            v-model="systemForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入系统描述"
          />
        </el-form-item>
        
        <el-form-item label="系统版本">
          <el-input v-model="systemForm.version" disabled />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveSystemSettings" :loading="loading">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { 
  getSystemSettings,
  updateSystemSettings
} from '@/api/settings';

export default defineComponent({
  name: 'System',
  components: {
    Plus
  },
  setup() {
    const loading = ref(false);
    const systemFormRef = ref<FormInstance>();
    
    // 上传相关配置
    const uploadUrl = import.meta.env.VITE_API_URL + '/api/system/logo';

    
    // 系统设置表单
    const systemForm = reactive({
      systemName: '',
      logo: '',
      description: '',
      version: '1.0.0'
    });
    
    // 加载系统设置
    const loadSystemSettings = async () => {
      try {
        const loadingInstance = ElLoading.service({
          text: '加载系统设置...',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        const res = await getSystemSettings();
        if (res.data && res.code === 200) {
          const settings = res.data;
          systemForm.systemName = settings.systemName || '';
          systemForm.logo = settings.logo || '';
          systemForm.description = settings.description || '';
          systemForm.version = settings.version || '1.0.0';
        } else {
          ElMessage.error(res.msg || '获取系统设置失败');
        }
        
        loadingInstance.close();
      } catch (error) {
        console.error('获取系统设置失败', error);
        ElMessage.error('获取系统设置失败');
      }
    };
    
    // 保存系统设置
    const saveSystemSettings = async () => {
      loading.value = true;
      try {
        const res = await updateSystemSettings({
          systemName: systemForm.systemName,
          logo: systemForm.logo,
          description: systemForm.description
        });
        
        if (res.data && res.code === 200) {
          ElMessage.success('系统设置保存成功');
        } else {
          ElMessage.error(res.msg || '保存失败');
        }
      } catch (error) {
        console.error('保存系统设置失败', error);
        ElMessage.error('保存失败');
      } finally {
        loading.value = false;
      }
    };
    
    // Logo上传相关方法
    const handleLogoSuccess = (response: any) => {
      if (response.code === 200) {
        systemForm.logo = response.data;
        ElMessage.success('Logo上传成功');
      } else {
        ElMessage.error(response.message || 'Logo上传失败');
      }
    };
    
    const beforeLogoUpload = (file: File) => {
      const isImage = file.type.startsWith('image/');
      const isLt2M = file.size / 1024 / 1024 < 2;
      
      if (!isImage) {
        ElMessage.error('只能上传图片文件！');
        return false;
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB！');
        return false;
      }
      return true;
    };
    
    onMounted(() => {
      loadSystemSettings();
    });
    
    return {
      systemFormRef,
      loading,
      systemForm,
      uploadUrl,
      saveSystemSettings,
      handleLogoSuccess,
      beforeLogoUpload
    };
  }
});
</script>

<style scoped>
.avatar-uploader {
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.form-hint {
  font-size: 12px;
  line-height: 1.5;
}
</style> 