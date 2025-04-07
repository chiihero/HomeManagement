<template>
  <div class="form-section">
    <h3>物品图片</h3>
    <div class="upload-container">
      <el-upload
        action="#"
        list-type="picture-card"
        :auto-upload="false"
        :file-list="[]"
        :on-change="handleImageChange"
        :limit="5"
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
    </div>
    
    <div class="image-preview" v-if="images && images.length > 0">
      <div v-for="(image, index) in images" :key="index" class="image-item">
        <el-image :src="image.url || image.imagePath" fit="cover" />
        <div class="image-actions">
          <el-button type="danger" circle icon="Delete" @click="removeImage(index)"></el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { Plus, Delete } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

export default defineComponent({
  name: 'EntityImageUpload',
  components: {
    Plus,
    Delete
  },
  props: {
    images: {
      type: Array,
      required: true
    }
  },
  emits: ['update:images'],
  setup(props, { emit }) {
    // 处理图片变更
    const handleImageChange = (file: any) => {
      if (!file) return;
      
      // 检查文件类型
      const isImage = file.raw.type.startsWith('image/');
      if (!isImage) {
        ElMessage.error('只能上传图片文件');
        return;
      }
      
      // 检查文件大小
      const isLt2M = file.raw.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过2MB');
        return;
      }
      
      // 创建预览并添加到表单
      const reader = new FileReader();
      reader.readAsDataURL(file.raw);
      reader.onload = () => {
        const newImages = [...props.images];
        newImages.push({
          file: file.raw,
          url: reader.result as string
        });
        emit('update:images', newImages);
      };
    };
    
    // 移除图片
    const removeImage = (index: number) => {
      const newImages = [...props.images];
      newImages.splice(index, 1);
      emit('update:images', newImages);
    };

    return {
      handleImageChange,
      removeImage
    };
  }
});
</script>

<style scoped>
.form-section {
  margin-bottom: 24px;
}

.form-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 500;
}

.upload-container {
  margin-bottom: 16px;
}

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  position: relative;
  width: 100%;
  height: 120px;
  margin-bottom: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.image-item .el-image {
  width: 100%;
  height: 100%;
  display: block;
}

.image-actions {
  position: absolute;
  right: 8px;
  bottom: 8px;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
}
</style> 