import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import compression from 'vite-plugin-compression'
import { visualizer } from 'rollup-plugin-visualizer'

// https://vite.dev/config/
export default defineConfig(({ command, mode }) => {
  // 根据当前工作目录中的 `mode` 加载 .env 文件
  const env = loadEnv(mode, process.cwd())
  
  return {
    plugins: [
      vue(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
        dts: './auto-imports.d.ts',
        imports: ['vue', 'vue-router', 'pinia'],
      }),
      Components({
        resolvers: [ElementPlusResolver()],
        dts: './components.d.ts',
      }),
      // gzip压缩
      compression({
        ext: '.gz', 
        algorithm: 'gzip',
        // 只压缩大于此大小的文件
        threshold: 10240, // 10kb
      }),
      // 在分析模式下生成包大小分析报告
      mode === 'analyze' 
        ? visualizer({
            open: true,
            filename: 'dist/stats.html',
            gzipSize: true,
            brotliSize: true,
          }) 
        : null,
    ],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    server: {
      host: '0.0.0.0', // 允许外部访问
      port: parseInt(env.VITE_PORT || '3000'),
      strictPort: false, // 如果端口被占用，会自动尝试下一个可用端口
      proxy: {
        '/api': {
          target: env.VITE_API_URL || 'http://localhost:26000',
          changeOrigin: true,
          // rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    },
    // 构建配置
    build: {
      // 产物目标浏览器最低版本
      target: 'es2015',
      // 启用/禁用 CSS 代码拆分
      cssCodeSplit: true,
      // 启用/禁用 brotli 压缩大小报告
      reportCompressedSize: false,
      // 最小化混淆，让控制台警告和错误更容易理解
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: command === 'build', // 生产环境删除console
          drop_debugger: command === 'build', // 生产环境删除debugger
        },
      },
      // chunk大小警告的限制
      chunkSizeWarningLimit: 1000,
      // 代码分割配置
      rollupOptions: {
        output: {
          // 静态资源分类和打包
          chunkFileNames: 'assets/js/[name]-[hash].js',
          entryFileNames: 'assets/js/[name]-[hash].js',
          assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
          // 按模块拆分代码
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia'],
            elementPlus: ['element-plus', '@element-plus/icons-vue'],
            echarts: ['echarts'],
            chartjs: ['chart.js'],
          },
        },
      },
    },
    // 性能提升
    optimizeDeps: {
      include: [
        'vue', 
        'vue-router', 
        'pinia', 
        'axios',
        'element-plus',
        '@element-plus/icons-vue',
        'echarts',
        'chart.js',
        'moment',
      ],
      exclude: [],
    },
    // SourceMap 生成
    sourcemap: command === 'serve',
  }
})
