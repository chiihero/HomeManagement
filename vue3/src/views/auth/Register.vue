<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { message } from "@/utils/message";
import { useNav } from "@/layout/hooks/useNav";
import type { FormInstance } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import type { RegisterRequest } from "@/types/user";

import User from "@iconify-icons/ri/user-3-fill";
import Lock from "@iconify-icons/ri/lock-fill";
import Message from "@iconify-icons/ri/mail-fill";
import View from "@iconify-icons/ri/eye-line";
import Hide from "@iconify-icons/ri/eye-off-line";

defineOptions({
  name: "Register"
});

const router = useRouter();
const loading = ref(false);
const registerFormRef = ref<FormInstance>();
const { title } = useNav();

const passwordVisible = ref(false);
const confirmPasswordVisible = ref(false);
const termsDialogVisible = ref(false);
const privacyDialogVisible = ref(false);

// 注册表单
const registerForm = reactive<RegisterRequest>({
  username: "",
  email: "",
  password: "",
  confirmPassword: ""
});

// 同意条款
const agreeTerms = ref(false);

// 验证密码是否一致
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

// 注册表单校验规则
const registerRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" }
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" }
  ]
};

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return;

  await registerFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true;
      try {
        const res = await useUserStoreHook().register(registerForm);

        if (res.success) {
          message("注册成功，请登录", { type: "success" });
          // 使用一个延迟，让用户看到成功消息后再跳转
          setTimeout(() => {
            router.push("/auth/login");
          }, 1500);
        } else {
          message(res.message || "注册失败，请检查信息后重试", {
            type: "error"
          });
        }
      } catch (error) {
        console.error("注册异常", error);
        message("注册异常，请稍后重试", { type: "error" });
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

// 按回车键注册
function onkeypress({ code }: KeyboardEvent) {
  if (["Enter", "NumpadEnter"].includes(code)) {
    handleRegister();
  }
}

onMounted(() => {
  window.document.addEventListener("keypress", onkeypress);
});

onBeforeUnmount(() => {
  window.document.removeEventListener("keypress", onkeypress);
});
</script>

<template>
  <div class="register-container select-none">
    <div class="register-card">
      <div class="title-container text-center mb-4">
        <h3 class="title text-primary m-0">{{ title }}</h3>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form w-full"
        size="large"
        label-position="left"
      >
        <div class="text-center mb-4">
          <h3 class="form-title m-0">用户注册</h3>
        </div>

        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            clearable
            :prefix-icon="useRenderIcon(User)"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱"
            clearable
            :prefix-icon="useRenderIcon(Message)"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="密码"
            :prefix-icon="useRenderIcon(Lock)"
          >
            <template #suffix>
              <el-icon
                class="cursor-pointer"
                @click="passwordVisible = !passwordVisible"
              >
                <component :is="useRenderIcon(passwordVisible ? View : Hide)" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            :type="confirmPasswordVisible ? 'text' : 'password'"
            placeholder="确认密码"
            :prefix-icon="useRenderIcon(Lock)"
          >
            <template #suffix>
              <el-icon
                class="cursor-pointer"
                @click="confirmPasswordVisible = !confirmPasswordVisible"
              >
                <component
                  :is="useRenderIcon(confirmPasswordVisible ? View : Hide)"
                />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-button
          :loading="loading"
          type="primary"
          class="w-full mb-4"
          @click="handleRegister"
        >
          注册
        </el-button>

        <div class="text-center">
          <span class="text-gray-500">已有账号？</span>
          <router-link to="/auth/login">
            <el-button type="primary" link>返回登录</el-button>
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.register-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--el-bg-color);
}

.register-card {
  width: 400px;
  padding: 24px;
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  backdrop-filter: blur(10px);
}

.title {
  font-size: 26px;
  font-weight: 600;
}

.form-title {
  font-size: 18px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.terms-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 0 16px;
}

.terms-content h4 {
  font-size: 16px;
  margin-bottom: 12px;
}

.terms-content p {
  margin-bottom: 10px;
  line-height: 1.6;
}

.terms-content ul {
  padding-left: 20px;
}

.terms-content li {
  margin-bottom: 8px;
  line-height: 1.6;
}
</style>
