<template>
  <div class="bg-gray-50 min-h-screen p-4 md:p-6">
    <!-- 头部 -->
    <el-card class="mb-6 shadow-sm border-0">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <h1 class="text-xl md:text-2xl font-bold text-gray-800 m-0 flex items-center">
          <el-icon class="mr-2 text-primary"><View /></el-icon>物品详情
        </h1>
        <div class="flex space-x-2">
          <el-button type="primary" @click="router.push(`/entitySearch/edit/${entityId}`)">
            <el-icon class="mr-1"><Edit /></el-icon>编辑
          </el-button>
          <el-button @click="router.push('/entitySearch/index')">
            <el-icon class="mr-1"><Back /></el-icon>返回
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 实体详情 -->
    <EntityDetail :entity-id="entityId" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import EntityDetail from "@/views/entity/components/EntityDetail.vue";
import { View, Edit, Back } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const entityId = ref<string>("");

onMounted(() => {
  entityId.value = route.params.id as string;
});
</script> 