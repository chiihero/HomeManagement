import { http } from "@/utils/http";
import { useUserStoreHook } from "@/store/modules/user";
const userId = useUserStoreHook().userId;

/**
 * 获取实体的图片列表
 * @param entityId 实体ID
 * @param type 图片类型（可选）
 * @returns 图片列表
 */
export function getEntityImages(entityId: string, type?: string) {
  return http.get(`/entity-images/entity/${entityId}`, { params: { type } });
}

/**
 * 上传实体图片
 * @param entityId 实体ID
 * @param file 文件对象
 * @param imageType 图片类型，默认为normal
 * @returns 上传结果
 */
export function uploadEntityImage(
  entityId: string,
  file: File,
  imageType: string = "normal"
) {
  const formData = new FormData();
  formData.append("userId", userId);
  formData.append("image", file);
  formData.append("imageType", imageType);

  return http.post(`/entity-images/entity/${entityId}`, {data: formData}, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
}

/**
 * 删除实体图片
 * @param imageId 图片ID
 * @returns 删除结果
 */
export function deleteEntityImage(imageId: string) {
  return http.delete(`/entity-images/${imageId}`);
}
