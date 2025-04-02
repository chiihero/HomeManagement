<template>
  <div class="login-container">
    <div class="login-card">
      <div class="title-container">
        <h3 class="title">家庭物品管理系统</h3>
      </div>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        class="login-form" 
        autocomplete="on" 
        label-position="left"
      >
        <div class="form-header">
          <h3 class="form-title">用户登录</h3>
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
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="密码"
            name="password"
            autocomplete="on"
            prefix-icon="Lock"
          >
            <template #suffix>
              <el-icon 
                class="password-toggle" 
                @click="passwordVisible = !passwordVisible"
              >
                <View v-if="passwordVisible" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <div class="form-options">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <el-button type="text" @click="goToForgotPassword">忘记密码？</el-button>
        </div>
        
        <el-button 
          :loading="loading" 
          type="primary" 
          class="login-button" 
          @click="handleLogin"
        >
          登录
        </el-button>
        
        <div class="register-link">
          <span>没有账号？</span>
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
              ElMessage.success('登录成功');
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
      loginForm,
      loginRules,
      loading,
      passwordVisible,
      handleLogin,
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
  background-color: #f0f2f5;
  background-image: url('@/assets/images/login-bg.jpg');
  background-size: cover;
  background-position: center;
}

.login-card {
  width: 400px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.title-container {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 26px;
  color: #409EFF;
  margin: 0;
  font-weight: bold;
}

.login-form {
  width: 100%;
}

.form-header {
  text-align: center;
  margin-bottom: 20px;
}

.form-title {
  font-size: 18px;
  color: #303133;
  margin: 0;
  font-weight: 500;
}

.password-toggle {
  cursor: pointer;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
  margin-bottom: 20px;
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: #606266;
}
</style> 