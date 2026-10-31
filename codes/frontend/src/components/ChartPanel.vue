<template>
  <div class="chart-panel">
    <div v-if="loading" class="chart-panel__mask">
      <el-icon class="is-loading" :size="22"><Loading /></el-icon>
    </div>
    <div v-else-if="empty" class="chart-panel__empty" :style="{ height }">
      <el-icon :size="28"><DataLine /></el-icon>
      <span>{{ emptyText }}</span>
    </div>
    <div v-show="!empty && !loading" ref="chartEl" class="chart-panel__canvas" :style="{ height }"></div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import echarts from '@/utils/echarts'

const props = defineProps({
  option: { type: Object, default: () => ({}) },
  height: { type: String, default: '280px' },
  loading: { type: Boolean, default: false },
  empty: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无数据' }
})

const chartEl = ref(null)
let chart = null

function render() {
  if (!chart) return
  chart.setOption(props.option || {}, true)
}

function resize() {
  chart?.resize()
}

onMounted(() => {
  chart = echarts.init(chartEl.value)
  render()
  window.addEventListener('resize', resize)
})

// option 变化时整体重绘（notMerge=true，避免旧 series 残留）
watch(() => props.option, render, { deep: true })

// 空态/加载态切换后容器尺寸会变，需要重新测量
watch(
  () => [props.empty, props.loading],
  async () => {
    await nextTick()
    resize()
  }
)

// 容器宽度变化（侧栏折叠、窗口缩放）也要跟着 resize
let observer = null
onMounted(() => {
  if (typeof ResizeObserver !== 'undefined' && chartEl.value) {
    observer = new ResizeObserver(() => resize())
    observer.observe(chartEl.value)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  observer?.disconnect()
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.chart-panel {
  position: relative;
  width: 100%;
}

.chart-panel__canvas {
  width: 100%;
}

.chart-panel__mask,
.chart-panel__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--mall-text-3);
  font-size: 13px;
}

.chart-panel__mask {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.7);
  z-index: 2;
}

.chart-panel__empty {
  flex-direction: column;
  min-height: 180px;
}
</style>
