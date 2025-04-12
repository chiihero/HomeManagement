import axios, {
  type InternalAxiosRequestConfig,
  type AxiosResponse,
  type AxiosError,
  type AxiosRequestConfig
} from "axios";
import { ElMessage } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import { router } from "@/router";

// 定义错误响应类型
interface ErrorResponse {
  msg?: string;
  message?: string;
  [key: string]: any;
}

// 请求去重实现
const pendingRequests = new Map<string, AbortController>();

// 创建请求Key
const createRequestKey = (config: AxiosRequestConfig): string => {
  const { method, url, params, data } = config;
  return `${method}_${url}_${JSON.stringify(params)}_${JSON.stringify(data)}`;
};

// 添加到等待队列
const addPendingRequest = (config: AxiosRequestConfig): void => {
  const requestKey = createRequestKey(config);
  if (pendingRequests.has(requestKey)) {
    removePendingRequest(config);
  }
  const controller = new AbortController();
  config.signal = controller.signal;
  pendingRequests.set(requestKey, controller);
};

// 移除等待队列请求
const removePendingRequest = (config: AxiosRequestConfig): void => {
  const requestKey = createRequestKey(config);
  if (pendingRequests.has(requestKey)) {
    const controller = pendingRequests.get(requestKey);
    controller?.abort();
    pendingRequests.delete(requestKey);
  }
};

// 创建axios实例
const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
    "X-Requested-With": "XMLHttpRequest"
  },
  withCredentials: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN"
});

// 请求拦截器
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 添加请求到等待队列（实现请求去重）
    if (config.method?.toLowerCase() !== "get") {
      addPendingRequest(config);
    }

    // 如果有token，则添加到请求头
    const token = localStorage.getItem("token");
    if (token && config.headers) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }

    // 确保非FormData类型的请求体被正确序列化为JSON
    if (config.data && !(config.data instanceof FormData)) {
      if (
        config.method?.toLowerCase() === "post" ||
        config.method?.toLowerCase() === "put"
      ) {
        config.headers["Content-Type"] = "application/json";
        config.data = JSON.stringify(config.data);
      }
    }

    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
http.interceptors.response.use(
  (response: AxiosResponse) => {
    // 从等待队列中移除请求
    removePendingRequest(response.config);

    // 如果响应中包含成功消息，显示给用户
    if (response.data && response.data.msg && response.status === 200) {
      ElMessage.success(response.data.msg);
    }

    // 直接返回响应的data部分，而不是整个响应对象
    return response.data;
  },
  (error: AxiosError<ErrorResponse>) => {
    // 移除等待队列中的请求
    if (error.config) {
      removePendingRequest(error.config);
    }

    let errMsg = "请求失败";

    if (error.response) {
      // 请求已发出，但服务器响应状态码不在 2xx 范围内
      const status = error.response.status;

      switch (status) {
        case 400:
          errMsg = error.response.data?.msg || "请求参数错误";
          break;
        case 401:
          errMsg = "用户未授权或会话已过期";
          // 尝试刷新token或跳转到登录页
          const userStore = useUserStoreHook();

          if (userStore.handRefreshToken) {
            userStore.handRefreshToken({}).then(success => {
              if (success && error.config) {
                // 重新发起请求
                return axios(error.config);
              } else {
                // 跳转登录页
                router.replace(
                  "/login?redirect=" +
                    encodeURIComponent(router.currentRoute.value.fullPath)
                );
                return Promise.reject(errMsg);
              }
            });
          } else {
            // 跳转登录页
            router.replace(
              "/login?redirect=" +
                encodeURIComponent(router.currentRoute.value.fullPath)
            );
          }
          break;
        case 403:
          errMsg = "无权访问该资源";
          break;
        case 404:
          errMsg = "请求的资源不存在";
          break;
        case 500:
          errMsg = error.response.data?.msg || "服务器内部错误";
          break;
        default:
          errMsg = `请求失败，错误码：${status}`;
      }

      // 使用服务器返回的错误消息
      if (error.response.data && error.response.data.msg) {
        errMsg = error.response.data.msg;
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      if (error.code === "ECONNABORTED") {
        errMsg = "请求超时，请稍后重试";
      } else {
        errMsg = "网络异常，无法连接到服务器";
      }
    } else {
      // 在设置请求时触发了错误
      errMsg = error.message || "请求失败，请稍后重试";
    }

    // 显示错误消息
    ElMessage.error(errMsg);

    return Promise.reject(errMsg);
  }
);

export default http;
