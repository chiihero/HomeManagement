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
          <el-form-item label="父级物品" prop="parentId">
            <el-tree-select
              v-model="form.parentId"
              :data="treeData"
              :props="{ label: 'name', value: 'id' }"
              placeholder="请选择父级物品"
              clearable
              check-strictly
              class="w-full"
            />
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
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="位置" prop="location">
            <el-input v-model="form.location" placeholder="请输入物品位置" />
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
          class="w-full"
        >
          <el-option
            v-for="tag in existingTags"
            :key="tag"
            :label="tag"
            :value="tag"
          />
        </el-select>
      </el-form-item>

      <el-divider content-position="left">图片与附件</el-divider>

      <el-form-item label="图片" prop="images">
        <el-upload
          v-model:file-list="imageList"
          action="/api/entity-images/entity"
          list-type="picture-card"
          :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove"
          :before-upload="beforeImageUpload"
          :on-success="handleImageUploadSuccess"
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
import { ref, watch } from "vue";
import { Plus, Upload } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import type { FormInstance, UploadProps, UploadFile } from "element-plus";
import type { Entity, EntityFormData, EntityStatus } from "@/types/entity";
import { getImageData } from "@/api/entity";

interface Props {
  entity: Entity | null;
  treeData: Entity[];
  existingTags: string[];
  isEditing: boolean;
  saving: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: "submit", form: EntityFormData): void;
  (e: "cancel"): void;
}>();

const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const dialogImageUrl = ref("");
const imageList = ref<UploadFile[]>([]);
const attachmentList = ref<UploadFile[]>([]);

// 表单数据
const form = ref<EntityFormData>({
  name: "",
  type: "item",
  parentId: "",
  status: "AVAILABLE" as EntityStatus,
  location: "",
  price: 0,
  purchaseDate: "",
  warrantyPeriod: 0,
  description: "",
  tags: [],
  images: [],
  attachments: []
});

// 表单验证规则
const rules = {
  name: [
    { required: true, message: "请输入物品名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" }
  ],
  type: [{ required: true, message: "请输入物品类型", trigger: "blur" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
  price: [{ required: true, message: "请输入价格", trigger: "blur" }],

};

// 处理图片预览
const handlePictureCardPreview: UploadProps["onPreview"] = file => {
  dialogImageUrl.value = file.url!;
  dialogVisible.value = true;
};

// 处理文件预览
const handlePreview: UploadProps["onPreview"] = file => {
  window.open(file.url);
};

// 处理文件移除
const handleRemove: UploadProps["onRemove"] = file => {
  if (file.raw?.type.startsWith("image/")) {
    const index = imageList.value.findIndex(item => item.uid === file.uid);
    if (index !== -1) {
      imageList.value.splice(index, 1);
    }
  } else {
    const index = attachmentList.value.findIndex(item => item.uid === file.uid);
    if (index !== -1) {
      attachmentList.value.splice(index, 1);
    }
  }
};

// 图片上传前检查
const beforeImageUpload: UploadProps["beforeUpload"] = file => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB!");
    return false;
  }
  return true;
};

// 附件上传前检查
const beforeAttachmentUpload: UploadProps["beforeUpload"] = file => {
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isLt10M) {
    ElMessage.error("文件大小不能超过 10MB!");
    return false;
  }
  return true;
};

// 处理图片上传成功
const handleImageUploadSuccess = async (response: any, file: any) => {
  if (response.code === 200 && response.data) {
    // 保存图片ID到表单数据
    if (!form.value.images) {
      form.value.images = [];
    }
    form.value.images.push(response.data.id);
    
    // 获取图片数据并创建URL
    try {
      const blob = await getImageData(response.data.id);
      const url = URL.createObjectURL(blob);
      file.url = url;
    } catch (error) {
      console.error("获取图片数据失败:", error);
      ElMessage.error("获取图片数据失败");
    }
  } else {
    ElMessage.error("图片上传失败");
  }
};

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(valid => {
    if (valid) {
      // 更新图片和附件列表
      form.value.images = imageList.value.map(file => file.url!);
      form.value.attachments = attachmentList.value.map(file => ({
        name: file.name,
        url: file.url!
      }));

      emit("submit", form.value);
    }
  });
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
      form.value = {
        name: newEntity.name || "",
        type: newEntity.type || "item",
        parentId: newEntity.parentId || "",
        status:
          (newEntity.status as unknown as EntityStatus) ||
          ("AVAILABLE" as EntityStatus),
        location: (newEntity as any).location || "",
        price: newEntity.price || 0,
        purchaseDate: newEntity.purchaseDate || "",
        warrantyPeriod: newEntity.warrantyPeriod || 0,
        description: newEntity.description || "",
        tags: ((newEntity.tags || []) as any) || [],
        images: ((newEntity.images || []) as any) || [],
        attachments: (newEntity as any).attachments || []
      };

      // 更新图片和附件列表
      imageList.value =
        (newEntity.images as any)?.map((url: string) => ({
          name: typeof url === "string" ? url.split("/").pop() || "" : "",
          url,
          uid: Date.now() + Math.random(),
          status: "success"
        })) || [];

      attachmentList.value = ((newEntity as any).attachments || []).map(
        (attachment: any) => ({
          name: attachment.name,
          url: attachment.url,
          uid: Date.now() + Math.random(),
          status: "success"
        })
      );
    } else {
      // 重置表单
      form.value = {
        name: "",
        type: "item",
        parentId: "",
        status: "AVAILABLE" as EntityStatus,
        location: "",
        price: 0,
        purchaseDate: "",
        warrantyPeriod: 0,
        description: "",
        tags: [],
        images: [],
        attachments: []
      };
      imageList.value = [];
      attachmentList.value = [];
    }
  },
  { immediate: true }
);
</script>
