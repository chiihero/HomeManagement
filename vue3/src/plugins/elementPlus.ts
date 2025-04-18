// 按需引入element-plus（该方法稳定且明确。当然也支持：https://element-plus.org/zh-CN/guide/quickstart.html#%E6%8C%89%E9%9C%80%E5%AF%BC%E5%85%A5）
import type { App, Component } from "vue";
import {
  /**
   * 为了方便演示平台将 element-plus 导出的所有组件引入，实际使用中如果你没用到哪个组件，将其注释掉就行
   * 导出来源：https://github.com/element-plus/element-plus/blob/dev/packages/element-plus/component.ts#L111-L211
   * */
  // ElAffix,         // 固钉组件 (未使用)
  // ElAlert,         // 警告提示组件 (未使用)
  ElAutocomplete,  // 带输入建议的输入框组件 (未使用)
  // ElAutoResizer, // 自动调整大小的组件 (已禁用)
  ElAvatar,        // 头像组件
  // ElAnchor,        // 锚点组件 (未使用)
  // ElAnchorLink,    // 锚点链接组件 (未使用)
  ElBacktop,       // 回到顶部组件
  ElBadge,         // 徽章组件
  ElBreadcrumb,    // 面包屑导航组件
  ElBreadcrumbItem,// 面包屑导航项
  ElButton,        // 按钮组件
  ElButtonGroup,   // 按钮组组件
  ElCalendar,      // 日历组件 (未使用)
  ElCard,          // 卡片组件
  // ElCarousel,      // 走马灯/轮播图组件 (未使用)
  // ElCarouselItem,  // 走马灯/轮播图项 (未使用)
  // ElCascader,      // 级联选择器组件 (未使用)
  // ElCascaderPanel, // 级联选择面板组件 (未使用)
  // ElCheckTag,      // 可选择的标签组件 (未使用)
  ElCheckbox,      // 复选框组件
  ElCheckboxButton,// 复选框按钮组件
  ElCheckboxGroup, // 复选框组组件
  ElCol,           // 栅格列组件
  ElCollapse,      // 折叠面板组件
  ElCollapseItem,  // 折叠面板项组件
  // ElCollapseTransition, // 折叠过渡效果组件 (未使用)
  ElColorPicker,   // 颜色选择器组件
  ElConfigProvider,// 全局配置组件
  ElContainer,     // 布局容器组件
  ElAside,         // 侧边栏容器组件
  ElFooter,        // 页脚容器组件
  ElHeader,        // 页头容器组件
  ElMain,          // 主内容容器组件
  ElDatePicker,    // 日期选择器组件
  ElDescriptions,  // 描述列表组件
  ElDescriptionsItem, // 描述列表项组件
  ElDialog,        // 对话框组件
  ElDivider,       // 分割线组件
  ElDrawer,        // 抽屉组件
  ElDropdown,      // 下拉菜单组件
  ElDropdownItem,  // 下拉菜单项组件
  ElDropdownMenu,  // 下拉菜单列表组件
  ElEmpty,         // 空状态组件
  ElForm,          // 表单组件
  ElFormItem,      // 表单项组件
  ElIcon,          // 图标组件
  ElImage,         // 图片组件
  // ElImageViewer,   // 图片查看器组件 (未使用)
  ElInput,         // 输入框组件
  // ElInputNumber,   // 数字输入框组件 (未使用)
  ElLink,          // 链接组件
  ElMenu,          // 菜单组件
  ElMenuItem,      // 菜单项组件
  ElMenuItemGroup, // 菜单组组件
  ElSubMenu,       // 子菜单组件
  ElPageHeader,    // 页头组件 (未使用)
  ElPagination,    // 分页组件
  ElPopconfirm,    // 气泡确认框组件
  // ElPopover,     // 弹出框组件 (已禁用)
  // ElPopper,        // 弹出层组件 (未使用)
  ElProgress,      // 进度条组件
  ElRadio,         // 单选框组件
  ElRadioButton,   // 单选按钮组件
  ElRadioGroup,    // 单选框组组件
  // ElRate,          // 评分组件 (未使用)
  // ElResult,        // 结果页组件 (未使用)
  ElRow,           // 栅格行组件
  ElScrollbar,     // 滚动条组件
  ElSelect,        // 选择器组件
  ElOption,        // 选择器选项组件
  ElOptionGroup,   // 选择器选项组组件
  // ElSelectV2,      // 虚拟列表选择器组件 (未使用)
  ElSkeleton,      // 骨架屏组件
  // ElSkeletonItem,  // 骨架屏子项组件 (未使用)
  // ElSlider,        // 滑块组件 (未使用)
  ElSpace,         // 间距组件
  ElStatistic,     // 统计数值组件 (未使用)
  // ElCountdown,     // 倒计时组件 (未使用)
  ElSteps,         // 步骤条组件
  ElStep,          // 步骤条项组件
  // ElSwitch,        // 开关组件 (未使用)
  ElTable,         // 表格组件
  ElTableColumn,   // 表格列组件
  // ElTableV2,       // 虚拟列表表格组件 (未使用)
  ElTabs,          // 标签页组件
  ElTabPane,       // 标签页面板组件
  ElTag,           // 标签组件
  ElText,          // 文本组件
  // ElTimePicker,    // 时间选择器组件 (未使用)
  // ElTimeSelect,    // 时间选择组件 (未使用)
  // ElTimeline,      // 时间线组件 (未使用)
  // ElTimelineItem,  // 时间线项组件 (未使用)
  ElTooltip,       // 文字提示组件 (未使用)
  // ElTransfer,      // 穿梭框组件 (未使用)
  ElTree,          // 树形控件组件
  // ElTreeSelect,    // 树形选择组件 (未使用)
  // ElTreeV2,        // 虚拟化树形组件 (未使用)
  ElUpload,        // 上传组件
  // ElWatermark,     // 水印组件 (未使用)
  // ElTour,          // 导览组件 (未使用)
  // ElTourStep,      // 导览步骤组件 (未使用)
  ElSegmented,     // 分段控制器组件
  /**
   * 为了方便演示平台将 element-plus 导出的所有插件引入，实际使用中如果你没用到哪个插件，将其注释掉就行
   * 导出来源：https://github.com/element-plus/element-plus/blob/dev/packages/element-plus/plugin.ts#L11-L16
   * */
  ElLoading,        // 加载指令
  ElInfiniteScroll, // 无限滚动指令 (未使用)
  // ElPopoverDirective, // v-popover 指令 (已禁用)
  ElMessage,        // 全局消息提示方法
  ElMessageBox,     // 全局消息弹框方法
  ElNotification    // 全局通知方法
} from "element-plus";

const components = [
  // ElAffix,         // 固钉组件 (未使用)
  // ElAlert,         // 警告提示组件 (未使用)
  ElAutocomplete,  // 带输入建议的输入框组件 (未使用)
  // ElAutoResizer, // 自动调整大小的组件 (已禁用)
  ElAvatar,        // 头像组件
  // ElAnchor,        // 锚点组件 (未使用)
  // ElAnchorLink,    // 锚点链接组件 (未使用)
  ElBacktop,       // 回到顶部组件
  ElBadge,         // 徽章组件
  ElBreadcrumb,    // 面包屑导航组件
  ElBreadcrumbItem,// 面包屑导航项
  ElButton,        // 按钮组件
  ElButtonGroup,   // 按钮组组件
  ElCalendar,      // 日历组件 (未使用)
  ElCard,          // 卡片组件
  // ElCarousel,      // 走马灯/轮播图组件 (未使用)
  // ElCarouselItem,  // 走马灯/轮播图项 (未使用)
  // ElCascader,      // 级联选择器组件 (未使用)
  // ElCascaderPanel, // 级联选择面板组件 (未使用)
  // ElCheckTag,      // 可选择的标签组件 (未使用)
  ElCheckbox,      // 复选框组件
  ElCheckboxButton,// 复选框按钮组件
  ElCheckboxGroup, // 复选框组组件
  ElCol,           // 栅格列组件
  ElCollapse,      // 折叠面板组件
  ElCollapseItem,  // 折叠面板项组件
  // ElCollapseTransition, // 折叠过渡效果组件 (未使用)
  ElColorPicker,   // 颜色选择器组件
  ElConfigProvider,// 全局配置组件
  ElContainer,     // 布局容器组件
  ElAside,         // 侧边栏容器组件
  ElFooter,        // 页脚容器组件
  ElHeader,        // 页头容器组件
  ElMain,          // 主内容容器组件
  ElDatePicker,    // 日期选择器组件
  ElDescriptions,  // 描述列表组件
  ElDescriptionsItem, // 描述列表项组件
  ElDialog,        // 对话框组件
  ElDivider,       // 分割线组件
  ElDrawer,        // 抽屉组件
  ElDropdown,      // 下拉菜单组件
  ElDropdownItem,  // 下拉菜单项组件
  ElDropdownMenu,  // 下拉菜单列表组件
  ElEmpty,         // 空状态组件
  ElForm,          // 表单组件
  ElFormItem,      // 表单项组件
  ElIcon,          // 图标组件
  ElImage,         // 图片组件
  // ElImageViewer,   // 图片查看器组件 (未使用)
  ElInput,         // 输入框组件
  // ElInputNumber,   // 数字输入框组件 (未使用)
  ElLink,          // 链接组件
  ElMenu,          // 菜单组件
  ElMenuItem,      // 菜单项组件
  ElMenuItemGroup, // 菜单组组件
  ElSubMenu,       // 子菜单组件
  ElPageHeader,    // 页头组件 (未使用)
  ElPagination,    // 分页组件
  ElPopconfirm,    // 气泡确认框组件
  // ElPopover,     // 弹出框组件 (已禁用)
  // ElPopper,        // 弹出层组件 (未使用)
  ElProgress,      // 进度条组件
  ElRadio,         // 单选框组件
  ElRadioButton,   // 单选按钮组件
  ElRadioGroup,    // 单选框组组件
  // ElRate,          // 评分组件 (未使用)
  // ElResult,        // 结果页组件 (未使用)
  ElRow,           // 栅格行组件
  ElScrollbar,     // 滚动条组件
  ElSelect,        // 选择器组件
  ElOption,        // 选择器选项组件
  ElOptionGroup,   // 选择器选项组组件
  // ElSelectV2,      // 虚拟列表选择器组件 (未使用)
  ElSkeleton,      // 骨架屏组件
  // ElSkeletonItem,  // 骨架屏子项组件 (未使用)
  // ElSlider,        // 滑块组件 (未使用)
  ElSpace,         // 间距组件
  ElStatistic,     // 统计数值组件 (未使用)
  // ElCountdown,     // 倒计时组件 (未使用)
  ElSteps,         // 步骤条组件
  ElStep,          // 步骤条项组件
  // ElSwitch,        // 开关组件 (未使用)
  ElTable,         // 表格组件
  ElTableColumn,   // 表格列组件
  // ElTableV2,       // 虚拟列表表格组件 (未使用)
  ElTabs,          // 标签页组件
  ElTabPane,       // 标签页面板组件
  ElTag,           // 标签组件
  ElText,          // 文本组件
  // ElTimePicker,    // 时间选择器组件 (未使用)
  // ElTimeSelect,    // 时间选择组件 (未使用)
  // ElTimeline,      // 时间线组件 (未使用)
  // ElTimelineItem,  // 时间线项组件 (未使用)
  ElTooltip,       // 文字提示组件 (未使用)
  // ElTransfer,      // 穿梭框组件 (未使用)
  ElTree,          // 树形控件组件
  // ElTreeSelect,    // 树形选择组件 (未使用)
  // ElTreeV2,        // 虚拟化树形组件 (未使用)
  ElUpload,        // 上传组件
  // ElWatermark,     // 水印组件 (未使用)
  // ElTour,          // 导览组件 (未使用)
  // ElTourStep,      // 导览步骤组件 (未使用)
  ElSegmented      // 分段控制器组件
];

const plugins = [
  ElLoading,        // 加载指令
  // ElInfiniteScroll, // 无限滚动指令 (未使用)
  // ElPopoverDirective, // 弹出框指令 (已禁用)
  ElMessage,        // 全局消息提示方法
  ElMessageBox,     // 全局消息弹框方法
  ElNotification    // 全局通知方法
];

/** 按需引入`element-plus` */
export function useElementPlus(app: App) {
  // 全局注册组件
  components.forEach((component: Component) => {
    app.component(component.name, component);
  });
  // 全局注册插件
  plugins.forEach(plugin => {
    app.use(plugin);
  });
}
