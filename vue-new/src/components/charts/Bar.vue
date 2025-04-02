<template>
  <div class="chart-container">
    <canvas ref="chartRef"></canvas>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, watch, onBeforeUnmount, PropType } from 'vue';
import Chart from 'chart.js/auto';

export default defineComponent({
  name: 'BarChart',
  props: {
    chartData: {
      type: Object as PropType<{
        labels: string[];
        datasets: {
          label: string;
          data: number[];
          backgroundColor: string | string[];
          borderColor?: string | string[];
          borderWidth?: number;
        }[];
      }>,
      required: true
    },
    options: {
      type: Object,
      default: () => ({})
    },
    horizontal: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const chartRef = ref<HTMLCanvasElement | null>(null);
    let chartInstance: Chart | null = null;

    // 初始化图表
    const initChart = () => {
      if (!chartRef.value) return;

      const ctx = chartRef.value.getContext('2d');
      if (!ctx) return;

      // 如果已存在图表实例，先销毁
      if (chartInstance) {
        chartInstance.destroy();
      }

      // 创建图表配置
      const defaultOptions = {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true
          }
        },
        plugins: {
          legend: {
            display: props.chartData.datasets.length > 1,
            position: 'top'
          }
        }
      };

      // 合并选项
      const mergedOptions = { ...defaultOptions, ...props.options };

      // 创建图表实例
      chartInstance = new Chart(ctx, {
        type: props.horizontal ? 'bar' : 'bar',
        data: props.chartData,
        options: {
          ...mergedOptions,
          indexAxis: props.horizontal ? 'y' : 'x'
        }
      });
    };

    // 更新图表数据
    const updateChart = () => {
      if (!chartInstance) return;
      
      chartInstance.data = props.chartData;
      chartInstance.update();
    };

    // 组件挂载时创建图表
    onMounted(() => {
      initChart();
    });

    // 组件卸载前销毁图表
    onBeforeUnmount(() => {
      if (chartInstance) {
        chartInstance.destroy();
        chartInstance = null;
      }
    });

    // 监听数据变化更新图表
    watch(() => props.chartData, () => {
      updateChart();
    }, { deep: true });

    // 监听水平方向属性变化
    watch(() => props.horizontal, () => {
      initChart();
    });

    return {
      chartRef
    };
  }
});
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
}
</style> 