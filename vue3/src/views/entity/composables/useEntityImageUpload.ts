import { ref } from "vue";
import { ElMessage } from "element-plus";
import type { UploadProps, UploadFile } from "element-plus";
import { uploadEntityImage, deleteEntityImage } from "@/api/image";

// 扩展UploadFile类型，添加可能存在的id属性
interface ExtendedUploadFile extends UploadFile {
  id?: string;
  // 扩展response类型
  response?: {
    id?: string;
    [key: string]: any;
  };
  // 添加自定义图片类型字段
  imageType?: string;
  // 添加原始数据属性
  raw?: File;
}

export function useEntityImageUpload() {
  const dialogVisible = ref(false);
  const dialogImageUrl = ref("");
  const imageList = ref<ExtendedUploadFile[]>([]);
  const deletedImageIds = ref<string[]>([]); // 存储已删除的图片ID

  // 处理图片预览
  const handlePictureCardPreview: UploadProps["onPreview"] = file => {
    dialogImageUrl.value = file.url!;
    dialogVisible.value = true;
  };

  // 处理文件移除
  const handleRemove: UploadProps["onRemove"] = file => {
    const index = imageList.value.findIndex(item => item.uid === file.uid);
    if (index !== -1) {
      // 如果图片有ID（已保存到后端的图片），将其添加到待删除列表
      const extendedFile = file as ExtendedUploadFile;
      if (
        extendedFile.id ||
        (extendedFile.response && extendedFile.response.id)
      ) {
        const imageId = extendedFile.id || extendedFile.response?.id;
        if (imageId) {
          console.log("添加图片ID到待删除列表:", imageId);
          deletedImageIds.value.push(imageId);
        }
      }
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
    console.log("图片变更:", file);
    return file;
  };

  /**
   * 上传实体的所有图片
   * @param entityId 实体ID
   * @param images 图片列表，含有file属性的对象数组
   * @returns 是否全部上传成功
   */
  const uploadImages = async (
    entityId: string,
    images: ExtendedUploadFile[]
  ) => {
    if (!entityId || !images || images.length === 0) {
      console.log("没有图片需要上传");
      return { success: true };
    }

    try {
      console.log("开始上传图片，实体ID:", entityId);
      console.log("原始图片数据:", images);

      // 过滤出需要上传的图片（只包含raw属性的图片）
      const imagesToUpload = images.filter((image: ExtendedUploadFile) => {
        const hasRaw = image && image.raw instanceof File;
        console.log("检查图片:", image, "是否有raw属性:", hasRaw);
        return hasRaw;
      });

      console.log("需要上传的图片:", imagesToUpload);

      if (imagesToUpload.length === 0) {
        console.log("没有新图片需要上传");
        return { success: true };
      }

      // 创建上传任务
      const uploadTasks = imagesToUpload.map(async (image, index) => {
        try {
          const imageType = image.imageType || "normal"; // 默认为普通图片
          const response = await uploadEntityImage(
            entityId,
            image.raw as File,
            imageType
          );

          // 使用类型断言处理response.data
          const responseData = response as any;

          if (responseData && responseData.data) {
            console.log(`图片 ${index + 1} 上传成功:`, responseData.data);
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
        ElMessage.warning(
          `部分图片上传失败，成功 ${successCount}/${imagesToUpload.length}`
        );
        return false;
      }
    } catch (error: any) {
      console.error("上传图片失败:", error);
      ElMessage.error(error.response?.data?.message || "上传图片失败");
      return { success: false, error };
    }
  };

  /**
   * 删除实体的图片
   * @returns 是否全部删除成功
   */
  const deleteImages = async (deletedIds: string[]) => {
    if (deletedIds.length === 0) {
      console.log("没有图片需要删除");
      return { success: true };
    }

    try {
      console.log("开始删除图片，数量:", deletedIds.length);

      // 创建删除任务
      const deleteTasks = deletedIds.map(async (imageId, index) => {
        try {
          console.log(`删除图片 ${index + 1}, ID:`, imageId);
          const response = await deleteEntityImage(imageId);

          // 使用类型断言处理response
          const responseData = response as any;

          if (responseData && responseData.code === 200) {
            console.log(`图片 ${index + 1} 删除成功`);
            return true;
          } else {
            console.error(`图片 ${index + 1} 删除失败:`, response);
            return false;
          }
        } catch (error) {
          console.error(`图片 ${index + 1} 删除出错:`, error);
          return false;
        }
      });

      // 等待所有删除任务完成
      const results = await Promise.all(deleteTasks);

      // 判断是否所有图片都删除成功
      const allSuccess = results.every(result => result === true);

      // 清空已删除图片ID列表
      deletedImageIds.value = [];
      deletedIds = [];

      if (allSuccess) {
        console.log("所有图片删除成功");
        return { success: true };
      } else {
        const successCount = results.filter(r => r === true).length;
        console.warn(
          `部分图片删除失败，成功 ${successCount}/${deleteTasks.length}`
        );
        return {
          success: false,
          message: `部分图片删除失败，成功 ${successCount}/${deleteTasks.length}`
        };
      }
    } catch (error: any) {
      console.error("删除图片失败:", error);
      return { success: false, error };
    }
  };

  // 重置图片列表
  const resetImageList = () => {
    imageList.value = [];
    deletedImageIds.value = []; // 同时重置删除列表
  };

  // 设置图片列表
  const setImageList = (images: any[]) => {
    if (!images || images.length === 0) {
      imageList.value = [];
      return;
    }

    console.log("设置图片列表:", images);

    // 处理不同类型的图片数据
    imageList.value = images.map((image: any) => {
      // 如果已经是UploadFile类型，直接返回
      if (image.uid && image.url) {
        return image;
      }

      // 如果是字符串URL
      if (typeof image === "string") {
        return {
          name: image.split("/").pop() || "",
          url: image,
          uid: Date.now() + Math.random(),
          status: "success"
        };
      }

      // 如果是EntityImage对象 - 使用后端直接引用图片
      if (image.id) {
        // 构建API URL
        console.log("添加图片，ID:", image.id, "URL:", image.imageUrl);

        return {
          name: image.fileName || `图片${image.id}`,
          url: image.imageUrl,
          uid: `${image.id}-${Date.now()}`, // 确保UID唯一
          status: "success",
          id: image.id,
          response: { id: image.id }
        };
      }

      // 默认情况
      return {
        name: "未知图片",
        url: "",
        uid: Date.now() + Math.random(),
        status: "error"
      };
    });

    console.log("处理后的图片列表:", imageList.value);
  };

  return {
    dialogVisible,
    dialogImageUrl,
    imageList,
    deletedImageIds,
    handlePictureCardPreview,
    handleRemove,
    beforeImageUpload,
    handleImageChange,
    uploadImages,
    deleteImages,
    resetImageList,
    setImageList
  };
}
