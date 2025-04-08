import { ref } from "vue";
import { ElMessage } from "element-plus";
import type { UploadProps, UploadFile } from "element-plus";
import { uploadEntityImage } from "@/api/image";

export function useEntityImageUpload() {
  const dialogVisible = ref(false);
  const dialogImageUrl = ref("");
  const imageList = ref<UploadFile[]>([]);

  // 处理图片预览
  const handlePictureCardPreview: UploadProps["onPreview"] = file => {
    dialogImageUrl.value = file.url!;
    dialogVisible.value = true;
  };

  // 处理文件移除
  const handleRemove: UploadProps["onRemove"] = file => {
    const index = imageList.value.findIndex(item => item.uid === file.uid);
    if (index !== -1) {
      imageList.value.splice(index, 1);
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

  // 处理图片上传成功
  const handleImageChange = (response: any, file: any, formData?: any) => {
    // 将文件对象保存到imageList中，等待表单提交时上传
    imageList.value.push(file);
    
    // 如果提供了表单数据对象，将图片信息更新到表单中
    if (formData && typeof formData === 'object') {
      // 如果表单中已有images字段且为数组，则添加到现有数组
      if (Array.isArray(formData.images)) {
        formData.images.push(file);
      } else {
        // 否则创建新的images数组
        formData.images = [...imageList.value];
      }
    }
    
    return file;
  };

  // 上传图片到服务器
  const uploadImages = async (entityId: string) => {
    const uploadedImageIds: string[] = [];
    
    for (const file of imageList.value) {
      if (file.raw) {
        try {
          const uploadResult = await uploadEntityImage(entityId, file.raw);
          if (uploadResult && uploadResult.data && uploadResult.data.id) {
            uploadedImageIds.push(uploadResult.data.id);
          }
        } catch (error) {
          console.error("上传图片失败:", error);
          ElMessage.error(`上传图片 ${file.name} 失败`);
        }
      }
    }
    
    return uploadedImageIds;
  };

  // 重置图片列表
  const resetImageList = () => {
    imageList.value = [];
  };

  // 设置图片列表
  const setImageList = (images: any[]) => {
    imageList.value = images.map((url: string) => ({
      name: typeof url === "string" ? url.split("/").pop() || "" : "",
      url,
      uid: Date.now() + Math.random(),
      status: "success"
    }));
  };

  return {
    dialogVisible,
    dialogImageUrl,
    imageList,
    handlePictureCardPreview,
    handleRemove,
    beforeImageUpload,
    handleImageChange,
    uploadImages,
    resetImageList,
    setImageList
  };
} 