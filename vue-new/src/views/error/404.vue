<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-image">
        <img src="@/assets/images/404.svg" alt="404" />
      </div>
      <h1 class="error-title">404</h1>
      <p class="error-text">抱歉，您访问的页面不存在</p>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'NotFound',
  setup() {
    const router = useRouter();
    
    const goHome = () => {
      router.push('/');
    };
    
    const goBack = () => {
      router.back();
    };
    
    return {
      goHome,
      goBack
    };
  }
});
</script>

<style scoped>
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.error-content {
  text-align: center;
  padding: 40px;
  max-width: 500px;
}

.error-image {
  margin-bottom: 24px;
}

.error-image img {
  max-width: 100%;
  height: auto;
}

.error-title {
  font-size: 72px;
  font-weight: bold;
  color: #409EFF;
  margin: 0 0 16px 0;
}

.error-text {
  font-size: 20px;
  color: #606266;
  margin-bottom: 32px;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style> 