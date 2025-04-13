<template>
  <div class="bg-gray-50 min-h-screen p-4 md:p-6">
    <!-- 头部 -->
    <el-card class="mb-6 shadow-sm border-0">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <h1 class="text-xl md:text-2xl font-bold text-gray-800 m-0 flex items-center">
          <el-icon class="mr-2 text-primary"><Edit /></el-icon>编辑物品
        </h1>
        <el-button @click="router.push(`/entitySearch/view/${entityId}`)">
          <el-icon class="mr-1"><Back /></el-icon>返回
        </el-button>
      </div>
    </el-card>

    <!-- 实体表单 -->
    <EntityForm 
      :entity-id="entityId" 
      :is-edit="true"
      @save-success="handleSaveSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import EntityForm from "@/views/entity/components/EntityForm.vue";
import { Edit, Back } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const entityId = ref<string>("");

onMounted(() => {
  entityId.value = route.params.id as string;
});

// 保存成功后的处理
const handleSaveSuccess = () => {
  ElMessage.success("保存成功");
  router.push(`/entitySearch/view/${entityId.value}`);
};
</script> 