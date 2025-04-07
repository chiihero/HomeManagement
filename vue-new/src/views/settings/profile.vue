<template>
  <div class="page-container">
    <div class="page-header mb-base">
      <h1 class="page-title m-0">个人资料</h1>
    </div>
    
    <el-card shadow="hover" class="mb-medium">
      <template #header>
        <div class="flex justify-between align-center">
          <span>个人信息设置</span>
        </div>
      </template>
      
      <el-form 
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-width="100px"
        class="form-container"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
          >
            <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="loading">保存个人信息</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { Plus } from '@element-plus/icons-vue';
import type { FormInstance } from 'element-plus';
import { 
  getUserProfile, 
  updateUserProfile,
  uploadUserAvatar
} from '@/api/user';

export default defineComponent({
  name: 'Profile',
  components: {
    Plus
  },
  setup() {
    const authStore = useAuthStore();
    const loading = ref(false);
    
    // 表单引用
    const profileFormRef = ref<FormInstance>();
    
    // 个人信息表单
    const profileForm = reactive({
      username: '',
      nickname: '',
      email: '',
      phone: '',
      avatar: ''
    });
    
    // 个人信息校验规则
    const profileRules = {
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ]
    };
    
    // 加载用户信息
    const loadUserInfo = async () => {
      try {
        const loadingInstance = ElLoading.service({
          text: '加载个人信息...',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        const res = await getUserProfile();
        if (res && res.code === 200) {
          const userData = res.data;
          profileForm.username = userData.username || '';
          profileForm.nickname = userData.nickname || '';
          profileForm.email = userData.email || '';
          profileForm.phone = userData.phone || '';
          profileForm.avatar = userData.avatar || '';
        } else {
          ElMessage.error(res?.msg || '获取用户信息失败');
        }
        
        loadingInstance.close();
      } catch (error) {
        console.error('获取用户信息失败', error);
        ElMessage.error('获取用户信息失败');
      }
    };
    
    // 处理头像上传
    const handleAvatarChange = async (file: any) => {
      if (!file.raw) return;
      
      try {
        loading.value = true;
        const res = await uploadUserAvatar(file.raw);
        if (res && res.code === 200) {
          profileForm.avatar = res.data.url;
          ElMessage.success('头像上传成功');
        } else {
          ElMessage.error(res?.msg || '上传头像失败');
        }
      } catch (error) {
        console.error('上传头像失败', error);
        ElMessage.error('上传头像失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 保存个人信息
    const saveProfile = async () => {
      if (!profileFormRef.value) return;
      
      await profileFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          loading.value = true;
          try {
            const res = await updateUserProfile({
              nickname: profileForm.nickname,
              email: profileForm.email,
              phone: profileForm.phone
            });
            
            if (res && res.code === 200) {
              ElMessage.success('个人信息保存成功');
              // 更新存储中的用户信息
              if (authStore.user) {
                authStore.setUser({
                  ...authStore.user,
                  nickname: profileForm.nickname,
                  email: profileForm.email,
                  phone: profileForm.phone
                });
              }
            } else {
              ElMessage.error(res?.msg || '保存失败');
            }
          } catch (error) {
            console.error('保存个人信息失败', error);
            ElMessage.error('保存失败');
          } finally {
            loading.value = false;
          }
        }
      });
    };
    
    onMounted(() => {
      loadUserInfo();
    });
    
    return {
      profileFormRef,
      loading,
      profileForm,
      profileRules,
      handleAvatarChange,
      saveProfile
    };
  }
});
</script>

<style scoped>
.avatar-uploader {
  text-align: center;
  border: 1px dashed var(--border-color);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color var(--transition-time);
}

.avatar-uploader:hover {
  border-color: var(--primary-color);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: var(--text-color-secondary);
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style> 