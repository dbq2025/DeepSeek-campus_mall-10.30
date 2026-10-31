<template>
  <div class="mall-container cart">
    <div class="page-card toolbar">
      <h1 class="page-title cart__title">购物车</h1>
      <span class="toolbar__right text-sub">共 {{ cartData.totalQuantity }} 件商品</span>
    </div>

    <div v-if="loading" class="page-card">
      <div class="skeleton-block cart__skeleton"></div>
      <div class="skeleton-block cart__skeleton"></div>
      <div class="skeleton-block cart__skeleton"></div>
    </div>

    <div v-else-if="error" class="page-card">
      <EmptyState type="error" title="购物车加载失败" :desc="error" @retry="load" />
    </div>

    <div v-else-if="!items.length" class="page-card">
      <EmptyState
        title="购物车还是空的"
        desc="挑几件喜欢的商品，加进来一起结算"
        action-text="去逛逛"
        @action="router.push('/products')"
      />
    </div>

    <template v-else>
      <div class="page-card cart__table-card">
        <el-table
          ref="tableRef"
          :data="items"
          row-key="id"
          size="default"
          @selection-change="onSelectionChange"
        >
          <el-table-column type="selection" width="46" :selectable="isSelectable" />

          <el-table-column label="商品" min-width="280">
            <template #default="{ row }">
              <div class="cart-item">
                <el-image :src="row.mainImage" fit="cover" class="cart-item__image">
                  <template #error>
                    <div class="cart-item__fallback"><el-icon><Picture /></el-icon></div>
                  </template>
                </el-image>
                <div class="cart-item__info">
                  <router-link :to="`/products/${row.productId}`" class="cart-item__name">
                    {{ row.productName }}
                  </router-link>
                  <p class="text-sub cart-item__spec">
                    {{ row.specText || '默认规格' }}
                  </p>
                  <el-tag v-if="row.productStatus !== 1" type="info" size="small">已下架</el-tag>
                  <el-tag v-else-if="row.stock === 0" type="danger" size="small">已售罄</el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="单价" width="110">
            <template #default="{ row }">
              <span class="price">{{ formatMoney(row.price) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="数量" width="150">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="Math.max(row.stock, 1)"
                size="small"
                :disabled="row.productStatus !== 1"
                @change="(value) => onQuantityChange(row, value)"
              />
            </template>
          </el-table-column>

          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="price">{{ multiplyMoney(row.price, row.quantity) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="90" align="center">
            <template #default="{ row }">
              <el-button link type="danger" @click="onRemove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="page-card cart__bar">
        <div class="cart__bar-left">
          <el-checkbox :model-value="allSelected" @change="toggleAll">全选</el-checkbox>
          <el-button link @click="onClear">清空购物车</el-button>
        </div>
        <div class="cart__bar-right">
          <span class="text-sub">已选 {{ selected.length }} 件</span>
          <span class="cart__total">
            合计：<span class="price cart__total-price">{{ selectedAmount }}</span>
          </span>
          <el-button type="primary" size="large" :disabled="!selected.length" @click="onCheckout">
            去结算
          </el-button>
        </div>
      </div>
      <p class="cart__hint text-sub">
        合计金额仅为前端预览，最终以创建订单接口返回的 payAmount 为准。
      </p>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchCart, updateCartItem, removeCartItem, clearCart } from '@/api/cart'
import { formatMoney, multiplyMoney, sumMoney } from '@/utils/format'
import { useCartStore } from '@/stores/cart'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const cartStore = useCartStore()

const tableRef = ref(null)
const loading = ref(true)
const error = ref('')
const cartData = ref({ items: [], totalQuantity: 0, totalAmount: '0.00' })
const selected = ref([])
/** 已成功落库的数量，用于接口失败时回滚输入框 */
const persistedQty = new Map()

const items = computed(() => cartData.value.items || [])

const selectedAmount = computed(() =>
  sumMoney(selected.value.map((row) => multiplyMoney(row.price, row.quantity)))
)

const allSelected = computed(
  () => items.value.length > 0 && selected.value.length === items.value.filter(isSelectable).length
)

function isSelectable(row) {
  return row.productStatus === 1 && row.stock > 0
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchCart()
    cartData.value = data || { items: [], totalQuantity: 0, totalAmount: '0.00' }
    cartStore.setCount(cartData.value.totalQuantity)
    persistedQty.clear()
    items.value.forEach((row) => persistedQty.set(row.id, row.quantity))
    await nextTick()
    // 默认勾选所有可结算行
    items.value.forEach((row) => {
      if (isSelectable(row)) tableRef.value?.toggleRowSelection(row, true)
    })
  } catch (e) {
    error.value = e.message || '加载失败'
    cartData.value = { items: [], totalQuantity: 0, totalAmount: '0.00' }
  } finally {
    loading.value = false
  }
}

function onSelectionChange(rows) {
  selected.value = rows
}

function toggleAll(value) {
  items.value.forEach((row) => {
    if (isSelectable(row)) tableRef.value?.toggleRowSelection(row, value)
  })
}

async function onQuantityChange(row, value) {
  if (!value || value < 1) {
    row.quantity = 1
    return
  }
  const previous = persistedQty.get(row.id) || 1
  try {
    await updateCartItem(row.id, value)
    persistedQty.set(row.id, value)
    cartStore.setCount(items.value.reduce((acc, item) => acc + Number(item.quantity), 0))
  } catch (e) {
    // 失败（含库存不足 3001）回到上一次成功的数量
    row.quantity = previous
  }
}

async function onRemove(row) {
  try {
    await ElMessageBox.confirm(`确定从购物车删除「${row.productName}」？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await removeCartItem(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    // 请求层已提示
  }
}

async function onClear() {
  try {
    await ElMessageBox.confirm('清空购物车后不可恢复，确定继续？', '清空确认', {
      type: 'warning',
      confirmButtonText: '清空',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await clearCart()
    ElMessage.success('购物车已清空')
    cartStore.reset()
    load()
  } catch (e) {
    // 请求层已提示
  }
}

function onCheckout() {
  if (!selected.value.length) {
    ElMessage.warning('请先勾选要结算的商品')
    return
  }
  const cartIds = selected.value.map((row) => row.id).join(',')
  router.push({ path: '/orders/confirm', query: { cartIds } })
}

onMounted(load)
</script>

<style scoped>
.cart {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cart .page-card.toolbar {
  margin-bottom: 0;
  padding: 18px 24px;
}

.cart__title {
  margin-bottom: 0;
}

.cart__table-card {
  padding: 8px 12px 4px;
}

.cart__skeleton {
  height: 72px;
  margin-bottom: 12px;
}

.cart-item {
  display: flex;
  gap: 14px;
  align-items: center;
}

.cart-item__image {
  width: 64px;
  height: 64px;
  flex: 0 0 64px;
  border-radius: 10px;
  overflow: hidden;
  background: var(--mall-bg-subtle);
  border: 1px solid var(--mall-border-light);
}

.cart-item__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-text-4);
}

.cart-item__info {
  min-width: 0;
}

.cart-item__name {
  font-weight: 500;
  color: var(--mall-text-1);
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--mall-dur-fast) ease-out;
}

.cart-item__name:hover {
  color: var(--mall-primary);
}

.cart-item__spec {
  font-size: 12px;
  margin: 2px 0 4px;
}

.cart__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mall-space-2);
  padding: 16px 24px;
  position: sticky;
  bottom: 16px;
  z-index: 5;
  box-shadow: var(--mall-shadow-pop);
}

.cart__bar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cart__bar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cart__total {
  font-size: 14px;
  color: var(--mall-text-2);
}

.cart__total-price {
  font-size: 28px;
  margin-left: 4px;
}

.cart__bar-right :deep(.el-button--primary) {
  height: 44px;
  padding: 0 28px;
  border-radius: 10px;
  font-size: 15px;
  box-shadow: var(--mall-shadow-brand);
}

.cart__hint {
  text-align: right;
  font-size: 12px;
  margin-top: -8px;
}

@media (max-width: 767px) {
  .cart {
    gap: 14px;
  }

  .cart .page-card.toolbar {
    padding: 14px 16px;
  }

  .cart__table-card {
    padding: 6px 8px 2px;
    overflow-x: auto;
  }

  .cart__bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 14px 16px;
    bottom: 8px;
  }

  .cart__bar-right {
    justify-content: space-between;
    gap: 10px;
  }

  .cart__total-price {
    font-size: 24px;
  }

  .cart__hint {
    text-align: left;
    margin-top: -4px;
  }
}
</style>
