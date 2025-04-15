import { ref, reactive } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { Entity, EntityStatus } from "@/types/entity";
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
      if (!authStore.userId) return;
      const userId = authStore.userId;
      const response = await getEntitiesByUser(userId);
      // @ts-ignore - 类型断言，忽略data属性不存在的错误
      if (response.data) {
        // 构建级联选择器需要的树形结构
        // @ts-ignore - 类型断言，忽略data属性不存在的错误
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

  // 计算对比色，确保文字在背景色上可见
  const getContrastColor = (bgColor: string) => {
    if (!bgColor) return "#ffffff";
    
    // 将十六进制颜色转换为RGB
    let color = bgColor.charAt(0) === "#" ? bgColor.substring(1) : bgColor;
    let r = parseInt(color.substr(0, 2), 16);
    let g = parseInt(color.substr(2, 2), 16);
    let b = parseInt(color.substr(4, 2), 16);
    
    // 计算亮度
    let yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
    
    // 如果亮度高于128，返回黑色，否则返回白色
    return (yiq >= 128) ? "#000000" : "#ffffff";
  };

  // 格式化标签数据为后端需要的格式
  const formatTagsForSubmit = (tagIds: any[], existingTags: any[] = []) => {
    if (!Array.isArray(tagIds) || tagIds.length === 0) return [];

    // 将标签ID数组转换为完整的Tag对象数组
    return tagIds.map(tagId => {
      const foundTag = existingTags.find(tag => tag.id === tagId);
      if (foundTag) {
        return foundTag; // 返回完整的Tag对象
      }
      // 如果是新创建的标签，提供一个默认值
      return {
        id: tagId,
        name: String(tagId), // 使用ID作为名称
        color: "#909399", // 默认颜色
        userId: authStore.userId?.toString() || "" // 当前用户ID，确保是字符串类型
      };
    });
  };

  return {
    rules,
    locationOptions,
    loadLocationOptions,
    getContrastColor,
    buildLocationTree,
    formatTagsForSubmit
  };
}
