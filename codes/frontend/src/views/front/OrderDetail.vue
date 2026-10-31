<template>
  <div class="mall-container order-detail">
    <div class="page-card">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/orders' }">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-if="loading" class="page-card">
      <div class="skeleton-block order-detail__skeleton"></div>
      <div class="skeleton-block order-detail__skeleton"></div>
    </div>

    <div v-else-if="error" class="page-card">
      <EmptyState
        type="error"
        title="订单加载失败"
        :desc="error"
        action-text="返回订单列表"
        @action="router.push('/orders')"
        @retry="load"
      />
    </div>

    <template v-else-if="order">
      <div class="page-card">
        <div class="status-bar">
          <div>
            <h1 class="status-bar__text">{{ order.statusText }}</h1>
            <p class="text-sub status-bar__no">订单号 {{ order.orderNo }} · 下单时间 {{ order.createTime }}</p>
          </div>
          <div class="status-bar__actions">
            <el-button
              v-for="action in order.actions"
              :key="action.action"
              :type="action.type"
              @click="onAction(action)"
            >
              {{ action.label }}
            </el-button>
          </div>
        </div>

        <el-alert
          v-if="order.status === 4"
          class="status-bar__alert"
          type="info"
          :closable="false"
          show-icon
          :title="`订单已取消（${order.cancelReason || '用户取消'}），库存已回补`"
          :description="order.cancelTime || ''"
        />

        <el-steps v-else class="status-bar__steps" :active="stepActive" align-center finish-status="success">
          <el-step title="提交订单" :description="order.createTime" />
          <el-step title="完成付款" :description="order.payTime || '待付款'" />
          <el-step title="商家发货" :description="order.shipTime || '待发货'" />
          <el-step title="确认收货" :description="order.finishTime || '待收货'" />
        </el-steps>
      </div>

      <div class="page-card">
        <h2 class="block-title">收货信息</h2>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ order.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="买家留言" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="物流单号" :span="2">
            {{ order.trackingNo || '尚未发货' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="page-card">
        <h2 class="block-title">商品清单</h2>
        <el-table :data="order.items" size="small">
          <el-table-column label="商品" min-width="260">
            <template #default="{ row }">
              <div class="order-item">
                <el-image :src="row.productImage" fit="cover" class="order-item__image">
                  <template #error>
                    <div class="order-item__fallback"><el-icon><Picture /></el-icon></div>
                  </template>
                </el-image>
                <div>
                  <p class="order-item__name">{{ row.productName }}</p>
                  <p class="text-sub order-item__spec">{{ row.specText || '默认规格' }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="110">
            <template #default="{ row }">
              <span class="price">{{ formatMoney(row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="80" prop="quantity" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="price">{{ formatMoney(row.totalAmount) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="amount-box">
          <div class="amount-box__row">
            <span class="text-sub">商品总额</span>
            <span class="price">{{ formatMoney(order.totalAmount) }}</span>
          </div>
          <div class="amount-box__row amount-box__row--total">
            <span>实付金额</span>
            <span class="price amount-box__value">{{ formatMoney(order.payAmount) }}</span>
          </div>
        </div>
      </div>

      <div class="order-detail__footer">
        <el-button @click="router.push('/orders')">返回订单列表</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchOrderDetail, payOrder, cancelOrder, confirmOrder } from '@/api/order'
import { formatMoney } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)
const error = ref('')

const stepActive = computed(() => {
  if (!order.value) return 0
  return { 0: 1, 1: 2, 2: 3, 3: 4 }[order.value.status] ?? 0
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    order.value = await fetchOrderDetail(String(route.params.id))
  } catch (e) {
    order.value = null
    error.value = e.message || '订单不存在'
  } finally {
    loading.value = false
  }
}

const ACTION_CONFIRM = {
  pay: { title: '确认支付', text: '本平台为课程演示，支付为模拟操作，确认后订单进入待发货状态。' },
  cancel: { title: '取消订单', text: '取消后库存将立即回补，确定取消该订单？' },
  confirm: { title: '确认收货', text: '确认已收到商品？确认后订单将变为已完成。' }
}

async function onAction(action) {
  const meta = ACTION_CONFIRM[action.action]
  if (meta) {
    try {
      await ElMessageBox.confirm(meta.text, meta.title, {
        confirmButtonText: '确定',
        cancelButtonText: '再想想',
        type: action.action === 'cancel' ? 'warning' : 'info'
      })
    } catch (e) {
      return
    }
  }
  try {
    if (action.action === 'pay') await payOrder(order.value.id)
    else if (action.action === 'cancel') await cancelOrder(order.value.id, '用户取消')
    else if (action.action === 'confirm') await confirmOrder(order.value.id)
    else return
    ElMessage.success('操作成功')
  } catch (e) {
    // 请求层已提示
  }
  load()
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.order-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-detail > .page-card:first-child {
  padding: 14px 24px;
  font-size: 13px;
}

.order-detail :deep(.el-breadcrumb__inner) {
  color: var(--mall-text-3);
  font-weight: 400;
}

.order-detail__skeleton {
  height: 72px;
  margin-bottom: 12px;
}

.status-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--mall-space-2);
  flex-wrap: wrap;
}

.status-bar__text {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--mall-primary);
}

.status-bar__no {
  margin-top: 6px;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.status-bar__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.status-bar__actions :deep(.el-button) {
  height: 36px;
  border-radius: 8px;
}

.status-bar__alert {
  margin-top: 20px;
  border-radius: 10px;
}

.status-bar__steps {
  margin-top: var(--mall-space-4);
}

.block-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 16px;
}

.order-detail :deep(.el-descriptions__body) {
  border-radius: var(--mall-radius);
  overflow: hidden;
}

.order-detail :deep(.el-descriptions__label) {
  width: 110px;
  background: var(--mall-bg-subtle);
  color: var(--mall-text-3);
  font-weight: 500;
}

.order-detail :deep(.el-descriptions__cell) {
  padding: 12px 16px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-item__image {
  width: 56px;
  height: 56px;
  flex: 0 0 56px;
  border-radius: 10px;
  overflow: hidden;
  background: var(--mall-bg-subtle);
  border: 1px solid var(--mall-border-light);
}

.order-item__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-text-4);
}

.order-item__name {
  font-size: 14px;
  font-weight: 500;
  color: var(--mall-text-1);
}

.order-item__spec {
  font-size: 12px;
  margin-top: 2px;
}

.amount-box {
  margin-top: 20px;
  margin-left: auto;
  width: 320px;
  padding: 16px 20px;
  border-radius: var(--mall-radius);
  background: var(--mall-bg-subtle);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-box__row {
  display: flex;
  gap: var(--mall-space-2);
  justify-content: space-between;
}

.amount-box__row--total {
  align-items: baseline;
  border-top: 1px dashed var(--mall-border);
  padding-top: 10px;
  margin-top: 2px;
}

.amount-box__value {
  font-size: 26px;
}

.order-detail__footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 767px) {
  .order-detail {
    gap: 14px;
  }

  .order-detail > .page-card:first-child {
    padding: 12px 16px;
  }

  .status-bar {
    flex-direction: column;
  }

  .status-bar__text {
    font-size: 20px;
  }

  .status-bar__steps {
    display: none;
  }

  .amount-box {
    width: 100%;
  }
}
</style>
