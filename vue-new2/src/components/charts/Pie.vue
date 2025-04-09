<template>
  <div ref="chartRef" :style="{ height: height + 'px' }" />
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import * as echarts from "echarts";
import type { EChartsOption } from "echarts";

interface Props {
  chartData: {
    labels: string[];
    datasets: {
      backgroundColor: string[];
      data: number[];
    }[];
  };
  height: number;
}

const props = defineProps<Props>();
const chartRef = ref<HTMLElement>();
let chart: echarts.ECharts | null = null;

const initChart = () => {
  if (!chartRef.value) return;

  chart = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  if (!chart) return;

  const option: EChartsOption = {
    tooltip: {
      trigger: "item",
      formatter: "{a} <br/>{b}: {c} ({d}%)"
    },
    legend: {
      orient: "vertical",
      right: 10,
      top: "center",
      data: props.chartData.labels
    },
    series: [
      {
        name: "数量",
        type: "pie",
        radius: ["50%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: "#fff",
          borderWidth: 2
        },
        label: {
          show: false,
          position: "center"
        },
        emphasis: {
          label: {
            show: true,
            fontSize: "18",
            fontWeight: "bold"
          }
        },
        labelLine: {
          show: false
        },
        data: props.chartData.labels.map((label, index) => ({
          name: label,
          value: props.chartData.datasets[0].data[index],
          itemStyle: {
            color: props.chartData.datasets[0].backgroundColor[index]
          }
        }))
      }
    ]
  };

  chart.setOption(option);
};

watch(
  () => props.chartData,
  () => {
    updateChart();
  },
  { deep: true }
);

onMounted(() => {
  initChart();
  window.addEventListener("resize", () => {
    chart?.resize();
  });
});
</script>
