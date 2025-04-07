<template>
  <div class="login-container">
    <div class="login-card">
      <div class="title-container text-center mb-medium">
        <h3 class="title text-primary m-0 fw-bold">家庭物品管理系统</h3>
      </div>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        class="login-form w-100" 
        autocomplete="on" 
        label-position="left"
      >
        <div class="text-center mb-medium">
          <h3 class="form-title m-0">用户登录</h3>
        </div>
        
        <el-form-item prop="username">
          <el-input
            ref="userNameRef"
            v-model="loginForm.username"
            placeholder="用户名"
            name="username"
            type="text"
            autocomplete="on"
            prefix-icon="User"
            @keyup.enter="focusPassword"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            ref="passwordRef"
            v-model="loginForm.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="密码"
            name="password"
            autocomplete="on"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          >
            <template #suffix>
              <el-icon 
                class="pointer" 
                @click="passwordVisible = !passwordVisible"
              >
                <View v-if="passwordVisible" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <div class="flex justify-between align-center mb-medium">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <el-button type="text" @click="goToForgotPassword">忘记密码？</el-button>
        </div>
        
        <el-button 
          :loading="loading" 
          type="primary" 
          class="w-100 mb-medium" 
          @click="handleLogin"
        >
          登录
        </el-button>
        
        <div class="text-center">
          <span class="text-secondary">没有账号？</span>
          <router-link to="/register">
            <el-button type="text">立即注册</el-button>
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { User, Lock, View, Hide } from '@element-plus/icons-vue';

export default defineComponent({
  name: 'Login',
  components: {
    User,
    Lock,
    View,
    Hide
  },
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    
    const loginFormRef = ref();
    const userNameRef = ref();
    const passwordRef = ref();
    const loading = ref(false);
    const passwordVisible = ref(false);
    
    // 登录表单
    const loginForm = reactive({
      username: '',
      password: '',
      rememberMe: false
    });
    
    // 登录表单校验规则
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    };
    
    // 用户名输入后按回车跳转到密码框
    const focusPassword = () => {
      if (passwordRef.value) {
        passwordRef.value.focus();
      }
    };
    
    // 处理登录
    const handleLogin = async () => {
      if (!loginFormRef.value) return;
      
      await loginFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          loading.value = true;
          try {
            const success = await authStore.login(
              loginForm.username,
              loginForm.password
            );
            
            if (success) {
              // ElMessage.success('登录成功');
              router.push('/');
            } else {
              ElMessage.error('登录失败，请检查用户名和密码');
            }
          } catch (error) {
            console.error('登录异常', error);
            ElMessage.error('登录异常，请稍后重试');
          } finally {
            loading.value = false;
          }
        }
      });
    };
    
    // 前往忘记密码页面
    const goToForgotPassword = () => {
      router.push('/forgot-password');
    };
    
    onMounted(() => {
      // 自动聚焦用户名输入框
      if (userNameRef.value) {
        userNameRef.value.focus();
      }
    });
    
    return {
      loginFormRef,
      userNameRef,
      passwordRef,
      loginForm,
      loginRules,
      loading,
      passwordVisible,
      handleLogin,
      focusPassword,
      goToForgotPassword
    };
  }
});
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-color);
}

.login-card {
  width: 400px;
  padding: var(--spacing-medium);
  background: var(--bg-color-card);
  border-radius: var(--border-radius-base);
  box-shadow: var(--box-shadow);
  backdrop-filter: blur(10px);
}

.title {
  font-size: 26px;
}

.form-title {
  font-size: 18px;
  color: var(--text-color-primary);
  font-weight: 500;
}

.pointer {
  cursor: pointer;
}
</style> 