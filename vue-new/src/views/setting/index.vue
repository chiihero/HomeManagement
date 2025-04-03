<template>
  <div class="page-container">
    <div class="page-header mb-base">
      <h1 class="page-title m-0">系统设置</h1>
    </div>
    
    <el-tabs v-model="activeTab" class="mb-medium">
      <el-tab-pane label="个人信息" name="profile">
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
              <el-button type="primary" @click="saveProfile" :loading="loading.profile">保存个人信息</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="修改密码" name="password">
        <el-card shadow="hover" class="mb-medium">
          <template #header>
            <div class="flex justify-between align-center">
              <span>修改密码</span>
            </div>
          </template>
          
          <el-form 
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="form-container"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input 
                v-model="passwordForm.oldPassword" 
                placeholder="请输入当前密码" 
                show-password 
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="passwordForm.newPassword" 
                placeholder="请输入新密码" 
                show-password 
              />
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                placeholder="请再次输入新密码" 
                show-password 
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="loading.password">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="通知设置" name="notification">
        <el-card shadow="hover" class="mb-medium">
          <template #header>
            <div class="flex justify-between align-center">
              <span>通知设置</span>
            </div>
          </template>
          
          <el-form 
            ref="notificationFormRef"
            :model="notificationForm"
            label-width="120px"
            class="form-container"
          >
            <el-form-item label="邮件通知">
              <el-switch
                v-model="notificationForm.emailNotification"
                active-text="开启"
                inactive-text="关闭"
              />
              <div class="form-hint text-secondary mt-mini">开启后，系统会通过邮件发送重要通知。</div>
            </el-form-item>
            
            <el-form-item label="到期提醒">
              <el-switch
                v-model="notificationForm.expirationReminder"
                active-text="开启"
                inactive-text="关闭"
              />
              <div class="form-hint text-secondary mt-mini">开启后，物品即将到期时会收到提醒。</div>
            </el-form-item>
            
            <el-form-item label="提前提醒天数">
              <el-select v-model="notificationForm.reminderDays" placeholder="请选择提前提醒天数">
                <el-option :value="1" label="1天" />
                <el-option :value="3" label="3天" />
                <el-option :value="7" label="7天" />
                <el-option :value="15" label="15天" />
                <el-option :value="30" label="30天" />
              </el-select>
              <div class="form-hint text-secondary mt-mini">设置物品到期前多少天开始提醒。</div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings" :loading="loading.notification">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="系统信息" name="system">
        <el-card shadow="hover">
          <template #header>
            <div class="flex justify-between align-center">
              <span>系统信息</span>
            </div>
          </template>
          
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">家庭物品管理系统</el-descriptions-item>
            <el-descriptions-item label="版本号">1.0.0</el-descriptions-item>
            <el-descriptions-item label="最后更新时间">2023-04-01</el-descriptions-item>
            <el-descriptions-item label="技术栈">Vue 3 + TypeScript + Element Plus</el-descriptions-item>
            <el-descriptions-item label="开发者">XXX</el-descriptions-item>
            <el-descriptions-item label="联系邮箱">xxx@example.com</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { Plus } from '@element-plus/icons-vue';
import type { FormInstance } from 'element-plus';

export default defineComponent({
  name: 'Settings',
  components: {
    Plus
  },
  setup() {
    const authStore = useAuthStore();
    const activeTab = ref('profile');
    
    // 表单引用
    const profileFormRef = ref<FormInstance>();
    const passwordFormRef = ref<FormInstance>();
    const notificationFormRef = ref<FormInstance>();
    
    // 加载状态
    const loading = reactive({
      profile: false,
      password: false,
      notification: false
    });
    
    // 个人信息表单
    const profileForm = reactive({
      username: '',
      nickname: '',
      email: '',
      phone: '',
      avatar: ''
    });
    
    // 密码表单
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    
    // 通知设置表单
    const notificationForm = reactive({
      emailNotification: true,
      expirationReminder: true,
      reminderDays: 7
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
    
    // 密码校验规则
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { 
          validator: (rule: any, value: string, callback: any) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入密码不一致'));
            } else {
              callback();
            }
          }, 
          trigger: 'blur' 
        }
      ]
    };
    
    // 加载用户信息
    const loadUserInfo = () => {
      if (authStore.currentUser) {
        profileForm.username = authStore.currentUser.username || '';
        profileForm.nickname = authStore.currentUser.nickname || '';
        profileForm.email = authStore.currentUser.email || '';
        profileForm.phone = authStore.currentUser.phone || '';
        profileForm.avatar = authStore.currentUser.avatar || '';
      }
    };
    
    // 处理头像上传
    const handleAvatarChange = (file: any) => {
      // 在实际项目中，这里应该调用上传API
      const reader = new FileReader();
      reader.readAsDataURL(file.raw);
      reader.onload = () => {
        profileForm.avatar = reader.result as string;
      };
    };
    
    // 保存个人信息
    const saveProfile = async () => {
      if (!profileFormRef.value) return;
      
      await profileFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          loading.profile = true;
          try {
            // 这里应调用实际API
            setTimeout(() => {
              ElMessage.success('个人信息保存成功');
              loading.profile = false;
            }, 1000);
          } catch (error) {
            console.error('保存个人信息失败', error);
            ElMessage.error('保存失败');
            loading.profile = false;
          }
        }
      });
    };
    
    // 修改密码
    const changePassword = async () => {
      if (!passwordFormRef.value) return;
      
      await passwordFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          loading.password = true;
          try {
            // 这里应调用实际API
            setTimeout(() => {
              ElMessage.success('密码修改成功');
              passwordForm.oldPassword = '';
              passwordForm.newPassword = '';
              passwordForm.confirmPassword = '';
              loading.password = false;
            }, 1000);
          } catch (error) {
            console.error('修改密码失败', error);
            ElMessage.error('修改失败');
            loading.password = false;
          }
        }
      });
    };
    
    // 保存通知设置
    const saveNotificationSettings = () => {
      loading.notification = true;
      try {
        // 这里应调用实际API
        setTimeout(() => {
          ElMessage.success('通知设置保存成功');
          loading.notification = false;
        }, 1000);
      } catch (error) {
        console.error('保存通知设置失败', error);
        ElMessage.error('保存失败');
        loading.notification = false;
      }
    };
    
    onMounted(() => {
      loadUserInfo();
    });
    
    return {
      activeTab,
      profileFormRef,
      passwordFormRef,
      notificationFormRef,
      loading,
      profileForm,
      passwordForm,
      notificationForm,
      profileRules,
      passwordRules,
      handleAvatarChange,
      saveProfile,
      changePassword,
      saveNotificationSettings
    };
  }
});
</script>

<style scoped>
.form-hint {
  font-size: 12px;
  line-height: 1.5;
}

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