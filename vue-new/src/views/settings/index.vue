<template>
  <div class="page-container">
    <div class="page-header mb-base">
      <h1 class="page-title m-0">设置</h1>
    </div>
    
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="mb-medium">
          <template #header>
            <div class="flex justify-between align-center">
              <span>个人资料</span>
              <el-button type="primary" link @click="$router.push('/setting/profile')">
                编辑
              </el-button>
            </div>
          </template>
          
          <div class="profile-info">
            <div class="avatar-container mb-medium">
              <el-avatar :size="100" :src="userInfo.avatar" />
            </div>
            <div class="info-item">
              <span class="label">用户名：</span>
              <span class="value">{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">昵称：</span>
              <span class="value">{{ userInfo.nickname }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱：</span>
              <span class="value">{{ userInfo.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机：</span>
              <span class="value">{{ userInfo.phone }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" class="mb-medium">
          <template #header>
            <div class="flex justify-between align-center">
              <span>通知设置</span>
              <el-button type="primary" link @click="$router.push('/setting/notification')">
                编辑
              </el-button>
            </div>
          </template>
          
          <div class="notification-info">
            <div class="info-item">
              <span class="label">邮件通知：</span>
              <el-tag :type="userInfo.emailNotification ? 'success' : 'info'">
                {{ userInfo.emailNotification ? '开启' : '关闭' }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">到期提醒：</span>
              <el-tag :type="userInfo.expirationReminder ? 'success' : 'info'">
                {{ userInfo.expirationReminder ? '开启' : '关闭' }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">提前提醒天数：</span>
              <span class="value">{{ userInfo.reminderDays }}天</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" class="mb-medium">
          <template #header>
            <div class="flex justify-between align-center">
              <span>系统设置</span>
              <el-button type="primary" link @click="$router.push('/setting/system')">
                编辑
              </el-button>
            </div>
          </template>
          
          <div class="system-info">
            <div class="info-item">
              <span class="label">系统名称：</span>
              <span class="value">{{ systemInfo.systemName }}</span>
            </div>
            <div class="info-item">
              <span class="label">系统版本：</span>
              <span class="value">{{ systemInfo.version }}</span>
            </div>
            <div class="info-item">
              <span class="label">系统描述：</span>
              <span class="value">{{ systemInfo.description }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { getSystemSettings } from '@/api/settings';
import { getUserProfile } from '@/api/user';
import type { SystemSettings, UserNotificationSettings } from '@/api/settings';
import type { UserProfile } from '@/api/user';

export default defineComponent({
  name: 'Setting',
  setup() {
    const userInfo = ref<Partial<UserProfile & UserNotificationSettings>>({
      username: '',
      nickname: '',
      email: '',
      phone: '',
      avatar: '',
      emailNotification: true,
      expirationReminder: true,
      reminderDays: 7
    });
    
    const systemInfo = ref<Partial<SystemSettings>>({
      systemName: '',
      version: '1.0.0',
      description: ''
    });
    
    // 加载用户信息
    const loadUserInfo = async () => {
      try {
        const loadingInstance = ElLoading.service({
          text: '加载用户信息...',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        const res = await getUserProfile();
        if (res && res.code === 200) {
          const userData = res.data;
          userInfo.value = {
            ...userData,
            emailNotification: (userData as any).emailNotification ?? true,
            expirationReminder: (userData as any).expirationReminder ?? true,
            reminderDays: (userData as any).reminderDays ?? 7
          };
        } else {
          ElMessage.error(res?.msg || '获取用户信息失败');
        }
        
        loadingInstance.close();
      } catch (error) {
        console.error('获取用户信息失败', error);
        ElMessage.error('获取用户信息失败');
      }
    };
    
    // 加载系统信息
    const loadSystemInfo = async () => {
      try {
        const res = await getSystemSettings();
        if (res && res.code === 200) {
          systemInfo.value = res.data;
        } else {
          ElMessage.error(res?.msg || '获取系统信息失败');
        }
      } catch (error) {
        console.error('获取系统信息失败', error);
        ElMessage.error('获取系统信息失败');
      }
    };
    
    onMounted(() => {
      loadUserInfo();
      loadSystemInfo();
    });
    
    return {
      userInfo,
      systemInfo
    };
  }
});
</script>

<style scoped>
.profile-info,
.notification-info,
.system-info {
  padding: 10px 0;
}

.avatar-container {
  text-align: center;
}

.info-item {
  margin-bottom: 15px;
  line-height: 1.5;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #606266;
  margin-right: 10px;
}

.value {
  color: #303133;
}
</style> 