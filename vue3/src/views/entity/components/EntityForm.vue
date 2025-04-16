<template>
  <div class="w-full">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="auto" class="max-w-3xl mx-auto"
      style="max-width: 600px" status-icon>
      <div class="container">
        <el-form-item>
          <el-button type="primary" size="large" :loading="saving" :icon="Check" @click="handleSubmit">保存</el-button>
          <el-button size="large" :icon="Close" @click="handleCancel">取消</el-button>
        </el-form-item>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入物品名称" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="类型" prop="type">
            <!-- <el-input v-model="form.type" placeholder="请输入物品类型" /> -->
            <el-select v-model="form.type" filterable allow-create default-first-option :reserve-keyword="false"
              placeholder="Choose tags for your article" style="width: 240px">
              <el-option value="物品" />
              <el-option value="空间" />
              <el-option value="药品" />
              <el-option value="耗材" />
              <el-option value="食品" />
            </el-select>

          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态" class="w-full">
              <el-option label="正常" value="normal" />
              <el-option label="损坏" value="damaged" />
              <el-option label="丢弃" value="discarded" />
              <el-option label="过期" value="expired" />
              <el-option label="借出" value="lent" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="数量" prop="quantity">
            <el-input-number v-model="form.quantity" :min="0" placeholder="请输入数量"
              class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="价格" prop="price">
            <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" placeholder="请输入价格"
              class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="购买日期" prop="purchaseDate">
            <el-date-picker v-model="form.purchaseDate" type="date" placeholder="请选择购买日期" value-format="YYYY-MM-DD"
              class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="生产日期" prop="productionDate">
            <el-date-picker v-model="form.productionDate" type="date" placeholder="请选择生产日期" value-format="YYYY-MM-DD"
              class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="保修期" prop="warrantyPeriod">
            <el-input-number v-model="form.warrantyPeriod" :min="0" :max="120" placeholder="请输入保修期（月）" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="保修截止日期" prop="warrantyEndDate">
            <el-date-picker v-model="form.warrantyEndDate" type="date" placeholder="请选择保修截止日期" value-format="YYYY-MM-DD"
              class="w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="所属位置" prop="parentId">
        <el-tree :current-node-key="currentNodeKey" :data="computedTreeData"
          :props="{ label: 'name', children: 'children' }" node-key="id" highlight-current default-expand-all
          :expand-on-click-node="false" class="location-tree" @current-change="handleLocationSelect">
          <template #default="{ node, data }">
            <div class="custom-tree-node">
              <span>
                <el-icon v-if="data.type === 'space'">
                  <Folder />
                </el-icon>
                <el-icon v-else>
                  <Goods />
                </el-icon>
                {{ node.label }}
              </span>
            </div>
          </template>
        </el-tree>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入物品描述" />
      </el-form-item>

      <el-form-item label="标签" prop="tags">
        <el-select v-model="form.tags" multiple filterable default-first-option placeholder="请选择或输入标签"
          @change="handleTagsChange">
          <el-option v-for="tag in existingTags" :key="tag.id" :label="tag.name" :value="tag.id">
            <div class="flex items-center">
              <el-tag :color="tag.color" style="margin-right: 12px" />
              <span :style="{ color: tag.color }">{{ tag.name }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="图片" prop="images">
        <el-upload ref="uploadRef" :file-list="imageList" @update:file-list="imageList = $event" action="#"
          list-type="picture-card" multiple :auto-upload="false" :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove" :before-upload="beforeImageUpload" :on-change="handleImageChange">
          <el-icon>
            <Plus />
          </el-icon>
        </el-upload>
        <el-dialog v-model="dialogVisible" append-to-body>
          <img class="w-full" :src="dialogImageUrl" alt="Preview Image" />
        </el-dialog>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance } from "element-plus";
import { Folder, Goods, Plus, Close, Check } from "@element-plus/icons-vue";
import { useEntityForm } from "../composables/useEntityForm";

// 添加此注释，需要在项目中创建相应的类型定义
// @ts-ignore
import type { Entity, EntityStatus, Tag } from "@/types/entity";
import { useEntityImageUpload } from "../composables/useEntityImageUpload";
import { useUserStoreHook } from "@/store/modules/user"; // 引入用户store

interface Props {
  entity: Entity | null;
  treeData: Entity[];
  existingTags: Tag[];
  isEditing: boolean;
  saving: boolean;
}

// 获取用户store
const userStore = useUserStoreHook();

// 表单数据
const form = reactive<Omit<Entity, "tags"> & { tags: any[] }>({
  name: "",
  type: "item",
  parentId: "",
  status: "normal" as EntityStatus,
  location: "",
  quantity: 0,
  price: 0,
  purchaseDate: "",
  productionDate: "",
  warrantyPeriod: 0,
  description: "",
  tags: [], // 这里存储标签ID数组
  images: [],
  deletedImageIds: [],
  userId: "" // 添加userId字段
});

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: "submit", form: Entity): void;
  (e: "cancel"): void;
}>();

const formRef = ref<FormInstance>();

// 使用图片上传composable
const {
  dialogVisible,
  dialogImageUrl,
  imageList,
  deletedImageIds,
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

/**
 * 日期计算相关函数和监听器
 * 实现生产日期、保修期和保修截止日期之间的联动计算
 * 当用户填写其中任意两个值时，自动计算第三个值
 */

/**
 * 根据生产日期和保修期计算保修截止日期
 * 计算公式: 保修截止日期 = 生产日期 + 保修期(月)
 */
const calculateWarrantyEndDate = () => {
  if (form.productionDate && form.warrantyPeriod > 0) {
    const date = new Date(form.productionDate);
    date.setMonth(date.getMonth() + form.warrantyPeriod);
    form.warrantyEndDate = date.toISOString().split("T")[0];
  }
};

/**
 * 根据生产日期和保修截止日期计算保修期(月数)
 * 计算公式: 保修期 = (保修截止日期年份 - 生产日期年份) * 12 + (保修截止日期月份 - 生产日期月份)
 */
const calculateWarrantyPeriod = () => {
  if (form.productionDate && form.warrantyEndDate) {
    const startDate = new Date(form.productionDate);
    const endDate = new Date(form.warrantyEndDate);
    const diffMonths =
      (endDate.getFullYear() - startDate.getFullYear()) * 12 +
      (endDate.getMonth() - startDate.getMonth());
    form.warrantyPeriod = diffMonths > 0 ? diffMonths : 0;
  }
};

/**
 * 根据保修截止日期和保修期计算生产日期
 * 计算公式: 生产日期 = 保修截止日期 - 保修期(月)
 * 注意: 仅在未提供生产日期时才进行此计算
 */
const calculateProductionDate = () => {
  if (form.warrantyEndDate && form.warrantyPeriod > 0) {
    const endDate = new Date(form.warrantyEndDate);
    endDate.setMonth(endDate.getMonth() - form.warrantyPeriod);
    form.productionDate = endDate.toISOString().split("T")[0];
  }
};

/**
 * 监听生产日期和保修期的变化，计算保修截止日期
 * 当生产日期和保修期都有效时，自动计算保修截止日期
 */
watch(
  [() => form.productionDate, () => form.warrantyPeriod],
  ([newProductionDate, newWarrantyPeriod]) => {
    if (newProductionDate && newWarrantyPeriod > 0) {
      calculateWarrantyEndDate();
    }
  }
);

/**
 * 监听生产日期和保修截止日期的变化，计算保修期
 * 当生产日期和保修截止日期都有效时，自动计算保修期(月数)
 */
watch(
  [() => form.productionDate, () => form.warrantyEndDate],
  ([newProductionDate, newWarrantyEndDate]) => {
    if (
      newProductionDate &&
      newWarrantyEndDate &&
      !isNaN(new Date(newProductionDate).getTime()) &&
      !isNaN(new Date(newWarrantyEndDate).getTime())
    ) {
      calculateWarrantyPeriod();
    }
  }
);

/**
 * 监听保修期和保修截止日期的变化，计算生产日期
 * 当保修期和保修截止日期都有效且生产日期未填写时，自动计算可能的生产日期
 * 注意: 此计算仅在生产日期未提供时进行，避免覆盖用户已输入的生产日期
 */
watch(
  [() => form.warrantyPeriod, () => form.warrantyEndDate],
  ([newWarrantyPeriod, newWarrantyEndDate]) => {
    if (
      newWarrantyPeriod > 0 &&
      newWarrantyEndDate &&
      !form.productionDate &&
      !isNaN(new Date(newWarrantyEndDate).getTime())
    ) {
      calculateProductionDate();
    }
  }
);

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
    { min: 1, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" }
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

// 格式化标签数据为后端需要的格式
const formatTagsForSubmit = (tagIds: any[]) => {
  return useEntityForm().formatTagsForSubmit(tagIds, props.existingTags);
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
          quantity: form.quantity,
          price: form.price,
          purchaseDate: form.purchaseDate,
          productionDate: form.productionDate,
          warrantyPeriod: form.warrantyPeriod,
          warrantyEndDate: form.warrantyEndDate,
          description: form.description,
          tags: formatTagsForSubmit(form.tags), // 格式化标签数据
          images: [...imageList.value], // 使用展开运算符创建新数组
          deletedImageIds: [...deletedImageIds.value],// 使用展开运算符创建新数组
          userId: form.userId
        };

        console.log("提交的表单数据:", formData);
        // 添加标签信息的日志输出，帮助调试
        console.log("提交的标签数据:", formData.tags);
        emit("submit", formData);
      } catch (error) {
        console.error("提交表单失败:", error);
        ElMessage.error("提交表单失败，请重试");
      }
    }
  });
};

// 监听实体变化
watch(
  () => props.entity,
  newEntity => {
    if (newEntity) {
      // 确保表单数据类型正确
      form.name = newEntity.name || "";
      form.type = newEntity.type || "物品";
      form.parentId = newEntity.parentId || "";
      // @ts-ignore
      form.status =
        (newEntity.status as EntityStatus) || ("normal" as EntityStatus);
      form.location = (newEntity as any).location || "";
      form.quantity = newEntity.quantity || 0;
      form.price = newEntity.price || 0;
      form.purchaseDate = newEntity.purchaseDate || "";
      form.productionDate = newEntity.productionDate || "";
      form.warrantyPeriod = newEntity.warrantyPeriod || 0;
      form.warrantyEndDate = newEntity.warrantyEndDate || "";

      form.description = newEntity.description || "";

      // 确保正确设置标签数据 - 如果标签是对象数组则使用它们的ID
      if (Array.isArray(newEntity.tags)) {
        // 检查标签数据的格式
        if (
          newEntity.tags.length > 0 &&
          typeof newEntity.tags[0] === "object" &&
          newEntity.tags[0].id
        ) {
          // 如果是Tag对象数组，提取ID数组
          form.tags = newEntity.tags.map(tag => tag.id);
        } else {
          // 直接使用，可能是ID数组
          form.tags = [...newEntity.tags];
        }
      } else {
        form.tags = [];
      }
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
      form.type = "物品";
      form.parentId = "";
      // @ts-ignore
      form.status = "normal" as EntityStatus;
      form.location = "";
      form.quantity = 0;
      form.price = 0;
      form.purchaseDate = "";
      form.warrantyPeriod = 0;
      form.warrantyEndDate = "";
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

.container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100vw;
  max-width: 100%;
  grid-gap: 18rem;
  padding: 0 2rem;
}
</style>
