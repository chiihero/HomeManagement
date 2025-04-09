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
  const handleImageChange = (response: any, file: any) => {
    // 将文件对象保存到imageList中，等待表单提交时上传
    imageList.value.push(file);

    return file;
  };

  /**
   * 上传实体的所有图片
   * @param entityId 实体ID
   * @param images 图片列表，含有file属性的对象数组
   * @returns 是否全部上传成功
   */
  const uploadImages = async (entityId: string, images: any[]) => {
    if (!entityId || !images || images.length === 0) {
      return true; // 没有图片需要上传，视为成功
    }

    try {
      // 过滤出有文件对象的图片
      // const imagesToUpload = images.filter(
      //   img => img.raw && img.raw instanceof File
      // );

      // if (imagesToUpload.length === 0) {
      //   return true; // 没有新图片需要上传
      // }

      // 创建上传任务
      const uploadTasks = images.map(async (image, index) => {
        try {
          const imageType = image.imageType || "normal"; // 默认为普通图片
          const response = await uploadEntityImage(
            entityId,
            image.raw, // 使用raw属性而不是file属性
            imageType
          );

          if (response && response.data) {
            console.log(`图片 ${index + 1} 上传成功:`, response.data);
            return true;
          } else {
            console.error(`图片 ${index + 1} 上传失败:`, response);
            return false;
          }
        } catch (error) {
          console.error(`图片 ${index + 1} 上传出错:`, error);
          return false;
        }
      });

      // 等待所有上传任务完成
      const results = await Promise.all(uploadTasks);

      // 判断是否所有图片都上传成功
      const allSuccess = results.every(result => result === true);

      if (allSuccess) {
        ElMessage.success("所有图片上传成功");
        return true;
      } else {
        const successCount = results.filter(r => r === true).length;
        // ElMessage.warning(
        //   `部分图片上传失败，成功 ${successCount}/${imagesToUpload.length}`
        // );
        return false;
      }
    } catch (error) {
      console.error("上传实体图片时发生错误:", error);
      ElMessage.error("图片上传过程中出现错误");
      return false;
    }
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
