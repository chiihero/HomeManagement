// 存储数据类型
interface StorageData<T> {
  value: T;
  expire?: number;
}

// 创建存储实例
function createStorage(options: {
  storage: Storage;
  prefixKey?: string;
  expire?: number;
}) {
  const { storage, prefixKey = "", expire = 60 * 60 * 24 * 7 } = options;

  // 获取存储键名
  const getKey = (key: string) => {
    return `${prefixKey}${key}`.toUpperCase();
  };

  // 设置存储项
  const setItem = <T>(key: string, value: T, expireTime?: number) => {
    const data: StorageData<T> = {
      value,
      expire: expireTime
        ? Date.now() + expireTime * 1000
        : Date.now() + expire * 1000
    };
    storage.setItem(getKey(key), JSON.stringify(data));
  };

  // 获取存储项
  const getItem = <T>(key: string): T | null => {
    const item = storage.getItem(getKey(key));
    if (!item) return null;

    try {
      const data = JSON.parse(item) as StorageData<T>;
      if (data.expire && data.expire < Date.now()) {
        removeItem(key);
        return null;
      }
      return data.value;
    } catch {
      return null;
    }
  };

  // 移除存储项
  const removeItem = (key: string) => {
    storage.removeItem(getKey(key));
  };

  // 清空存储
  const clear = () => {
    storage.clear();
  };

  return {
    setItem,
    getItem,
    removeItem,
    clear
  };
}

// 创建本地存储实例
export const storageLocal = createStorage({
  storage: localStorage,
  prefixKey: "HOME_MANAGEMENT_",
  expire: 60 * 60 * 24 * 7 // 7天
});

// 创建会话存储实例
export const storageSession = createStorage({
  storage: sessionStorage,
  prefixKey: "HOME_MANAGEMENT_",
  expire: 60 * 60 // 1小时
});
