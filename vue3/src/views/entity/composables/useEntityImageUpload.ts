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
    console.log("图片变更:", file);
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
  const uploadImages = async (entityId: string, images: UploadFile[]) => {
    if (!entityId || !images || images.length === 0) {
      console.log("没有图片需要上传");
      return { success: true };
    }

    try {
      console.log("开始上传图片，实体ID:", entityId);
      console.log("原始图片数据:", images);

      // 递归处理嵌套的数组
      const flattenImages = (arr: any[]): any[] => {
        return arr.reduce((flat: any[], item: any) => {
          if (Array.isArray(item)) {
            return flat.concat(flattenImages(item));
          }
          return flat.concat(item);
        }, []);
      };

      // 展平图片数组
      const flattenedImages = flattenImages(images);
      console.log("展平后的图片数组:", flattenedImages);

      // 过滤出需要上传的图片（只包含raw属性的图片）
      const imagesToUpload = flattenedImages.filter((image: any) => {
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
          const imageType = image.imageType || 'normal'; // 默认为普通图片
          const response = await uploadEntityImage(entityId, image.raw, imageType);
          
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
        ElMessage.success('所有图片上传成功');
        return true;
      } else {
        const successCount = results.filter(r => r === true).length;
        ElMessage.warning(`部分图片上传失败，成功 ${successCount}/${imagesToUpload.length}`);
        return false;
      }
    } catch (error: any) {
      console.error("上传图片失败:", error);
      ElMessage.error(error.response?.data?.message || "上传图片失败");
      return { success: false, error };
    }
  };

  // 重置图片列表
  const resetImageList = () => {
    imageList.value = [];
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
      if (typeof image === 'string') {
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
          uid: `${image.id}-${Date.now()}`,  // 确保UID唯一
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
    handlePictureCardPreview,
    handleRemove,
    beforeImageUpload,
    handleImageChange,
    uploadImages,
    resetImageList,
    setImageList
  };
}
