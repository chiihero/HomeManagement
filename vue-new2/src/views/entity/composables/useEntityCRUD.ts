import { ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getEntityTree,
  getEntity,
  createEntity,
  updateEntity,
  deleteEntity
} from "@/api/entity";
import { getEntityImages } from "@/api/image";

import type { Entity, EntityFormData } from "@/types/entity";
import { useUserStoreHook } from "@/store/modules/user";
import { useEntityImageUpload } from "../composables/useEntityImageUpload";

export function useEntityCRUD() {
  // 状态
  const authStore = useUserStoreHook();

  const loading = ref(false);
  const saving = ref(false);
  const treeData = ref<Entity[]>([]);
  const currentEntity = ref<Entity | null>(null);
  const isEditing = ref(false);
  const isAdding = ref(false);
  // 使用图片上传composable
  const {
    uploadImages
  } = useEntityImageUpload();

  // 加载树形数据
  const loadTreeData = async () => {
    loading.value = true;
    try {
      const response = await getEntityTree(authStore.currentUser.id);
      if (response.data) {
        treeData.value = response.data;
      }
    } catch (error) {
      console.error("Failed to load entity tree:", error);
      ElMessage.error("加载物品结构失败");
    } finally {
      loading.value = false;
    }
  };

  // 处理节点点击
  const handleNodeClick = async (node: Entity) => {
    if (isEditing.value || isAdding.value) {
      const confirmed = await ElMessageBox.confirm(
        "当前有未保存的更改，是否继续？",
        "提示",
        {
          confirmButtonText: "继续",
          cancelButtonText: "取消",
          type: "warning"
        }
      );
      if (!confirmed) return;
    }

    isEditing.value = false;
    isAdding.value = false;

    // 加载完整的物品详情
    try {
      await loadEntityDetail(node.id);
    } catch (error) {
      console.error("Failed to load entity detail:", error);
      // 如果获取详情失败，至少显示基本信息
      currentEntity.value = node;
    }
  };

  // 打开添加表单
  const openAddEntityForm = () => {
    isAdding.value = true;
    isEditing.value = false;
    currentEntity.value = null;
  };

  // 打开编辑表单
  const openEditEntityForm = () => {
    isEditing.value = true;
    isAdding.value = false;
  };

  // 取消编辑或添加
  const cancelEditOrAdd = () => {
    isEditing.value = false;
    isAdding.value = false;
    if (currentEntity.value) {
      // 重新加载当前实体详情
      loadEntityDetail(currentEntity.value.id);
    }
  };

  // 加载实体详情
  const loadEntityDetail = async (id: string) => {
    try {
      const response = await getEntity(id);
      if (response.data) {
        currentEntity.value = response.data;
        // 如果实体中没有图片数据，单独加载图片
        if (!currentEntity.value.images || currentEntity.value.images.length === 0) {
          await loadEntityImages(id);
        }
      }
    } catch (error) {
      console.error("Failed to load entity detail:", error);
      ElMessage.error("加载物品详情失败");
    }
  };
  
  /**
   * 单独加载实体的图片列表
   * @param entityId 实体ID
   */
  const loadEntityImages = async (entityId: string) => {
    if (!entityId) return;

    loading.value = true;
    try {
      const response = await getEntityImages(entityId);

      if (response.data) {
        // 更新实体的图片列表
        if (currentEntity.value) {
          currentEntity.value.images = response.data;
        }
      }
    } catch (error) {
      console.error("加载实体图片错误:", error);
    } finally {
      loading.value = false;
    }
  };


  // 保存实体
  const saveEntity = async (formData: EntityFormData) => {
    saving.value = true;
    try {

      // 特殊处理根空间的情况
      if (formData.parentId === '0') {
        // @ts-ignore - 忽略类型检查，因为服务端需要接收null值
        formData.parentId = null; // 发送null表示没有父节点
      }
      // 从提交数据中移除images字段，避免发送大量无用数据到后端
      // delete formData.images;
      const response = isAdding.value
        ? await createEntity(formData)
        : await updateEntity(currentEntity.value?.id || "", formData);

      if (response.data) {
        // 将返回的数据转换为Entity类型
        const savedEntity = response.data as Entity;
        
        // 更新当前实体
        currentEntity.value = savedEntity;
        
        // 获取实体ID用于图片上传
        const entityId = savedEntity.id;
        
        // 上传图片（如果有）
        if (entityId) {
          try {
            const uploadedImageIds = await uploadImages(entityId);
            if (uploadedImageIds && uploadedImageIds.length > 0) {
              ElMessage.success(isAdding.value ? '添加物品及图片成功' : '更新物品及图片成功');
            }
          } catch (uploadError) {
            console.error('上传图片失败:', uploadError);
            ElMessage.warning(isAdding.value ? '物品已添加，但图片上传失败' : '物品已更新，但图片上传失败');
          }
        } else {
          ElMessage.success(isAdding.value ? "添加成功" : "更新成功");
        }
        
        // 操作完成后重置状态
        isEditing.value = false;
        isAdding.value = false;
        
        // 重新加载树形数据
        loadTreeData();
        
        // 如果是添加操作，重新加载实体详情
        if (entityId) {
          await loadEntityDetail(entityId);
        }
      }
    } catch (error) {
      console.error("Failed to save entity:", error);
      ElMessage.error(isAdding.value ? "添加失败" : "更新失败");
    } finally {
      saving.value = false;
    }
  };

  // 处理删除
  const handleDelete = async (entity: Entity) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除物品"${entity.name}"吗？此操作不可恢复。`,
        "警告",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      );

      const response = await deleteEntity(entity.id);
      if (response.data) {
        ElMessage.success("删除成功");
        currentEntity.value = null;
        loadTreeData();
      }
    } catch (error) {
      if (error !== "cancel") {
        console.error("Failed to delete entity:", error);
        ElMessage.error("删除失败");
      }
    }
  };

  
  return {
    loading,
    saving,
    treeData,
    currentEntity,
    isEditing,
    isAdding,
    loadTreeData,
    handleNodeClick,
    handleDelete,
    openAddEntityForm,
    openEditEntityForm,
    cancelEditOrAdd,
    saveEntity
  };
}
