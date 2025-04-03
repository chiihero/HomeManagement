<template>
  <div class="register-container">
    <div class="register-card">
      <div class="title-container text-center mb-medium">
        <h3 class="title text-primary m-0">家庭物品管理系统</h3>
      </div>
      
      <el-form 
        ref="registerFormRef" 
        :model="registerForm" 
        :rules="registerRules" 
        class="register-form w-100" 
        label-position="left"
      >
        <div class="text-center mb-medium">
          <h3 class="form-title m-0">用户注册</h3>
        </div>
        
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="密码"
            prefix-icon="Lock"
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
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            :type="confirmPasswordVisible ? 'text' : 'password'"
            placeholder="确认密码"
            prefix-icon="Lock"
          >
            <template #suffix>
              <el-icon 
                class="pointer" 
                @click="confirmPasswordVisible = !confirmPasswordVisible"
              >
                <View v-if="confirmPasswordVisible" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="agreeTerms">
          <el-checkbox v-model="registerForm.agreeTerms">
            我已阅读并同意
            <el-button type="text" @click="showTerms">服务条款</el-button>
            和
            <el-button type="text" @click="showPrivacyPolicy">隐私政策</el-button>
          </el-checkbox>
        </el-form-item>
        
        <el-button 
          :loading="loading" 
          type="primary" 
          class="w-100 mb-medium" 
          @click="handleRegister"
        >
          注册
        </el-button>
        
        <div class="text-center">
          <span class="text-secondary">已有账号？</span>
          <router-link to="/login">
            <el-button type="text">返回登录</el-button>
          </router-link>
        </div>
      </el-form>
    </div>
    
    <!-- 服务条款对话框 -->
    <el-dialog
      v-model="termsDialogVisible"
      title="服务条款"
      width="50%"
    >
      <div class="terms-content">
        <h4>服务条款</h4>
        <p>欢迎使用家庭物品管理系统！使用本系统即表示您同意以下条款：</p>
        <ul>
          <li>用户应当遵守中华人民共和国相关法律法规。</li>
          <li>用户应当妥善保管账号信息，不得将账号借给他人使用。</li>
          <li>用户应当对自己在系统中的操作负责。</li>
          <li>本系统保留随时修改服务条款的权利。</li>
        </ul>
      </div>
    </el-dialog>
    
    <!-- 隐私政策对话框 -->
    <el-dialog
      v-model="privacyDialogVisible"
      title="隐私政策"
      width="50%"
    >
      <div class="terms-content">
        <h4>隐私政策</h4>
        <p>我们非常重视您的隐私保护，使用本系统即表示您同意以下隐私条款：</p>
        <ul>
          <li>我们会收集您的基本账号信息，如用户名、邮箱等。</li>
          <li>系统会记录您的登录时间和操作记录，用于安全审计。</li>
          <li>我们不会将您的个人信息泄露给任何第三方。</li>
          <li>您有权随时删除您的账号及相关数据。</li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { User, Message, Lock, View, Hide } from '@element-plus/icons-vue';

export default defineComponent({
  name: 'Register',
  components: {
    User,
    Message,
    Lock,
    View,
    Hide
  },
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    
    const registerFormRef = ref();
    const loading = ref(false);
    const passwordVisible = ref(false);
    const confirmPasswordVisible = ref(false);
    const termsDialogVisible = ref(false);
    const privacyDialogVisible = ref(false);
    
    // 注册表单
    const registerForm = reactive({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      agreeTerms: false
    });
    
    // 验证密码是否一致
    const validateConfirmPassword = (rule: any, value: string, callback: any) => {
      if (value !== registerForm.password) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    
    // 注册表单校验规则
    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ],
      agreeTerms: [
        { 
          validator: (rule: any, value: boolean, callback: any) => {
            if (value) {
              callback();
            } else {
              callback(new Error('请阅读并同意服务条款和隐私政策'));
            }
          }, 
          trigger: 'change' 
        }
      ]
    };
    
    // 处理注册
    const handleRegister = async () => {
      if (!registerFormRef.value) return;
      
      await registerFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          loading.value = true;
          try {
            const success = await authStore.register({
              username: registerForm.username,
              email: registerForm.email,
              password: registerForm.password
            });
            
            if (success) {
              ElMessage.success('注册成功，请登录');
              router.push('/login');
            } else {
              ElMessage.error('注册失败，请检查信息后重试');
            }
          } catch (error) {
            console.error('注册异常', error);
            ElMessage.error('注册异常，请稍后重试');
          } finally {
            loading.value = false;
          }
        }
      });
    };
    
    // 显示服务条款
    const showTerms = () => {
      termsDialogVisible.value = true;
    };
    
    // 显示隐私政策
    const showPrivacyPolicy = () => {
      privacyDialogVisible.value = true;
    };
    
    return {
      registerFormRef,
      registerForm,
      registerRules,
      loading,
      passwordVisible,
      confirmPasswordVisible,
      termsDialogVisible,
      privacyDialogVisible,
      handleRegister,
      showTerms,
      showPrivacyPolicy
    };
  }
});
</script>

<style scoped>
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-color);
}

.register-card {
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

.terms-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 0 var(--spacing-medium);
}

.terms-content h4 {
  font-size: 16px;
  margin-bottom: var(--spacing-base);
}

.terms-content p {
  margin-bottom: var(--spacing-small);
  line-height: 1.6;
}

.terms-content ul {
  padding-left: var(--spacing-medium);
}

.terms-content li {
  margin-bottom: var(--spacing-mini);
  line-height: 1.6;
}
</style> 