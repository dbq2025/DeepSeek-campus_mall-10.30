<template>
  <div class="stats">
    <div class="page-card">
      <div class="toolbar">
        <h1 class="page-title stats__title">数据统计</h1>
        <div class="toolbar__right">
          <el-radio-group v-model="days" size="small" @change="loadTrend">
            <el-radio-button :value="7">近 7 天</el-radio-button>
            <el-radio-button :value="30">近 30 天</el-radio-button>
          </el-radio-group>
          <el-button :icon="Refresh" :loading="loading" @click="loadAll">刷新</el-button>
        </div>
      </div>

      <div v-if="error" class="stats__error">
        <EmptyState type="error" title="统计数据加载失败" :desc="error" @retry="loadAll" />
      </div>

      <div v-else class="stat-cards">
        <div class="stat-card">
          <span class="stat-card__label">累计成交额（近 {{ days }} 天）</span>
          <span class="stat-card__value stat-card__value--price">¥{{ formatMoney(totalAmount) }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-card__label">累计订单</span>
          <span class="stat-card__value">{{ totalOrders }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-card__label">待发货</span>
          <span class="stat-card__value stat-card__value--warning">
            {{ overview.pendingShipmentCount ?? 0 }}
          </span>
        </div>
        <div class="stat-card">
          <span class="stat-card__label">在库商品 / 注册用户</span>
          <span class="stat-card__value">
            {{ overview.totalProductCount ?? 0 }} / {{ overview.totalUserCount ?? 0 }}
          </span>
        </div>
      </div>
    </div>

    <template v-if="!error">
      <div class="page-card">
        <h2 class="block-title">销售趋势</h2>
        <ChartPanel
          :option="trendOption"
          :loading="loading"
          :empty="!loading && trendEmpty"
          height="320px"
        />
      </div>

      <div class="chart-grid">
        <div class="page-card">
          <h2 class="block-title">订单状态分布</h2>
          <ChartPanel
            :option="statusOption"
            :loading="loading"
            :empty="!loading && statusEmpty"
            height="300px"
          />
        </div>

        <div class="page-card">
          <h2 class="block-title">热销商品 TOP 5</h2>
          <ChartPanel
            :option="topOption"
            :loading="loading"
            :empty="!loading && !topProducts.length"
            height="300px"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import {
  fetchOrderStatusStat,
  fetchOverview,
  fetchSalesTrend,
  fetchTopProducts
} from '@/api/admin/stat'
import { formatMoney, sumMoney } from '@/utils/format'
import ChartPanel from '@/components/ChartPanel.vue'
import EmptyState from '@/components/EmptyState.vue'

const days = ref(7)
const loading = ref(true)
const error = ref('')
const overview = ref({})
const trend = ref({ dates: [], amounts: [] })
const statusStat = ref([])
const topProducts = ref([])

const totalAmount = computed(() => sumMoney(trend.value.amounts))
const totalOrders = computed(() => statusStat.value.reduce((acc, item) => acc + item.count, 0))
const trendEmpty = computed(() => !trend.value.amounts.some((item) => Number(item) > 0))
const statusEmpty = computed(() => !statusStat.value.some((item) => item.count > 0))

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 12, right: 24, top: 24, bottom: 8, containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trend.value.dates.map((date) => String(date).slice(5)),
    axisLine: { lineStyle: { color: '#e2e8f0' } },
    axisLabel: { color: '#94a3b8', interval: days.value > 10 ? 'auto' : 0, rotate: days.value > 10 ? 40 : 0 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#eef2f7' } },
    axisLabel: { color: '#94a3b8' }
  },
  series: [
    {
      name: '成交额',
      type: 'line',
      smooth: true,
      showSymbol: days.value <= 10,
      data: trend.value.amounts.map((item) => Number(item)),
      itemStyle: { color: '#4f46e5' },
      lineStyle: { width: 2 },
      areaStyle: { color: 'rgba(79,70,229,0.12)' }
    }
  ]
}))

const statusOption = computed(() => ({
  color: ['#4f46e5', '#7c3aed', '#12b76a', '#f79009', '#94a3b8'],
  tooltip: { trigger: 'item', formatter: '{b}：{c} 单（{d}%）' },
  legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#64748b' } },
  series: [
    {
      type: 'pie',
      radius: ['48%', '70%'],
      center: ['50%', '44%'],
      label: { show: false },
      itemStyle: { borderColor: '#ffffff', borderWidth: 2 },
      data: statusStat.value.map((item) => ({ name: item.statusText, value: item.count }))
    }
  ]
}))

const topOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 8, right: 24, top: 16, bottom: 8, containLabel: true },
  xAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#eef2f7' } },
    axisLabel: { color: '#94a3b8' }
  },
  yAxis: {
    type: 'category',
    inverse: true,
    data: topProducts.value.map((item) => item.productName),
    axisLine: { lineStyle: { color: '#e2e8f0' } },
    axisLabel: { color: '#64748b', width: 110, overflow: 'truncate' }
  },
  series: [
    {
      name: '销量',
      type: 'bar',
      barWidth: 14,
      data: topProducts.value.map((item) => item.sales),
      itemStyle: { color: '#4f46e5', borderRadius: [0, 4, 4, 0] }
    }
  ]
}))

async function loadTrend() {
  loading.value = true
  try {
    trend.value = (await fetchSalesTrend(days.value)) || { dates: [], amounts: [] }
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    const [o, s, st, top] = await Promise.all([
      fetchOverview(),
      fetchSalesTrend(days.value),
      fetchOrderStatusStat(),
      fetchTopProducts(5)
    ])
    overview.value = o || {}
    trend.value = s || { dates: [], amounts: [] }
    statusStat.value = st || []
    topProducts.value = top || []
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats > .page-card {
  padding: 20px 24px;
}

.stats__title {
  margin-bottom: 0;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  background: var(--mall-bg-subtle);
  border: 1px solid var(--mall-border-light);
  border-radius: var(--mall-radius);
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-card__label {
  font-size: 12px;
  color: var(--mall-text-3);
}

.stat-card__value {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  color: var(--mall-text-1);
}

.stat-card__value--price {
  color: var(--mall-price);
}

.stat-card__value--warning {
  color: var(--mall-warning);
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stats__error {
  padding: 8px 0;
}

@media (max-width: 1100px) {
  .stat-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .stats > .page-card {
    padding: 16px;
  }

  .stat-card__value {
    font-size: 20px;
  }
}
</style>
