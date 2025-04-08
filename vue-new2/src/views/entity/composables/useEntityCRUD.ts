import { ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getEntityTree,
  getEntity,
  createEntity,
  updateEntity,
  deleteEntity,
  getEntityImages
} from "@/api/entity";
import type { Entity, EntityFormData } from "@/types/entity";
import { useUserStore } from "@/store/modules/user";

export function useEntityCRUD() {
  // 状态
  const authStore = useUserStore();

  const loading = ref(false);
  const saving = ref(false);
  const treeData = ref<Entity[]>([]);
  const currentEntity = ref<Entity | null>(null);
  const isEditing = ref(false);
  const isAdding = ref(false);

  // 加载树形数据
  const loadTreeData = async () => {
    loading.value = true;
    try {
      const response = await getEntityTree(authStore.currentUser.id);
      if (response.data && response.code === 200) {
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
      if (response.data && response.code === 200) {
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
      const response = isAdding.value
        ? await createEntity(formData)
        : await updateEntity(currentEntity.value?.id || "", formData);

      if (response.data && response.code === 200) {
        ElMessage.success(isAdding.value ? "添加成功" : "更新成功");
        isEditing.value = false;
        isAdding.value = false;
        loadTreeData();
        if (response.data) {
          currentEntity.value = response.data;
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
      if (response.data && response.code === 200) {
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
