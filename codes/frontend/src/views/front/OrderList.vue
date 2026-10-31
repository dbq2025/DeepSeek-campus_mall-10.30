<template>
  <div class="mall-container order-list">
    <div class="page-card">
      <el-tabs v-model="activeStatus" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待付款" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>
    </div>

    <div v-if="loading" class="page-card">
      <div v-for="n in 3" :key="n" class="skeleton-block order-list__skeleton"></div>
    </div>

    <div v-else-if="error" class="page-card">
      <EmptyState type="error" title="订单加载失败" :desc="error" @retry="load" />
    </div>

    <div v-else-if="!list.length" class="page-card">
      <EmptyState
        title="这里还没有订单"
        desc="下单后可以在本页查看状态、支付与确认收货"
        action-text="去逛商品"
        @action="router.push('/products')"
      />
    </div>

    <template v-else>
      <div v-for="order in list" :key="order.id" class="page-card order-card">
        <div class="order-card__head">
          <span class="order-card__no">订单号：{{ order.orderNo }}</span>
          <span class="text-sub">{{ order.createTime }}</span>
          <StatusTag class="order-card__status" :status="order.status" :text="order.statusText" />
        </div>

        <div class="order-card__items">
          <div
            v-for="item in order.items"
            :key="item.id"
            class="order-line"
            @click="router.push(`/orders/${order.id}`)"
          >
            <el-image :src="item.productImage" fit="cover" class="order-line__image">
              <template #error>
                <div class="order-line__fallback"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="order-line__info">
              <p class="order-line__name">{{ item.productName }}</p>
              <p class="text-sub order-line__spec">{{ item.specText || '默认规格' }}</p>
            </div>
            <div class="order-line__price">
              <span class="price">{{ formatMoney(item.price) }}</span>
              <span class="text-sub"> × {{ item.quantity }}</span>
            </div>
          </div>
        </div>

        <div class="order-card__foot">
          <span class="order-card__amount">
            实付：<span class="price order-card__amount-value">{{ formatMoney(order.payAmount) }}</span>
          </span>
          <div class="order-card__actions">
            <el-button size="small" @click="router.push(`/orders/${order.id}`)">查看详情</el-button>
            <!-- 动作按钮完全由后端 actions[] 驱动，前端不判断状态 -->
            <el-button
              v-for="action in order.actions"
              :key="action.action"
              size="small"
              :type="action.type"
              @click="onAction(action, order)"
            >
              {{ action.label }}
            </el-button>
          </div>
        </div>
      </div>

      <BasePager
        v-model:page="query.page"
        v-model:size="query.size"
        :total="total"
        @change="onPageChange"
      />
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchOrders, payOrder, cancelOrder, confirmOrder } from '@/api/order'
import { useListQuery } from '@/composables/useListQuery'
import { formatMoney } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'
import BasePager from '@/components/BasePager.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

const activeStatus = ref('all')

const { query, list, total, loading, error, load, setPage, setSize } = useListQuery(
  (params) => {
    const payload = { ...params }
    if (payload.status === 'all' || payload.status === '') delete payload.status
    return fetchOrders(payload)
  },
  { status: 'all' },
  { pageSize: 5 }
)

function onTabChange() {
  query.page = 1
  load()
}

function onPageChange({ page, size }) {
  if (size !== query.size) setSize(size)
  else setPage(page)
}

const ACTION_CONFIRM = {
  pay: { title: '确认支付', text: '本平台为课程演示，支付为模拟操作，确认后订单进入待发货状态。' },
  cancel: { title: '取消订单', text: '取消后库存将立即回补，确定取消该订单？' },
  confirm: { title: '确认收货', text: '确认已收到商品？确认后订单将变为已完成。' }
}

async function onAction(action, order) {
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
    if (action.action === 'pay') await payOrder(order.id)
    else if (action.action === 'cancel') await cancelOrder(order.id, '用户取消')
    else if (action.action === 'confirm') await confirmOrder(order.id)
    else return
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    // 状态已变化等错误由请求层提示，这里刷新以保证与服务端一致
    load()
  }
}
</script>

<style scoped>
.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-list > .page-card:first-child {
  padding: 4px 20px 0;
}

.order-list :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.order-list :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--mall-border-light);
}

.order-list :deep(.el-tabs__item) {
  height: 48px;
  font-size: 14px;
  color: var(--mall-text-3);
}

.order-list :deep(.el-tabs__item.is-active) {
  color: var(--mall-primary);
  font-weight: 600;
}

.order-list :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 3px;
}

.order-list__skeleton {
  height: 96px;
  margin-bottom: 12px;
}

.order-card {
  padding: 0 24px;
}

.order-card__head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 0 14px;
  border-bottom: 1px solid var(--mall-border-light);
  font-size: 13px;
  color: var(--mall-text-3);
}

.order-card__no {
  font-weight: 600;
  color: var(--mall-text-1);
  font-variant-numeric: tabular-nums;
}

.order-card__status {
  margin-left: auto;
}

.order-card__items {
  padding: 6px 0;
}

.order-line {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 8px;
  margin: 0 -8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color var(--mall-dur-fast) ease-out;
}

.order-line:hover {
  background: var(--mall-bg-subtle);
}

.order-line:hover .order-line__name {
  color: var(--mall-primary);
}

.order-line__image {
  width: 64px;
  height: 64px;
  flex: 0 0 64px;
  border-radius: 10px;
  overflow: hidden;
  background: var(--mall-bg-subtle);
  border: 1px solid var(--mall-border-light);
}

.order-line__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-text-4);
}

.order-line__info {
  flex: 1;
  min-width: 0;
}

.order-line__name {
  font-size: 14px;
  font-weight: 500;
  color: var(--mall-text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--mall-dur-fast) ease-out;
}

.order-line__spec {
  font-size: 12px;
  margin-top: 2px;
}

.order-line__price {
  white-space: nowrap;
  font-size: 13px;
}

.order-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mall-space-2);
  padding: 14px 0 16px;
  border-top: 1px solid var(--mall-border-light);
  flex-wrap: wrap;
}

.order-card__amount {
  font-size: 13px;
  color: var(--mall-text-3);
}

.order-card__amount-value {
  font-size: 22px;
  margin-left: 2px;
}

.order-card__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.order-card__actions :deep(.el-button) {
  height: 32px;
  border-radius: 8px;
}

@media (max-width: 767px) {
  .order-list {
    gap: 12px;
  }

  .order-list > .page-card:first-child {
    padding: 0 12px;
  }

  .order-card {
    padding: 0 16px;
  }

  .order-card__head {
    flex-wrap: wrap;
    gap: 4px 8px;
  }

  .order-card__status {
    margin-left: 0;
  }

  .order-line__image {
    width: 56px;
    height: 56px;
    flex: 0 0 56px;
  }

  .order-card__foot {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-card__actions {
    width: 100%;
  }
}
</style>
