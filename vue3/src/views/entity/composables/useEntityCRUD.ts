import { ref } from "vue";
import { ElMessage, ElMessageBox, ElSelect } from "element-plus";
import {
  getEntityTree,
  getEntity,
  createEntity,
  updateEntity,
  deleteEntity,
  getEntitiesByUser,
  pageEntities
} from "@/api/entity";
// 注释掉未使用的导入
// import { getEntityImages } from "@/api/image";
import { getAllTags } from "@/api/tag"; // 从tag.ts导入getAllTags
import type { Entity, Tag } from "@/types/entity";
import { useUserStoreHook } from "@/store/modules/user";
import { useEntityImageUpload } from "../composables/useEntityImageUpload";

export interface EntityCRUDOptions {
  isSearchMode?: boolean;
  pagination?: {
    current: number;
    size: number;
    total: number;
  };
  initialSearchForm?: Record<string, any>;
}

export function useEntityCRUD(options: EntityCRUDOptions = {}) {
  // 状态
  const authStore = useUserStoreHook();
  const loading = ref(false);
  const treeLoading = ref(false);
  const saving = ref(false);
  const treeData = ref<Entity[]>([]);
  const currentEntity = ref<Entity | null>(null);
  const isEditing = ref(false);
  const isAdding = ref(false);
  const entityTags = ref<Tag[]>([]);
  // 添加详情加载状态
  const detailLoading = ref(false);
  
  // 搜索模式下的状态
  const { isSearchMode = false, pagination, initialSearchForm = {} } = options;
  const entityList = ref<Entity[]>([]);
  const searchForm = ref<Record<string, any>>({
    userId: authStore.userId || "",
    ...initialSearchForm
  });
  
  // 对话框状态
  const formDialogVisible = ref(false);
  const detailDialogVisible = ref(false);
  
  // 分页参数
  const paginationConfig = ref(pagination || {
    current: 1,
    size: 10,
    total: 0
  });

  // 使用图片上传composable
  const { uploadImages, deleteImages } = useEntityImageUpload();

  // 加载树形数据
  const loadTreeData = async () => {
    treeLoading.value = true;
    try {
      // 获取当前展开的节点ID，如果访问树组件ref的话
      // 保存当前选中的物品
      const currentId = currentEntity.value?.id;

      let response;
      
      // 根据模式决定使用哪个API获取数据
      if (isSearchMode) {
        response = await getEntitiesByUser(authStore.userId);
      } else {
        response = await getEntityTree(authStore.userId);
      }
      
      if (response.data) {
        // @ts-ignore - 忽略类型检查，应为响应类型定义问题
        treeData.value = response.data;

        // 如果当前有选中的物品，重新加载详情以保持选中状态
        if (currentId) {
          await loadEntityDetail(currentId);
        }
      }
    } catch (error) {
      console.error("Failed to load entity tree:", error);
      ElMessage.error("加载物品结构失败");
    } finally {
      treeLoading.value = false;
    }
  };

  // 加载所有标签数据
  const loadAllTags = async () => {
    try {
      // 从用户store获取userId
      const userId = authStore.userId;
      console.log("当前用户ID:", userId);

      if (!userId) {
        console.error("无法获取用户ID");
        return;
      }

      console.log("开始获取标签数据，userId:", userId);
      const response = await getAllTags(userId);
      console.log("标签API返回原始数据:", response);

      if (response && response.data && response.data.length > 0) {
        console.log("标签API返回数据:", response.data);
        entityTags.value = response.data || [];
        console.log("处理后的标签列表:", entityTags.value);
      }
    } catch (error) {
      console.error("加载标签失败:", error);
      ElMessage.error("加载标签数据失败");
    }
  };

  // 处理节点点击
  const handleNodeClick = async (node: Entity) => {
    // 如果点击的节点与当前选中的节点相同，则不执行任何操作
    if (currentEntity.value && currentEntity.value.id === node.id) {
      return;
    }

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
    if (isSearchMode) {
      formDialogVisible.value = true;
    }
  };

  // 打开编辑表单
  const openEditEntityForm = () => {
    isEditing.value = true;
    isAdding.value = false;
    if (isSearchMode) {
      formDialogVisible.value = true;
    }
  };

  // 取消编辑或添加
  const cancelEditOrAdd = () => {
    isEditing.value = false;
    isAdding.value = false;
    if (isSearchMode) {
      formDialogVisible.value = false;
    }
    if (currentEntity.value) {
      // 重新加载当前实体详情
      loadEntityDetail(currentEntity.value.id);
    }
  };

  // 加载实体详情
  const loadEntityDetail = async (id: string) => {
    // 设置详情页加载状态
    detailLoading.value = true;
    try {
      const response = await getEntity(id);
      if (response.data) {
        // @ts-ignore - 忽略类型检查，应为响应类型定义问题
        currentEntity.value = response.data;
      }
    } catch (error) {
      console.error("Failed to load entity detail:", error);
      ElMessage.error("加载物品详情失败");
    } finally {
      detailLoading.value = false;
    }
  };
  
  // 查看实体详情
  const handleViewDetail = async (entity: Entity) => {
    detailLoading.value = true;
    currentEntity.value = null;
    
    if (isSearchMode) {
      detailDialogVisible.value = true;
    }
    
    try {
      await loadEntityDetail(entity.id);
    } catch (error) {
      console.error("获取实体详情失败:", error);
      ElMessage.error("获取实体详情失败");
    } finally {
      detailLoading.value = false;
    }
  };
  
  // 编辑实体
  const handleEdit = async (entity: Entity) => {
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false;
    }
    
    saving.value = false;
    currentEntity.value = null;
    
    try {
      // 获取最新实体数据
      await loadEntityDetail(entity.id);
      
      // 获取树形数据和标签列表
      await Promise.all([loadTreeData(), loadAllTags()]);
      
      // 设置编辑状态
      isEditing.value = true;
      if (isSearchMode) {
        formDialogVisible.value = true;
      }
    } catch (error) {
      console.error("获取编辑数据失败:", error);
      ElMessage.error("获取编辑数据失败");
    }
  };

  // 保存实体
  const saveEntity = async (formData: Entity) => {
    saving.value = true;
    try {
      console.log("保存实体，表单数据:", formData);

      // 特殊处理根空间的情况
      if (formData.parentId === "0") {
        // @ts-ignore - 忽略类型检查，因为服务端需要接收null值
        formData.parentId = null; // 发送null表示没有父节点
      }

      // 临时保存图片数据，因为提交给后端时不需要这些数据
      const images = formData.images ? [...formData.images] : [];
      console.log("准备上传的图片:", images);

      // 从提交数据中移除images字段，避免发送大量无用数据到后端
      const entityData = { 
        id: currentEntity.value?.id,
        ...formData 
      };
      delete entityData.images;

      const response = isAdding.value
        ? await createEntity(entityData)
        : await updateEntity(entityData.id || "", entityData);

      if (response.data) {
        // 更新当前实体
        if(isAdding.value){
          currentEntity.value =  response.data
        }else{
          currentEntity.value =  formData
          currentEntity.value.id = entityData.id
        }

        // 获取实体ID用于图片上传
        console.log("实体保存成功，ID:", currentEntity.value.id);

        // 首先处理删除的图片
        let deleteSuccess = true;
        if (currentEntity.value.id && formData.deletedImageIds && formData.deletedImageIds.length > 0) {
          try {
            console.log("开始处理已删除的图片");
            const deleteResult = await deleteImages(formData.deletedImageIds);
            deleteSuccess = deleteResult.success;
            if (!deleteSuccess) {
              console.warn("部分图片删除失败:", deleteResult.message);
              ElMessage.warning(deleteResult.message || "部分图片删除失败");
            }
          } catch (deleteError) {
            console.error("删除图片出错:", deleteError);
            deleteSuccess = false;
          }
        }

        // 然后上传新图片（如果有）
        let uploadSuccess = true;
        if (currentEntity.value.id && images && images.length > 0) {
          try {
            console.log("开始上传图片，数量:", images.length);
            uploadSuccess = (await uploadImages(
              currentEntity.value.id,
              images
            )) as boolean;

            if (!uploadSuccess) {
              ElMessage.warning(
                isAdding.value
                  ? "物品已添加，但部分图片上传失败"
                  : "物品已更新，但部分图片上传失败"
              );
            }
          } catch (uploadError) {
            console.error("上传图片失败:", uploadError);
            uploadSuccess = false;
            ElMessage.warning(
              isAdding.value
                ? "物品已添加，但图片上传失败"
                : "物品已更新，但图片上传失败"
            );
          }
        }

        // 根据图片处理结果显示相应消息
        if (deleteSuccess && uploadSuccess) {
          ElMessage.success(isAdding.value ? "添加成功" : "更新成功");
        }

        // 重置表单状态
        isEditing.value = false;
        isAdding.value = false;
        
        if (isSearchMode) {
          formDialogVisible.value = false;
          // 如果在搜索模式，刷新列表
          loadEntityList();
        } else {
          // 如果在树形模式，刷新树形数据
          loadTreeData();
        }
      }
    } catch (error) {
      console.error("Failed to save entity:", error);
      ElMessage.error("保存失败，请检查网络连接");
    } finally {
      saving.value = false;
    }
  };

  // 处理删除
  const handleDelete = async (entity: Entity) => {
    try {
      const confirmed = await ElMessageBox.confirm(
        `确定要删除 ${entity.name} 吗？`,
        "警告",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      );

      if (!confirmed) return;

      loading.value = true;
      const response = await deleteEntity(entity.id);

      if (response) {
        ElMessage.success("删除成功");
        
        if (currentEntity.value && currentEntity.value.id === entity.id) {
          currentEntity.value = null;
        }
        
        if (isSearchMode) {
          // 在搜索模式下刷新列表
          loadEntityList();
        } else {
          // 在树形模式下刷新树形数据
          loadTreeData();
        }
      }
    } catch (error) {
      console.error("Failed to delete entity:", error);
      ElMessage.error("删除失败，请稍后重试");
    } finally {
      loading.value = false;
    }
  };
  
  // 加载实体列表（搜索模式）
  const loadEntityList = async () => {
    if (!authStore.userId) return;

    loading.value = true;
    try {
      const response = await pageEntities({
        current: paginationConfig.value.current,
        size: paginationConfig.value.size,
        ...searchForm.value
      });

      // @ts-ignore - 类型断言，忽略data属性不存在的错误
      if (response.data) {
        // 适配不同的API返回结构
        // @ts-ignore - 类型断言，忽略data属性不存在的错误
        entityList.value = response.data.records || response.data.list || [];
        // @ts-ignore - 类型断言，忽略data属性不存在的错误
        paginationConfig.value.total = response.data.total || 0;
      }
    } catch (error) {
      console.error("加载实体列表失败:", error);
      ElMessage.error("加载实体列表失败，请检查网络连接");
    } finally {
      loading.value = false;
    }
  };
  
  // 搜索
  const handleSearch = () => {
    if (isSearchMode) {
      paginationConfig.value.current = 1;
      loadEntityList();
    }
  };
  
  // 重置搜索
  const resetSearch = () => {
    if (isSearchMode) {
      Object.keys(searchForm.value).forEach(key => {
        if (key !== "userId") {
          searchForm.value[key] = "";
        }
      });
      paginationConfig.value.current = 1;
      loadEntityList();
    }
  };
  
  // 处理分页大小改变
  const handleSizeChange = (size: number) => {
    if (isSearchMode) {
      paginationConfig.value.size = size;
      loadEntityList();
    }
  };

  // 处理当前页改变
  const handleCurrentChange = (current: number) => {
    if (isSearchMode) {
      paginationConfig.value.current = current;
      loadEntityList();
    }
  };
  
  // 关闭表单对话框前确认
  const handleFormDialogClose = (done: () => void) => {
    if (saving.value) {
      return;
    }
    
    ElMessageBox.confirm("确定要关闭吗？未保存的数据将会丢失", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    })
      .then(() => {
        isEditing.value = false;
        isAdding.value = false;
        done();
      })
      .catch(() => {});
  };

  return {
    // 状态
    loading,
    treeLoading,
    saving,
    treeData,
    currentEntity,
    isEditing,
    isAdding,
    entityTags,
    detailLoading,
    // 搜索相关
    entityList,
    searchForm,
    paginationConfig,
    // 对话框状态
    formDialogVisible,
    detailDialogVisible,
    // 方法
    loadTreeData,
    loadAllTags,
    handleNodeClick,
    openAddEntityForm,
    openEditEntityForm,
    cancelEditOrAdd,
    loadEntityDetail,
    saveEntity,
    handleDelete,
    handleViewDetail,
    handleEdit,
    loadEntityList,
    handleSearch,
    resetSearch,
    handleSizeChange,
    handleCurrentChange,
    handleFormDialogClose
  };
}
