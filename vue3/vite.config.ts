import { getPluginsList } from "./build/plugins";
import { include, exclude } from "./build/optimize";
import { type UserConfigExport, type ConfigEnv, loadEnv } from "vite";
import {
  root,
  alias,
  wrapperEnv,
  pathResolve,
  __APP_INFO__
} from "./build/utils";
import basicSsl from '@vitejs/plugin-basic-ssl'

export default ({ mode }: ConfigEnv): UserConfigExport => {
  const { VITE_CDN, VITE_PORT, VITE_COMPRESSION, VITE_PUBLIC_PATH } =
    wrapperEnv(loadEnv(mode, root));
  return {
    base: VITE_PUBLIC_PATH,
    root,
    resolve: {
      alias
    },
    // 服务端渲染
    server: {
      https: true ,
      // 端口号
      port: VITE_PORT,
      host: "0.0.0.0",
      // 本地跨域代理 https://cn.vitejs.dev/config/server-options.html#server-proxy
      proxy: {
        "/api": {
          target: "http://192.168.123.4:26000",
          // target: "http://localhost:26000",
          changeOrigin: true
          // 不需要重写路径，因为环境变量中的 URL 已包含 /api
          // rewrite: (path) => path.replace(/^\/api/, "")
        },
        "/uploads": {
          target: "http://192.168.123.4:26000",
          // target: "http://localhost:26000",
          changeOrigin: true
          // 不需要重写路径，因为环境变量中的 URL 已包含 /api
          // rewrite: (path) => path.replace(/^\/api/, "")
        }
      },
      // 预热文件以提前转换和缓存结果，降低启动期间的初始页面加载时长并防止转换瀑布
      warmup: {
        clientFiles: ["./index.html", "./src/{views,components}/*"]
      }
    },
    plugins: getPluginsList(VITE_CDN, VITE_COMPRESSION),

    // https://cn.vitejs.dev/config/dep-optimization-options.html#dep-optimization-options
    optimizeDeps: {
      include,
      exclude
    },
    build: {
      // https://cn.vitejs.dev/guide/build.html#browser-compatibility
      target: "es2015",
      sourcemap: false,
      // 消除打包大小超过500kb警告
      chunkSizeWarningLimit: 4000,
      rollupOptions: {
        input: {
          index: pathResolve("./index.html", import.meta.url)
        },
        // 静态资源分类打包
        output: {
          experimentalMinChunkSize: 10*1024, // 单位b
          manualChunks: {
            'vue-vendor': ['vue', 'vue-router', 'pinia'],

            'element-plus': ['element-plus'],
            'echarts': ['echarts'],
            'utils': ['@pureadmin/utils', '@vueuse/core', 'dayjs', 'axios'],
          },
          chunkFileNames: "static/js/[name]-[hash:8].js",
          entryFileNames: "static/js/[name]-[hash:8].js",
          assetFileNames: "static/[ext]/[name]-[hash:8].[ext]"
        }
      }
    },
    define: {
      __INTLIFY_PROD_DEVTOOLS__: false,
      __APP_INFO__: JSON.stringify(__APP_INFO__)
    }
  };
};
