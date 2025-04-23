/**
 * 应用程序主入口文件
 * 负责初始化Vue应用、注册全局组件、指令和插件
 */
import App from "./App.vue";
import router from "./router";
import { setupStore } from "@/store";
import { getPlatformConfig } from "./config";
import { MotionPlugin } from "@vueuse/motion";
import { useEcharts } from "@/plugins/echarts";
import { createApp, type Directive } from "vue";
import { useElementPlus } from "@/plugins/elementPlus";
import { injectResponsiveStorage } from "@/utils/responsive";

// import Table from "@pureadmin/table";
// import PureDescriptions from "@pureadmin/descriptions";

// 引入重置样式
import "./style/reset.scss";
// 导入公共样式
import "./style/index.scss";
// 一定要在main.ts中导入tailwind.css，防止vite每次hmr都会请求src/style/index.scss整体css文件导致热更新慢的问题
import "./style/tailwind.css";
import "element-plus/dist/index.css";
// 导入字体图标
import "./assets/iconfont/iconfont.js";
import "./assets/iconfont/iconfont.css";

/**
 * 创建Vue应用实例
 */
const app = createApp(App);

// 自定义指令注册
import * as directives from "@/directives";
Object.keys(directives).forEach(key => {
  app.directive(key, (directives as { [key: string]: Directive })[key]);
});

// 全局注册@iconify/vue图标库组件
import {
  IconifyIconOffline,
  IconifyIconOnline,
  FontIcon
} from "./components/ReIcon";
app.component("IconifyIconOffline", IconifyIconOffline);
app.component("IconifyIconOnline", IconifyIconOnline);
app.component("FontIcon", FontIcon);

// 全局注册按钮级别权限组件
import { Auth } from "@/components/ReAuth";
import { Perms } from "@/components/RePerms";
app.component("Auth", Auth);
app.component("Perms", Perms);

// 全局注册vue-tippy提示组件
import "tippy.js/dist/tippy.css";
import "tippy.js/themes/light.css";
import VueTippy from "vue-tippy";
app.use(VueTippy);

/**
 * 应用初始化流程
 * 1. 获取平台配置
 * 2. 设置状态管理
 * 3. 挂载路由和插件
 * 4. 注入响应式存储
 * 5. 挂载应用到DOM
 */
getPlatformConfig(app).then(async config => {
  // 初始化Pinia存储
  setupStore(app);

  // 挂载路由
  app.use(router);
  // 等待路由准备就绪
  await router.isReady();

  // 注入响应式存储
  injectResponsiveStorage(app, config);

  // 挂载插件
  app
    .use(MotionPlugin)
    .use(useElementPlus)
    //.use(Table)
    // .use(PureDescriptions)
    .use(useEcharts);

  // 挂载应用到DOM
  app.mount("#app");
});
