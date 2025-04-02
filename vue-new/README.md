# 家庭物品管理系统 - Vue3重构版

## 项目介绍
本项目是家庭物品管理系统的前端重构项目，使用Vue3、TypeScript、Element Plus等现代前端技术栈进行开发，提供更加流畅、美观的用户界面和更好的用户体验。

## 技术栈
- Vue 3.5.x - 渐进式JavaScript框架
- TypeScript - 静态类型检查的JavaScript超集
- Pinia - Vue.js的状态管理工具
- Vue Router - Vue.js官方路由管理器
- Element Plus - 基于Vue 3的组件库
- ECharts - 功能强大的交互式图表库
- Axios - 基于Promise的HTTP客户端
- Vite - 现代化的前端构建工具

## 功能模块
- 实体管理：管理家庭物品的CRUD操作，支持图片、标签、位置等属性
- 标签管理：管理物品标签的CRUD操作，支持颜色定制
- 提醒管理：设置物品相关提醒，如过期提醒、维护提醒等
- 借出管理：记录物品借出和归还信息
- 数据统计：提供图表化的统计分析功能
- 系统设置：个性化配置、数据备份与恢复等

## 开发环境
- Node.js >= 18.x
- npm >= 9.x

## 项目启动
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产环境版本
npm run build
```

## 项目目录结构
```
vue-new/
├── public/             # 静态资源目录
├── src/
│   ├── api/            # API接口定义
│   ├── assets/         # 项目资源文件
│   ├── components/     # 通用组件
│   ├── main/           # 核心功能模块
│   ├── router/         # 路由配置
│   ├── store/          # Pinia状态管理
│   ├── views/          # 页面视图组件
│   ├── App.vue         # 根组件
│   └── main.ts         # 入口文件
├── .env                # 环境变量
├── index.html          # HTML模板
├── package.json        # 项目配置
├── tsconfig.json       # TypeScript配置
└── vite.config.ts      # Vite配置
```

## 特性
- 响应式设计 - 适配各种屏幕尺寸
- 主题定制 - 支持自定义主题颜色
- 数据可视化 - 直观展示物品统计信息
- 数据导入导出 - 支持数据的备份与恢复
- 动态搜索 - 支持实时搜索物品和标签

## 重构改进
1. 使用Vue3的Composition API替代Options API，提高代码复用性和可读性
2. 使用TypeScript提供类型安全，减少运行时错误
3. 使用Pinia替代Vuex，简化状态管理
4. 使用Element Plus组件库提供统一的UI体验
5. 优化路由结构，支持路由懒加载
6. 增强数据可视化功能，提供更多统计图表
7. 改进用户界面，提供更直观的操作体验
