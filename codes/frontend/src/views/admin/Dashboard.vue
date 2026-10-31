<template>
  <div class="dashboard">
    <div v-if="error" class="page-card">
      <EmptyState type="error" title="概览数据加载失败" :desc="error" @retry="load" />
    </div>

    <template v-else>
      <div class="metric-grid">
        <div v-for="card in metrics" :key="card.key" class="metric">
          <div class="metric__label">{{ card.label }}</div>
          <div class="metric__value" :class="card.className">
            <span v-if="loading" class="skeleton-block metric__skeleton"></span>
            <template v-else>{{ card.prefix }}{{ card.value }}</template>
          </div>
          <div class="metric__hint text-sub">{{ card.hint }}</div>
        </div>
      </div>

      <div class="chart-grid">
        <div class="page-card">
          <div class="card-head">
            <h2 class="card-title">近 7 天销售额</h2>
            <router-link to="/admin/stats" class="card-link">更多统计</router-link>
          </div>
          <ChartPanel
            :option="salesOption"
            :loading="loading"
            :empty="!loading && salesEmpty"
            height="280px"
          />
        </div>

        <div class="page-card">
          <div class="card-head">
            <h2 class="card-title">订单状态分布</h2>
          </div>
          <ChartPanel
            :option="statusOption"
            :loading="loading"
            :empty="!loading && statusEmpty"
            height="280px"
          />
        </div>
      </div>

      <div class="page-card">
        <div class="card-head">
          <h2 class="card-title">热销商品 TOP 5</h2>
        </div>
        <div v-if="loading" class="skeleton-block dashboard__skeleton"></div>
        <el-table v-else-if="topProducts.length" :data="topProducts" size="small">
          <el-table-column type="index" label="排名" width="70" />
          <el-table-column prop="productName" label="商品" min-width="220" show-overflow-tooltip />
          <el-table-column prop="sales" label="累计销量" width="120" align="right" />
        </el-table>
        <EmptyState v-else title="暂无销量数据" desc="产生订单后这里会展示热销排行" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  fetchOrderStatusStat,
  fetchOverview,
  fetchSalesTrend,
  fetchTopProducts
} from '@/api/admin/stat'
import { formatMoney } from '@/utils/format'
import ChartPanel from '@/components/ChartPanel.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(true)
const error = ref('')
const overview = ref({})
const sales = ref({ dates: [], amounts: [] })
const statusStat = ref([])
const topProducts = ref([])

const metrics = computed(() => [
  {
    key: 'todayOrderCount',
    label: '今日订单',
    value: overview.value.todayOrderCount ?? 0,
    prefix: '',
    hint: '不含已取消订单'
  },
  {
    key: 'todaySalesAmount',
    label: '今日成交额',
    value: formatMoney(overview.value.todaySalesAmount ?? '0.00'),
    prefix: '¥',
    hint: '按支付时间统计',
    className: 'metric__value--price'
  },
  {
    key: 'pendingShipmentCount',
    label: '待发货',
    value: overview.value.pendingShipmentCount ?? 0,
    prefix: '',
    hint: '需要在订单管理中处理',
    className: 'metric__value--warning'
  },
  {
    key: 'totalProductCount',
    label: '商品总数',
    value: overview.value.totalProductCount ?? 0,
    prefix: '',
    hint: `平台注册用户 ${overview.value.totalUserCount ?? 0} 人`
  }
])

const salesEmpty = computed(() => !sales.value.amounts.some((amount) => Number(amount) > 0))
const statusEmpty = computed(() => !statusStat.value.some((item) => item.count > 0))

const salesOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 12, right: 20, top: 20, bottom: 8, containLabel: true },
  xAxis: {
    type: 'category',
    data: sales.value.dates.map((date) => String(date).slice(5)),
    axisLine: { lineStyle: { color: '#dcdfe6' } },
    axisLabel: { color: '#909399' }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#ebeef5' } },
    axisLabel: { color: '#909399' }
  },
  series: [
    {
      name: '销售额',
      type: 'line',
      smooth: true,
      symbolSize: 6,
      data: sales.value.amounts.map((amount) => Number(amount)),
      itemStyle: { color: '#409eff' },
      areaStyle: { color: 'rgba(64,158,255,0.12)' }
    }
  ]
}))

const statusOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}：{c} 单（{d}%）' },
  legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#606266' } },
  series: [
    {
      name: '订单状态',
      type: 'pie',
      radius: ['45%', '68%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      label: { show: false },
      data: statusStat.value.map((item) => ({ name: item.statusText, value: item.count }))
    }
  ]
}))

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [o, s, st, top] = await Promise.all([
      fetchOverview(),
      fetchSalesTrend(7),
      fetchOrderStatusStat(),
      fetchTopProducts(5)
    ])
    overview.value = o || {}
    sales.value = s || { dates: [], amounts: [] }
    statusStat.value = st || []
    topProducts.value = top || []
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: var(--mall-space-2);
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--mall-space-2);
}

.metric {
  background: var(--mall-bg-white);
  border-radius: var(--mall-radius);
  padding: 18px;
}

.metric__label {
  font-size: 13px;
  color: var(--mall-text-3);
}

.metric__value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 600;
  line-height: 1.2;
}

.metric__value--price {
  color: var(--mall-price);
}

.metric__value--warning {
  color: var(--mall-warning);
}

.metric__skeleton {
  display: inline-block;
  width: 80px;
  height: 26px;
}

.metric__hint {
  margin-top: 6px;
  font-size: 12px;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: var(--mall-space-2);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
}

.card-link {
  font-size: 12px;
  color: var(--mall-primary);
}

.dashboard__skeleton {
  height: 120px;
}

@media (max-width: 1100px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .metric__value {
    font-size: 22px;
  }
}
</style>
