<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import {
  updateUserProfile,
  updateUserPassword,
  uploadUserAvatar,
  getUserInfo
} from "@/api/user";
import type { UserProfile } from "@/api/user";

const userStore = useUserStoreHook();
const avatarUrl = ref(userStore.avatar || "");
const loading = ref(false);

// 个人资料表单
const profileForm = reactive<UserProfile>({
  username: userStore.username || "",
  nickname: userStore.nickname || "",
  email: "",
  phone: "",
  avatar: avatarUrl.value
});

// 密码修改表单
const passwordForm = reactive({
  currentPassword: "",
  newPassword: "",
  confirmPassword: ""
});

// 表单校验规则
const profileRules = {
  nickname: [
    { required: true, message: "请输入昵称", trigger: "blur" },
    { min: 2, max: 20, message: "长度在2到20个字符之间", trigger: "blur" }
  ],
  email: [
    { pattern: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/, message: "请输入正确的邮箱地址", trigger: "blur" }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" }
  ]
};

const passwordRules = {
  currentPassword: [
    { required: true, message: "请输入当前密码", trigger: "blur" },
    { min: 6, message: "密码长度至少为6个字符", trigger: "blur" }
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度至少为6个字符", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
};

// 表单引用
const profileFormRef = ref();
const passwordFormRef = ref();

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo();
    if (res.code === 200 && res.data) {
      profileForm.email = res.data.email || "";
      profileForm.phone = res.data.phone || "";
      // 其他字段如果需要也可以在这里填充
    }
  } catch (error) {
    console.error("获取用户信息失败", error);
    ElMessage.error("获取用户信息失败");
  }
};

onMounted(() => {
  fetchUserInfo();
});

// 头像上传前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === "image/jpeg" || file.type === "image/png";
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error("上传头像图片只能是 JPG 或 PNG 格式!");
  }
  if (!isLt2M) {
    ElMessage.error("上传头像图片大小不能超过 2MB!");
  }
  return isJPG && isLt2M;
};

// 上传头像
const handleAvatarSuccess = async (file) => {
  loading.value = true;
  try {
    const res = await uploadUserAvatar(file.raw);
    if (res.code === 200 && res.data) {
      avatarUrl.value = res.data;
      profileForm.avatar = res.data;
      userStore.SET_AVATAR(res.data);
      ElMessage.success("头像上传成功");
    } else {
      ElMessage.error(res.message || "头像上传失败");
    }
  } catch (error) {
    console.error("头像上传错误", error);
    ElMessage.error("头像上传失败");
  } finally {
    loading.value = false;
  }
};

// 提交个人资料
const submitProfile = async () => {
  if (!profileFormRef.value) return;
  
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    loading.value = true;
    try {
      const res = await updateUserProfile(profileForm);
      if (res.code === 200) {
        ElMessage.success("个人资料更新成功");
        // 更新Store中的用户信息
        userStore.setUserInfo({
          nickname: profileForm.nickname,
          avatar: profileForm.avatar
        });
      } else {
        ElMessage.error(res.message || "个人资料更新失败");
      }
    } catch (error) {
      console.error("更新个人资料错误", error);
      ElMessage.error("个人资料更新失败");
    } finally {
      loading.value = false;
    }
  });
};

// 提交密码修改
const submitPassword = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    if (passwordForm.newPassword === passwordForm.currentPassword) {
      ElMessage.warning("新密码不能与当前密码相同");
      return;
    }
    
    loading.value = true;
    try {
      const res = await updateUserPassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword
      });
      
      if (res.code === 200) {
        ElMessage.success("密码修改成功，请重新登录");
        // 清空表单
        passwordForm.currentPassword = "";
        passwordForm.newPassword = "";
        passwordForm.confirmPassword = "";
        // 提示用户需要重新登录
        ElMessageBox.confirm(
          "密码已修改成功，需要重新登录以应用新密码",
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          }
        ).then(() => {
          userStore.logOut();
        });
      } else {
        ElMessage.error(res.message || "密码修改失败，请检查当前密码是否正确");
      }
    } catch (error) {
      console.error("密码修改错误", error);
      ElMessage.error("密码修改失败");
    } finally {
      loading.value = false;
    }
  });
};
</script>

<template>
  <div class="user-profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>个人信息</h3>
        </div>
      </template>
      
      <el-tabs>
        <el-tab-pane label="基本资料">
          <div class="avatar-container">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarSuccess"
            >
              <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              <div class="avatar-text">点击上传头像</div>
            </el-upload>
          </div>
          
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" />
            </el-form-item>
            
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="submitProfile" :loading="loading">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="修改密码">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="password-form"
          >
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                show-password
                placeholder="请输入当前密码"
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请确认新密码"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="submitPassword" :loading="loading">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.user-profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.avatar-uploader {
  text-align: center;
}

.avatar {
  width: 128px;
  height: 128px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 128px;
  height: 128px;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-text {
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}

.profile-form, .password-form {
  max-width: 500px;
  margin: 0 auto;
}
</style> 