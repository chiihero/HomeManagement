<template>
  <div class="page-container">
    <div class="page-header mb-base">
      <h1 class="page-title m-0">通知设置</h1>
    </div>
    
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
          <el-button type="primary" @click="saveNotificationSettings" :loading="loading">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { 
  updateUserNotifications,
  getUserProfile
} from '@/api/user';

export default defineComponent({
  name: 'Notification',
  setup() {
    const loading = ref(false);
    const notificationFormRef = ref<FormInstance>();
    
    // 通知设置表单
    const notificationForm = reactive({
      emailNotification: true,
      expirationReminder: true,
      reminderDays: 7
    });
    
    // 加载通知设置
    const loadNotificationSettings = async () => {
      try {
        const loadingInstance = ElLoading.service({
          text: '加载通知设置...',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        const res = await getUserProfile();
        if (res && res.code === 200) {
          const userData = res.data;
          // 从用户设置中获取通知设置
          notificationForm.emailNotification = userData.emailNotification || true;
          notificationForm.expirationReminder = userData.expirationReminder || true;
          notificationForm.reminderDays = userData.reminderDays || 7;
        } else {
          ElMessage.error(res?.msg || '获取通知设置失败');
        }
        
        loadingInstance.close();
      } catch (error) {
        console.error('获取通知设置失败', error);
        ElMessage.error('获取通知设置失败');
      }
    };
    
    // 保存通知设置
    const saveNotificationSettings = async () => {
      loading.value = true;
      try {
        const res = await updateUserNotifications({
          emailNotification: notificationForm.emailNotification,
          expirationReminder: notificationForm.expirationReminder,
          reminderDays: notificationForm.reminderDays
        });
        
        if (res && res.code === 200) {
          ElMessage.success('通知设置保存成功');
        } else {
          ElMessage.error(res?.msg || '保存失败');
        }
      } catch (error) {
        console.error('保存通知设置失败', error);
        ElMessage.error('保存失败');
      } finally {
        loading.value = false;
      }
    };
    
    onMounted(() => {
      loadNotificationSettings();
    });
    
    return {
      notificationFormRef,
      loading,
      notificationForm,
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
</style> 