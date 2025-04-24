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
              <el-option value="包装" />

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

      <!-- 药品特有字段 -->
      <div v-if="form.type === '药品'">
        <h3 class="text-base font-medium text-gray-900 pb-2 border-b border-gray-200 w-full mt-4 mb-4">
          药品特有信息
        </h3>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="有效成分">
              <el-input v-model="form.activeIngredient" placeholder="请输入有效成分" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="剂型">
              <el-select v-model="form.dosageForm" filterable allow-create placeholder="请选择或输入剂型">
                <el-option value="片剂" />
                <el-option value="胶囊" />
                <el-option value="口服液" />
                <el-option value="注射剂" />
                <el-option value="粉剂" />
                <el-option value="颗粒剂" />
                <el-option value="滴剂" />
                <el-option value="贴剂" />
                <el-option value="软膏" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="批号">
              <el-input v-model="form.batchNumber" placeholder="请输入批号" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="用法用量">
              <el-input v-model="form.usageDosage" placeholder="请输入用法用量" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="批准文号">
              <el-input v-model="form.approvalNumber" placeholder="请输入批准文号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="说明书内容" prop="instructionText">
          <el-input v-model="form.instructionText" type="textarea" :rows="6" placeholder="请输入药品说明书内容" />
        </el-form-item>
        
        <el-form-item label="说明书图片" prop="instructionImages">
          <el-upload 
            ref="instructionUploadRef" 
            :file-list="instructionImageList" 
            @update:file-list="instructionImageList = $event" 
            action="#" 
            list-type="picture-card" 
            multiple 
            :auto-upload="false" 
            :on-preview="handleInstructionPreview" 
            :on-remove="handleInstructionRemove" 
            :before-upload="beforeImageUpload" 
            :on-change="handleInstructionChange">
            <el-icon>
              <Plus />
            </el-icon>
          </el-upload>
          <el-dialog v-model="instructionDialogVisible" append-to-body>
            <img class="w-full" :src="instructionDialogImageUrl" alt="Preview Image" />
          </el-dialog>
        </el-form-item>
      </div>

      <!-- 耗材特有字段 -->
      <div v-if="form.type === '耗材'">
        <h3 class="text-base font-medium text-gray-900 pb-2 border-b border-gray-200 w-full mt-4 mb-4">
          耗材特有信息
        </h3>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="消耗速率">
              <el-input v-model="form.consumptionRate" placeholder="如：每月1包" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="剩余数量">
              <el-input-number v-model="form.remainingQuantity" :min="0" :precision="2" :step="0.1" placeholder="请输入剩余数量" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="单位">
              <el-select v-model="form.unit" filterable allow-create placeholder="请选择或输入单位">
                <el-option value="包" />
                <el-option value="卷" />
                <el-option value="瓶" />
                <el-option value="个" />
                <el-option value="片" />
                <el-option value="支" />
                <el-option value="盒" />
                <el-option value="箱" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="更换周期(天)">
              <el-input-number v-model="form.replacementCycle" :min="0" placeholder="请输入更换周期" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="上次更换日期">
              <el-date-picker v-model="form.lastReplacementDate" type="date" placeholder="请选择上次更换日期" value-format="YYYY-MM-DD" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

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

// 扩展Entity类型，创建表单数据类型
type EntityFormData = Entity & {
  tags: any[];
  // 以下字段已经在Entity接口中定义，此处无需重复
  // activeIngredient?: string;
  // dosageForm?: string;
  // batchNumber?: string;
  // usageDosage?: string;
  // approvalNumber?: string;
  // instructionText?: string;
  // instructionImages?: string;
  // consumptionRate?: string;
  // remainingQuantity?: number;
  // unit?: string;
  // replacementCycle?: number;
  // lastReplacementDate?: string;
};

// 表单数据
const form = reactive<EntityFormData>({
  name: "",
  type: "物品",
  parentId: "",
  status: "normal" as EntityStatus,
  location: "",
  quantity: 1,
  price: 0,
  purchaseDate: "",
  productionDate: "",
  warrantyPeriod: 0,
  description: "",
  tags: [], // 这里存储标签ID数组
  images: [],
  deletedImageIds: [],
  userId: "", // 添加userId字段
  
  // 药品特有字段
  activeIngredient: "",
  dosageForm: "",
  batchNumber: "",
  usageDosage: "",
  approvalNumber: "",
  instructionText: "",
  instructionImages: "",
  
  // 耗材特有字段
  consumptionRate: "",
  remainingQuantity: 0,
  unit: "",
  replacementCycle: 0,
  lastReplacementDate: "",
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
          id: props.entity?.id, // 确保在编辑模式下包含ID
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

        // 根据实体类型添加特有字段
        if (form.type === "药品") {
          // 添加药品特有字段
          Object.assign(formData, {
            activeIngredient: form.activeIngredient,
            dosageForm: form.dosageForm,
            batchNumber: form.batchNumber,
            usageDosage: form.usageDosage,
            approvalNumber: form.approvalNumber,
            instructionText: form.instructionText,
            instructionImages: form.instructionImages
          });

          // 处理说明书图片上传，处理成字符串格式的URL列表
          if (instructionImageList.value && instructionImageList.value.length > 0) {
            const urls = instructionImageList.value
              .filter(img => img.url || img.fileUrl)
              .map(img => img.url || img.fileUrl)
              .join(',');
            formData.instructionImages = urls;
          }
        } else if (form.type === "耗材") {
          // 添加耗材特有字段
          Object.assign(formData, {
            consumptionRate: form.consumptionRate,
            remainingQuantity: form.remainingQuantity,
            unit: form.unit,
            replacementCycle: form.replacementCycle,
            lastReplacementDate: form.lastReplacementDate
          });
        }

        console.log("提交的表单数据:", formData);
        // 添加标签信息的日志输出，帮助调试
        console.log("提交的标签数据:", formData.tags);
        
        // 使用类型断言将formData转换为Entity类型以满足emit要求
        emit("submit", formData as unknown as Entity);
      } catch (error) {
        console.error("提交表单失败:", error);
        ElMessage.error("提交表单失败，请重试");
      }
    }
  });
};

// 说明书图片相关
const instructionDialogVisible = ref(false);
const instructionDialogImageUrl = ref('');
const instructionImageList = ref<any[]>([]);

// 处理说明书图片预览
const handleInstructionPreview = (file: any) => {
  instructionDialogImageUrl.value = file.url;
  instructionDialogVisible.value = true;
};

// 处理删除说明书图片
const handleInstructionRemove = (file: any, fileList: any[]) => {
  // 如果有ID，添加到待删除列表
  if (file.id) {
    deletedImageIds.value.push(file.id);
  }
};

// 处理说明书图片变化
const handleInstructionChange = (file: any, fileList: any[]) => {
  instructionImageList.value = fileList;
};

// 设置说明书图片列表
const setInstructionImageList = (images: any[]) => {
  instructionImageList.value = images.map(img => ({
    ...img,
    name: img.fileName || '说明书图片',
    url: img.url || img.imageUrl
  }));
};

// 重置说明书图片列表
const resetInstructionImageList = () => {
  instructionImageList.value = [];
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
      form.quantity = newEntity.quantity || 1;
      form.price = newEntity.price || 0;
      form.purchaseDate = newEntity.purchaseDate || "";
      form.productionDate = newEntity.productionDate || "";
      form.warrantyPeriod = newEntity.warrantyPeriod || 0;
      form.warrantyEndDate = newEntity.warrantyEndDate || "";

      form.description = newEntity.description || "";
      
      // 设置当前选中的节点为传入的parentId
      if (newEntity.parentId) {
        currentNodeKey.value = newEntity.parentId;
        // 更新选中位置的名称
        updateSelectedLocationName();
      }

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

      // 设置药品特有字段
      if (newEntity.type === '药品' && newEntity.activeIngredient !== undefined) {
        form.activeIngredient = newEntity.activeIngredient || '';
        form.dosageForm = newEntity.dosageForm || '';
        form.batchNumber = newEntity.batchNumber || '';
        form.usageDosage = newEntity.usageDosage || '';
        form.approvalNumber = newEntity.approvalNumber || '';
        form.instructionText = newEntity.instructionText || '';
        form.instructionImages = newEntity.instructionImages || '';
        
        // 如果有说明书图片，设置图片列表
        if (newEntity.instructionImagesList) {
          setInstructionImageList(newEntity.instructionImagesList);
        } else {
          resetInstructionImageList();
        }
      }
      
      // 设置耗材特有字段
      if (newEntity.type === '耗材' && newEntity.consumptionRate !== undefined) {
        form.consumptionRate = newEntity.consumptionRate || '';
        form.remainingQuantity = newEntity.remainingQuantity || 0;
        form.unit = newEntity.unit || '';
        form.replacementCycle = newEntity.replacementCycle || 0;
        form.lastReplacementDate = newEntity.lastReplacementDate || '';
      }
    } else {
      // 重置表单
      form.name = "";
      form.type = "物品";
      form.parentId = "";
      // @ts-ignore
      form.status = "normal" as EntityStatus;
      form.location = "";
      form.quantity = 1;
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
      
      // 重置药品特有字段
      form.activeIngredient = '';
      form.dosageForm = '';
      form.batchNumber = '';
      form.usageDosage = '';
      form.approvalNumber = '';
      form.instructionText = '';
      form.instructionImages = '';
      resetInstructionImageList();
      
      // 重置耗材特有字段
      form.consumptionRate = '';
      form.remainingQuantity = 0;
      form.unit = '';
      form.replacementCycle = 0;
      form.lastReplacementDate = '';
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
