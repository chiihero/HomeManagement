<template>
  <div class="w-full">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="max-w-3xl mx-auto"
      status-icon
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入物品名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型" prop="type">
            <el-input v-model="form.type" placeholder="请输入物品类型" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属位置" prop="parentId">
            <el-tree
              :current-node-key="currentNodeKey"
              :data="computedTreeData"
              :props="{ label: 'name', children: 'children' }"
              node-key="id"
              highlight-current
              default-expand-all
              :expand-on-click-node="false"
              class="location-tree"
              @current-change="handleLocationSelect"
            >
              <template #default="{ node, data }">
                <div class="custom-tree-node">
                  <span>
                    <el-icon v-if="data.type === 'space'"><Folder /></el-icon>
                    <el-icon v-else><Goods /></el-icon>
                    {{ node.label }}
                  </span>
                </div>
              </template>
            </el-tree>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select
              v-model="form.status"
              placeholder="请选择状态"
              class="w-full"
            >
              <el-option label="可用" value="AVAILABLE" />
              <el-option label="使用中" value="IN_USE" />
              <el-option label="维护中" value="MAINTENANCE" />
              <el-option label="已处置" value="DISPOSED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="价格" prop="price">
            <el-input-number
              v-model="form.price"
              :precision="2"
              :step="0.1"
              :min="0"
              placeholder="请输入价格"
              class="w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="购买日期" prop="purchaseDate">
            <el-date-picker
              v-model="form.purchaseDate"
              type="date"
              placeholder="请选择购买日期"
              value-format="YYYY-MM-DD"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="保修期" prop="warrantyPeriod">
            <el-input-number
              v-model="form.warrantyPeriod"
              :min="0"
              :max="120"
              placeholder="请输入保修期（月）"
              class="w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入物品描述"
        />
      </el-form-item>

      <el-form-item label="标签" prop="tags">
        <el-select
          v-model="form.tags"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="请选择或输入标签"
          @change="handleTagsChange"
        >
          <el-option
            v-for="tag in existingTags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
            :style="{ backgroundColor: tag.color, color: getContrastColor(tag.color) }"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="图片" prop="images">
        <el-upload
          ref="uploadRef"
          :file-list="imageList"
          action="#"
          list-type="picture-card"
          multiple
          :auto-upload="false"
          :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove"
          :before-upload="beforeImageUpload"
          :on-change="handleImageChange"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <el-dialog v-model="dialogVisible" append-to-body>
          <img class="w-full" :src="dialogImageUrl" alt="Preview Image" />
        </el-dialog>
      </el-form-item>

      <el-form-item class="mt-8">
        <div class="flex justify-end gap-4">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSubmit"
            >保存</el-button
          >
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance } from "element-plus";
import { Folder, Goods, Plus } from "@element-plus/icons-vue";
import { useEntityForm } from '../composables/useEntityForm';

// 添加此注释，需要在项目中创建相应的类型定义
// @ts-ignore
import type { Entity, EntityFormData, EntityStatus, Tag } from "@/types/entity";
import { useEntityImageUpload } from "../composables/useEntityImageUpload";
import { useUserStoreHook } from "@/store/modules/user"; // 引入用户store

interface Props {
  entity: Entity | null;
  treeData: Entity[];
  existingTags: Tag[];
  isEditing: boolean;
  saving: boolean;
  
}
const { getContrastColor } = useEntityForm();

// 获取用户store
const userStore = useUserStoreHook();

// 表单数据
const form = reactive<EntityFormData>({
  name: "",
  type: "item",
  parentId: "",
  status: "normal" as EntityStatus,
  location: "",
  price: 0,
  purchaseDate: "",
  warrantyPeriod: 0,
  description: "",
  tags: [],
  images: [],
  userId: "" // 添加userId字段
});

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: "submit", form: EntityFormData): void;
  (e: "cancel"): void;
}>();

const formRef = ref<FormInstance>();

// 使用图片上传composable
const {
  dialogVisible,
  dialogImageUrl,
  imageList,
  handlePictureCardPreview,
  handleRemove,
  beforeImageUpload,
  handleImageChange,
  resetImageList,
  setImageList
} = useEntityImageUpload();

// 位置选择相关变量和函数
const selectedLocationName = ref<string>("根空间");
const currentNodeKey = ref<string>("0");


// 处理位置选择
const handleLocationSelect = (data: any) => {
  if (data && data.id) {
    form.parentId = data.id;
    selectedLocationName.value = data.name;
  }
};

// 确保树中有根空间节点
const ensureRootSpace = (data: any[]) => {
  // 检查是否已存在根空间
  const hasRootSpace = data.some(item => item.id === "0");

  if (!hasRootSpace && data.length > 0) {
    // 添加根空间节点
    return [
      {
        id: "0",
        name: "根空间",
        type: "space",
        children: [...data]
      }
    ];
  }

  return data;
};

// 计算后的树数据，确保包含根空间
const computedTreeData = computed(() => {
  return ensureRootSpace(props.treeData);
});


// 表单验证规则
const rules = {
  name: [
    { required: true, message: "请输入物品名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" }
  ],
  type: [{ required: true, message: "请输入物品类型", trigger: "blur" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
  price: [{ required: true, message: "请输入价格", trigger: "blur" }]
};

// 更新选中位置的名称
const updateSelectedLocationName = () => {
  if (!form.parentId) {
    selectedLocationName.value = "根空间";
    return;
  }

  // 找出对应的位置名称
  const findNodeName = (nodes: any[]): string | null => {
    for (const node of nodes) {
      if (node.id === form.parentId) {
        return node.name;
      }
      if (node.children && node.children.length > 0) {
        const result = findNodeName(node.children);
        if (result) return result;
      }
    }
    return null;
  };

  const name = findNodeName(props.treeData);
  if (name) {
    selectedLocationName.value = name;
  } else {
    selectedLocationName.value = "根空间";
  }
};

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async valid => {
    if (valid) {
      try {
        // 确保userId正确设置
        form.userId = userStore.userId?.toString() || "";
        
        // 确保图片数据正确传递
        console.log("提交前的图片列表:", imageList.value);
        
        // 创建一个新的表单数据对象，包含所有必要的字段
        const formData = {
          name: form.name,
          type: form.type,
          parentId: form.parentId,
          status: form.status,
          location: form.location,
          price: form.price,
          purchaseDate: form.purchaseDate,
          warrantyPeriod: form.warrantyPeriod,
          description: form.description,
          tags: form.tags, // 确保这里传递的是标签ID数组
          images: [...imageList.value], // 使用展开运算符创建新数组
          userId: form.userId
        };
        
        console.log("提交的表单数据:", formData);
        // 添加标签信息的日志输出，帮助调试
        console.log("提交的标签数据:", form.tags);
        emit("submit", formData);
      } catch (error) {
        console.error("提交表单失败:", error);
        ElMessage.error("提交表单失败，请重试");
      }
    }
  });
};

// 处理标签变化
const handleTagsChange = (value: any) => {
  console.log("标签变化:", value);
  // 确保form.tags被正确更新
  form.tags = value;
};

// 处理取消
const handleCancel = () => {
  emit("cancel");
};

// 监听实体变化
watch(
  () => props.entity,
  newEntity => {
    if (newEntity) {
      // 确保表单数据类型正确
      form.name = newEntity.name || "";
      form.type = newEntity.type || "item";
      form.parentId = newEntity.parentId || "";
      // @ts-ignore
      form.status = (newEntity.status as EntityStatus) || ("normal" as EntityStatus);
      form.location = (newEntity as any).location || "";
      form.price = newEntity.price || 0;
      form.purchaseDate = newEntity.purchaseDate || "";
      form.warrantyPeriod = newEntity.warrantyPeriod || 0;
      form.description = newEntity.description || "";
      
      // 确保正确设置标签数据
      form.tags = Array.isArray(newEntity.tags) ? [...newEntity.tags] : [];
      console.log("设置的标签数据:", form.tags);
      
      form.images = newEntity.images || [];
      form.userId = newEntity.userId || userStore.userId?.toString() || ""; // 保留原有userId或使用当前用户ID

      // 更新图片列表
      if (newEntity.images) {
        setImageList(newEntity.images);
      } else {
        resetImageList();
      }

      // 更新当前选中的节点和位置名称
      currentNodeKey.value = newEntity.parentId || "";
      updateSelectedLocationName();
    } else {
      // 重置表单
      form.name = "";
      form.type = "item";
      form.parentId = "";
      // @ts-ignore
      form.status = "normal" as EntityStatus;
      form.location = "";
      form.price = 0;
      form.purchaseDate = "";
      form.warrantyPeriod = 0;
      form.description = "";
      form.tags = [];
      form.images = [];
      form.userId = userStore.userId?.toString() || ""; // 重置为当前用户ID
      resetImageList();
      currentNodeKey.value = "0";
      selectedLocationName.value = "根空间";
    }
  },
  { immediate: true }
);
</script>

<style scoped>
.location-tree {
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background-color: #fff;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.location-tree .el-tree-node__content {
  height: 32px;
}

.selected-location {
  margin-top: 8px;
  padding: 8px 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
