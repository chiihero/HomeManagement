// HTTP 响应结果类型
export interface ResponseResult<T = any> {
  code: number;
  message: string;
  data: T;
}

// HTTP 请求配置类型
export interface RequestConfig {
  baseURL?: string;
  timeout?: number;
  headers?: Record<string, string>;
  params?: Record<string, any>;
  data?: any;
  responseType?: "json" | "blob" | "arraybuffer" | "text";
}

// HTTP 响应类型
export interface Response<T = any> {
  data: T;
  status: number;
  statusText: string;
  headers: Record<string, string>;
  config: RequestConfig;
}

// HTTP 错误类型
export interface HttpError extends Error {
  config: RequestConfig;
  code?: string;
  request?: any;
  response?: Response;
}

// HTTP 请求方法类型
export type HttpMethod =
  | "get"
  | "post"
  | "put"
  | "delete"
  | "patch"
  | "head"
  | "options";

// HTTP 请求函数类型
export type HttpRequest = <T = any, R = ResponseResult<T>>(
  url: string,
  config?: RequestConfig
) => Promise<R>;

// HTTP 实例类型
export interface HttpInstance {
  request: HttpRequest;
  get: <T = any, R = ResponseResult<T>>(
    url: string,
    config?: RequestConfig
  ) => Promise<R>;
  post: <T = any, R = ResponseResult<T>>(
    url: string,
    data?: any,
    config?: RequestConfig
  ) => Promise<R>;
  put: <T = any, R = ResponseResult<T>>(
    url: string,
    data?: any,
    config?: RequestConfig
  ) => Promise<R>;
  delete: <T = any, R = ResponseResult<T>>(
    url: string,
    config?: RequestConfig
  ) => Promise<R>;
  patch: <T = any, R = ResponseResult<T>>(
    url: string,
    data?: any,
    config?: RequestConfig
  ) => Promise<R>;
  head: <T = any, R = ResponseResult<T>>(
    url: string,
    config?: RequestConfig
  ) => Promise<R>;
  options: <T = any, R = ResponseResult<T>>(
    url: string,
    config?: RequestConfig
  ) => Promise<R>;
  interceptors: {
    request: {
      use: (
        onFulfilled?: (config: RequestConfig) => RequestConfig,
        onRejected?: (error: HttpError) => any
      ) => number;
      eject: (id: number) => void;
    };
    response: {
      use: (
        onFulfilled?: (response: Response) => any,
        onRejected?: (error: HttpError) => any
      ) => number;
      eject: (id: number) => void;
    };
  };
}
