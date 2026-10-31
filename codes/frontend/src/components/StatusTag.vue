<template>
  <el-tag :type="current.type" effect="light" size="small" disable-transitions>
    {{ text || current.label }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

/**
 * 状态标签：所有状态 → 文案/颜色的映射集中在这里，页面不再各写一份。
 * 订单状态文案以接口返回的 statusText 为准（传 text 覆盖），本映射只做兜底与配色。
 */
const ORDER_MAP = {
  0: { label: '待付款', type: 'warning' },
  1: { label: '待发货', type: 'primary' },
  2: { label: '待收货', type: 'primary' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'info' }
}

const PRODUCT_MAP = {
  1: { label: '上架中', type: 'success' },
  0: { label: '已下架', type: 'info' }
}

const props = defineProps({
  status: { type: [Number, String], default: 0 },
  type: { type: String, default: 'order' },
  text: { type: String, default: '' }
})

const current = computed(() => {
  const map = props.type === 'product' ? PRODUCT_MAP : ORDER_MAP
  return map[Number(props.status)] || { label: '未知', type: 'info' }
})
</script>
