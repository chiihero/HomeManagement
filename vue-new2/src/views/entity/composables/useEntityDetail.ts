import { ref, Ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { getEntityDetail } from '@/api/entity';
import { getEntityImages } from '@/api/entity';
import { Entity } from '@/types/entity';

/**
 * 实体详情相关处理函数
 */
export function useEntityDetail() {
  const authStore = useAuthStore();
  
  const loading = ref(false);
  const entity = ref<Entity | null>(null);
  const imageLoading = ref(false);
  
  /**
   * 加载实体详情，包括基本信息和图片
   * @param id 实体ID
   */
  const loadEntityDetail = async (id: number | string) => {
    if (!id) return;
    
    loading.value = true;
    try {
      // 加载实体基本信息
      const response = await getEntityDetail(id.toString());
      
      if (response.data) {
        entity.value = response.data;
        
        // 如果实体中没有图片数据，单独加载图片
        if (!entity.value.images || entity.value.images.length === 0) {
          await loadEntityImages(id);
        }
      } else {
        ElMessage.error('获取实体详情失败');
      }
    } catch (error) {
      console.error('加载实体详情错误:', error);
      ElMessage.error('加载实体详情出错');
    } finally {
      loading.value = false;
    }
  };
  
  /**
   * 单独加载实体的图片列表
   * @param entityId 实体ID 
   */
  const loadEntityImages = async (entityId: number | string) => {
    if (!entityId || !entity.value) return;
    
    imageLoading.value = true;
    try {
      const response = await getEntityImages(Number(entityId));
      
      if (response.data) {
        // 更新实体的图片列表
        entity.value.images = response.data;
      }
    } catch (error) {
      console.error('加载实体图片错误:', error);
    } finally {
      imageLoading.value = false;
    }
  };
  
  /**
   * 刷新实体详情
   */
  const refreshEntityDetail = async () => {
    if (entity.value && entity.value.id) {
      await loadEntityDetail(entity.value.id);
    }
  };
  
  return {
    loading,
    imageLoading,
    entity,
    loadEntityDetail,
    loadEntityImages,
    refreshEntityDetail
  };
} 