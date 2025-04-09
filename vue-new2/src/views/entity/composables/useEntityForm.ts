import { ref, reactive } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { Entity, EntityFormData, EntityStatus } from "@/types/entity";
import { useUserStoreHook } from "@/store/modules/user";
import { getEntitiesByUser } from "@/api/entity";

export function useEntityForm() {
  // 位置选项
  const locationOptions = ref<any[]>([]);

  const authStore = useUserStoreHook();


  // 表单验证规则
  const rules = reactive<FormRules>({
    name: [
      { required: true, message: "请输入物品名称", trigger: "blur" },
      { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" }
    ],
    type: [{ required: true, message: "请选择物品类型", trigger: "change" }],
    status: [{ required: true, message: "请选择物品状态", trigger: "change" }],
    price: [
      { required: true, message: "请输入物品价格", trigger: "blur" },
      { type: "number", min: 0, message: "价格必须大于等于0", trigger: "blur" }
    ],
    purchaseDate: [
      { required: true, message: "请选择购买日期", trigger: "change" }
    ]
  });

  // 加载位置选项
  const loadLocationOptions = async () => {
    try {
      if (!authStore.currentUser?.id) return;
      const userId = authStore.currentUser.id;
      const response = await getEntitiesByUser(userId);
      if (response.data) {
        // 构建级联选择器需要的树形结构
        locationOptions.value = buildLocationTree(response.data || []);
      }
    } catch (error) {
      console.error("加载位置选项失败:", error);
    }
  };

  // 构建位置树
  const buildLocationTree = (spaces: Entity[]) => {
    // 将空间列表转换为树形结构
    const result: any[] = [];
    const map = new Map();

    // 添加一个根空间作为顶级目录
    const rootSpace: {
      id: string;
      name: string;
      type: string;
      children: any[];
    } = {
      id: "0",
      name: "根空间",
      type: "space",
      children: []
    };

    // 添加根空间到结果中
    result.push(rootSpace);
    map.set("0", rootSpace);

    // 先创建映射
    spaces.forEach(space => {
      map.set(space.id, { ...space, children: [] });
    });

    // 然后构建树
    spaces.forEach(space => {
      const node = map.get(space.id);
      if (space.parentId) {
        const parent = map.get(space.parentId);
        if (parent) {
          parent.children.push(node);
        } else {
          // 如果找不到父节点，默认放到根空间下
          rootSpace.children.push(node);
        }
      } else {
        // 没有父节点的空间，作为根空间的子节点
        rootSpace.children.push(node);
      }
    });

    return result;
  };

  return {
    rules,
    locationOptions,
    loadLocationOptions,
    buildLocationTree
  };
}
