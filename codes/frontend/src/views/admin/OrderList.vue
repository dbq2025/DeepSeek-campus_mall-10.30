<template>
  <div class="admin-order">
    <div class="page-card">
      <div class="toolbar">
        <el-input
          v-model="query.orderNo"
          placeholder="按订单号搜索"
          clearable
          style="width: 220px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="待付款" :value="0" />
          <el-option label="待发货" :value="1" />
          <el-option label="待收货" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
        <span class="toolbar__right text-sub">共 {{ total }} 笔订单</span>
      </div>

      <BaseTable
        :columns="columns"
        :data="list"
        :loading="loading"
        :error="error"
        empty-text="暂无订单"
        :action-width="150"
        @retry="load"
      >
        <template #orderNo="{ row }">
          <span class="order-no">{{ row.orderNo }}</span>
        </template>

        <template #items="{ row }">
          <span class="text-sub">
            {{ row.items[0]?.productName || '—' }}
            <template v-if="row.items.length > 1">等 {{ row.items.length }} 件</template>
          </span>
        </template>

        <template #payAmount="{ row }">
          <span class="price">{{ formatMoney(row.payAmount) }}</span>
        </template>

        <template #status="{ row }">
          <StatusTag :status="row.status" :text="row.statusText" />
        </template>

        <template #createTime="{ row }">
          <span class="text-sub">{{ formatDateTime(row.createTime) }}</span>
        </template>

        <template #actions="{ row }">
          <el-button link type="primary" @click="router.push(`/admin/orders/${row.id}`)">
            详情
          </el-button>
          <el-button
            v-for="action in row.actions"
            :key="action.action"
            link
            type="primary"
            @click="onAction(action, row)"
          >
            {{ action.label }}
          </el-button>
        </template>
      </BaseTable>

      <BasePager
        v-model:page="query.page"
        v-model:size="query.size"
        :total="total"
        @change="onPageChange"
      />
    </div>

    <BaseDialog
      v-model="shipVisible"
      title="订单发货"
      :confirm-loading="shipping"
      width="420px"
      @confirm="confirmShip"
      @closed="trackingNo = ''"
    >
      <p class="text-sub ship__tip">订单号：{{ currentOrder?.orderNo }}</p>
      <el-form label-width="80px">
        <el-form-item label="物流单号">
          <el-input v-model="trackingNo" maxlength="50" placeholder="选填，如 SF1234567890" />
        </el-form-item>
      </el-form>
      <p class="text-sub ship__tip">确认后订单状态变为「待收货」，用户可确认收货。</p>
    </BaseDialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, Search } from '@element-plus/icons-vue'
import { confirmOrder, fetchOrderPage, shipOrder } from '@/api/admin/order'
import { useListQuery } from '@/composables/useListQuery'
import { formatDateTime, formatMoney } from '@/utils/format'
import BaseTable from '@/components/BaseTable.vue'
import BasePager from '@/components/BasePager.vue'
import BaseDialog from '@/components/BaseDialog.vue'
import StatusTag from '@/components/StatusTag.vue'

const router = useRouter()

const columns = [
  { prop: 'orderNo', label: '订单号', width: 190, slot: 'orderNo' },
  { prop: 'userName', label: '下单用户', width: 110 },
  { prop: 'items', label: '商品', minWidth: 220, slot: 'items' },
  { prop: 'payAmount', label: '金额', width: 110, slot: 'payAmount' },
  { prop: 'status', label: '状态', width: 100, align: 'center', slot: 'status' },
  { prop: 'createTime', label: '下单时间', width: 170, slot: 'createTime' }
]

const { query, list, total, loading, error, load, search, reset, setPage, setSize } = useListQuery(
  (params) => fetchOrderPage(params),
  { orderNo: '', status: '' }
)

const shipVisible = ref(false)
const shipping = ref(false)
const trackingNo = ref('')
const currentOrder = ref(null)

function onReset() {
  query.orderNo = ''
  query.status = ''
  reset()
}

function onPageChange({ page, size }) {
  if (size !== query.size) setSize(size)
  else setPage(page)
}

function onAction(action, row) {
  if (action.action === 'ship') {
    currentOrder.value = row
    trackingNo.value = row.trackingNo || ''
    shipVisible.value = true
    return
  }
  if (action.action === 'confirm') {
    ElMessageBox.confirm('确认该订单已送达？确认后订单将变为已完成。', '确认收货', {
      type: 'info',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
      .then(async () => {
        await confirmOrder(row.id)
        ElMessage.success('操作成功')
        load()
      })
      .catch(() => {})
  }
}

async function confirmShip() {
  if (!currentOrder.value) return
  shipping.value = true
  try {
    await shipOrder(currentOrder.value.id, trackingNo.value.trim())
    ElMessage.success('发货成功')
    shipVisible.value = false
    load()
  } catch (e) {
    // 请求层已提示
  } finally {
    shipping.value = false
  }
}
</script>

<style scoped>
.admin-order {
  display: flex;
  flex-direction: column;
}

.admin-order .page-card {
  padding: 20px 24px;
}

.order-no {
  font-size: 12px;
  color: var(--mall-text-2);
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.01em;
}

.ship__tip {
  font-size: 12px;
  margin-bottom: 10px;
  color: var(--mall-text-3);
}

@media (max-width: 767px) {
  .admin-order .page-card {
    padding: 14px 12px;
  }
}
</style>
