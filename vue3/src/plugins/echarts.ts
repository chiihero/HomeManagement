import type { App } from "vue";
import * as echarts from "echarts/core";
import { 
  PieChart,      // 饼图
  BarChart,      // 柱状图
  // LineChart      // 折线图
} from "echarts/charts";
import { 
  // CanvasRenderer,  // Canvas渲染器
  SVGRenderer      // SVG渲染器
} from "echarts/renderers";
import {
  GridComponent,      // 网格组件，用于直角坐标系中的布局
  TitleComponent,     // 标题组件，用于设置图表标题
  // PolarComponent,     // 极坐标系组件，用于极坐标下的图表
  LegendComponent,    // 图例组件，用于展示图表项的标记、名称和颜色
  // GraphicComponent,   // 图形组件，用于在图表中添加自定义图形元素
  // ToolboxComponent,   // 工具箱组件，提供保存图片、数据视图、动态类型切换等功能
  TooltipComponent,   // 提示框组件，用于鼠标悬浮时显示数据信息
  // DataZoomComponent,  // 数据区域缩放组件，用于缩放或平移显示区域
  // VisualMapComponent  // 视觉映射组件，用于将数据映射到视觉元素上
} from "echarts/components";

const { use } = echarts;

use([
  PieChart,          // 饼图
  BarChart,          // 柱状图
  // LineChart,         // 折线图
  // CanvasRenderer,    // Canvas渲染器
  SVGRenderer,       // SVG渲染器
  GridComponent,     // 网格组件
  TitleComponent,    // 标题组件
  // PolarComponent,    // 极坐标系组件
  LegendComponent,   // 图例组件
  // GraphicComponent,  // 图形组件
  // ToolboxComponent,  // 工具箱组件
  TooltipComponent,  // 提示框组件
  // DataZoomComponent, // 数据区域缩放组件
  // VisualMapComponent // 视觉映射组件
]);

/**
 * @description 按需引入echarts，具体看 https://echarts.apache.org/handbook/zh/basics/import/#%E5%9C%A8-typescript-%E4%B8%AD%E6%8C%89%E9%9C%80%E5%BC%95%E5%85%A5
 * @see 温馨提示：必须将 `$echarts` 添加到全局 `globalProperties` ，具体看 https://pure-admin-utils.netlify.app/hooks/useECharts/useECharts#%E4%BD%BF%E7%94%A8%E5%89%8D%E6%8F%90
 */
export function useEcharts(app: App) {
  app.config.globalProperties.$echarts = echarts;
}

export default echarts;
