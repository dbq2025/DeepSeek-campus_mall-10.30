<template>
  <div class="admin-order-detail">
    <div class="page-card">
      <div class="head">
        <div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/orders' }">订单管理</el-breadcrumb-item>
            <el-breadcrumb-item>订单详情</el-breadcrumb-item>
          </el-breadcrumb>
          <h1 class="head__no">{{ order?.orderNo || '—' }}</h1>
        </div>
        <div class="head__actions">
          <el-button @click="router.push('/admin/orders')">返回列表</el-button>
          <el-button
            v-for="action in order?.actions || []"
            :key="action.action"
            :type="action.type"
            :loading="actionLoading"
            @click="onAction(action)"
          >
            {{ action.label }}
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="page-card">
      <div class="skeleton-block detail-skeleton"></div>
      <div class="skeleton-block detail-skeleton"></div>
    </div>

    <div v-else-if="error" class="page-card">
      <EmptyState
        type="error"
        title="订单加载失败"
        :desc="error"
        action-text="返回列表"
        @action="router.push('/admin/orders')"
        @retry="load"
      />
    </div>

    <template v-else-if="order">
      <div class="page-card">
        <el-steps :active="stepActive" align-center finish-status="success">
          <el-step title="提交订单" :description="order.createTime" />
          <el-step title="完成付款" :description="order.payTime || '待付款'" />
          <el-step title="商家发货" :description="order.shipTime || '待发货'" />
          <el-step title="确认收货" :description="order.finishTime || '待收货'" />
        </el-steps>
        <el-alert
          v-if="order.status === 4"
          class="cancel-alert"
          type="info"
          :closable="false"
          show-icon
          :title="`订单已取消（${order.cancelReason || '—'}）`"
          :description="order.cancelTime || ''"
        />
      </div>

      <div class="info-grid">
        <div class="page-card">
          <h2 class="block-title">订单信息</h2>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="下单用户">{{ order.userName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <StatusTag :status="order.status" :text="order.statusText" />
            </el-descriptions-item>
            <el-descriptions-item label="物流单号">{{ order.trackingNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="买家留言">{{ order.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="page-card">
          <h2 class="block-title">收货信息</h2>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ order.receiverAddress }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div class="page-card">
        <h2 class="block-title">商品明细</h2>
        <el-table :data="order.items" size="small">
          <el-table-column label="商品" min-width="240">
            <template #default="{ row }">
              <div class="item-cell">
                <el-image :src="row.productImage" fit="cover" class="item-cell__image">
                  <template #error>
                    <div class="item-cell__fallback"><el-icon><Picture /></el-icon></div>
                  </template>
                </el-image>
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="规格" min-width="140">
            <template #default="{ row }">
              <span class="text-sub">{{ row.specText || '默认规格' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="110">
            <template #default="{ row }">
              <span class="price">{{ formatMoney(row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="price">{{ formatMoney(row.totalAmount) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="amount-box">
          <div class="amount-row">
            <span class="text-sub">商品总额</span>
            <span class="price">{{ formatMoney(order.totalAmount) }}</span>
          </div>
          <div class="amount-row amount-row--total">
            <span>实付金额</span>
            <span class="price amount-value">{{ formatMoney(order.payAmount) }}</span>
          </div>
        </div>
      </div>
    </template>

    <BaseDialog
      v-model="shipVisible"
      title="订单发货"
      :confirm-loading="actionLoading"
      width="420px"
      @confirm="confirmShip"
      @closed="trackingNo = ''"
    >
      <el-form label-width="80px">
        <el-form-item label="物流单号">
          <el-input v-model="trackingNo" maxlength="50" placeholder="选填，如 SF1234567890" />
        </el-form-item>
      </el-form>
    </BaseDialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { confirmOrder, fetchOrderById, shipOrder } from '@/api/admin/order'
import { formatMoney } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import BaseDialog from '@/components/BaseDialog.vue'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)
const error = ref('')
const actionLoading = ref(false)
const shipVisible = ref(false)
const trackingNo = ref('')

const stepActive = computed(() => {
  if (!order.value) return 0
  return { 0: 1, 1: 2, 2: 3, 3: 4 }[order.value.status] ?? 0
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    order.value = await fetchOrderById(String(route.params.id))
  } catch (e) {
    order.value = null
    error.value = e.message || '订单不存在'
  } finally {
    loading.value = false
  }
}

async function onAction(action) {
  if (action.action === 'ship') {
    trackingNo.value = order.value.trackingNo || ''
    shipVisible.value = true
    return
  }
  if (action.action === 'confirm') {
    try {
      await ElMessageBox.confirm('确认该订单已送达？确认后订单将变为已完成。', '确认收货', {
        type: 'info',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })
    } catch (e) {
      return
    }
    actionLoading.value = true
    try {
      await confirmOrder(order.value.id)
      ElMessage.success('操作成功')
      load()
    } finally {
      actionLoading.value = false
    }
  }
}

async function confirmShip() {
  actionLoading.value = true
  try {
    await shipOrder(order.value.id, trackingNo.value.trim())
    ElMessage.success('发货成功')
    shipVisible.value = false
    load()
  } catch (e) {
    // 请求层已提示
  } finally {
    actionLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-order-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-order-detail > .page-card {
  padding: 20px 24px;
}

.head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--mall-space-2);
  flex-wrap: wrap;
}

.head__no {
  margin-top: 8px;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.01em;
  font-variant-numeric: tabular-nums;
  color: var(--mall-text-1);
}

.head__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.head__actions :deep(.el-button) {
  height: 36px;
  border-radius: 8px;
}

.detail-skeleton {
  height: 60px;
  margin-bottom: 12px;
}

.cancel-alert {
  margin-top: 16px;
  border-radius: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-cell__image {
  width: 44px;
  height: 44px;
  flex: 0 0 44px;
  border-radius: 10px;
  overflow: hidden;
  background: var(--mall-bg-subtle);
  border: 1px solid var(--mall-border-light);
}

.item-cell__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-text-4);
}

.amount-box {
  margin-top: 20px;
  margin-left: auto;
  width: 320px;
  padding: 16px 20px;
  background: var(--mall-bg-subtle);
  border-radius: var(--mall-radius);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  gap: var(--mall-space-2);
}

.amount-row--total {
  align-items: baseline;
  border-top: 1px dashed var(--mall-border);
  padding-top: 10px;
}

.amount-value {
  font-size: 24px;
}

@media (max-width: 991px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .amount-box {
    width: 100%;
  }
}

@media (max-width: 767px) {
  .admin-order-detail > .page-card {
    padding: 16px;
  }
}
</style>
