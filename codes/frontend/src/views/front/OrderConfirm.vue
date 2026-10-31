<template>
  <div class="mall-container confirm">
    <div class="page-card">
      <h1 class="page-title">确认订单</h1>

      <div v-if="loading">
        <div class="skeleton-block confirm__skeleton"></div>
        <div class="skeleton-block confirm__skeleton"></div>
      </div>

      <EmptyState
        v-else-if="error"
        type="error"
        title="订单信息加载失败"
        :desc="error"
        action-text="返回购物车"
        @action="router.push('/cart')"
        @retry="load"
      />

      <EmptyState
        v-else-if="!lines.length"
        title="没有待结算的商品"
        desc="请先在购物车勾选商品，或从商品详情页点击「立即购买」"
        action-text="去购物车"
        @action="router.push('/cart')"
      />
    </div>

    <template v-if="!loading && !error && lines.length">
      <div class="page-card">
        <h2 class="confirm__title">收货信息</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="confirm__form">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="收货人" prop="receiverName">
                <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" maxlength="50" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="手机号" prop="receiverPhone">
                <el-input v-model="form.receiverPhone" placeholder="11 位手机号" maxlength="11" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="收货地址" prop="receiverAddress">
            <el-input
              v-model="form.receiverAddress"
              type="textarea"
              :rows="2"
              maxlength="255"
              show-word-limit
              placeholder="省 / 市 / 区 + 详细地址"
            />
          </el-form-item>
          <el-form-item label="买家留言" prop="remark">
            <el-input v-model="form.remark" maxlength="200" show-word-limit placeholder="选填，如配送时间要求" />
          </el-form-item>
        </el-form>
      </div>

      <div class="page-card">
        <h2 class="confirm__title">商品清单</h2>
        <el-table :data="lines" size="small">
          <el-table-column label="商品" min-width="260">
            <template #default="{ row }">
              <div class="confirm-item">
                <el-image :src="row.productImage || row.mainImage" fit="cover" class="confirm-item__image">
                  <template #error>
                    <div class="confirm-item__fallback"><el-icon><Picture /></el-icon></div>
                  </template>
                </el-image>
                <div>
                  <p class="confirm-item__name">{{ row.productName }}</p>
                  <p class="text-sub confirm-item__spec">{{ row.specText || '默认规格' }}</p>
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
              <span class="price">{{ multiplyMoney(row.price, row.quantity) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="page-card confirm__bar">
        <div class="confirm__summary">
          <span class="text-sub">共 {{ totalQuantity }} 件</span>
          <span class="confirm__amount">
            应付金额：<span class="price confirm__amount-value">{{ previewAmount }}</span>
          </span>
        </div>
        <div class="confirm__actions">
          <el-button @click="router.back()">返回</el-button>
          <el-button type="primary" size="large" :loading="submitting" @click="onSubmit">
            提交订单
          </el-button>
        </div>
      </div>
      <p class="text-sub confirm__hint">
        提交后库存将被锁定，30 分钟内未支付订单会自动取消并回补库存。
      </p>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCart } from '@/api/cart'
import { fetchProductDetail } from '@/api/product'
import { createOrder } from '@/api/order'
import { formatMoney, multiplyMoney, sumMoney } from '@/utils/format'
import { checkPhone } from '@/utils/validate'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const cartStore = useCartStore()

const ADDRESS_KEY = 'mall_last_address'

const formRef = ref(null)
const loading = ref(true)
const error = ref('')
const submitting = ref(false)
const lines = ref([])
const fromCart = ref(false)
const cartIds = ref([])

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  remark: ''
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [{ validator: checkPhone, trigger: 'blur' }],
  receiverAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const totalQuantity = computed(() => lines.value.reduce((acc, row) => acc + Number(row.quantity || 0), 0))
const previewAmount = computed(() => sumMoney(lines.value.map((row) => multiplyMoney(row.price, row.quantity))))

function fillReceiver() {
  form.receiverName = user.userInfo?.nickname || ''
  form.receiverPhone = user.userInfo?.phone || ''
  try {
    const saved = JSON.parse(localStorage.getItem(ADDRESS_KEY) || 'null')
    if (saved) {
      form.receiverName = saved.receiverName || form.receiverName
      form.receiverPhone = saved.receiverPhone || form.receiverPhone
      form.receiverAddress = saved.receiverAddress || ''
    }
  } catch (e) {
    // 本地地址解析失败时忽略，按空表单处理
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const rawCartIds = String(route.query.cartIds || '')
    if (rawCartIds) {
      fromCart.value = true
      cartIds.value = rawCartIds.split(',').filter(Boolean)
      const data = await fetchCart()
      const all = data?.items || []
      const chosen = all.filter((row) => cartIds.value.includes(row.id))
      lines.value = chosen.map((row) => ({
        productId: row.productId,
        productName: row.productName,
        productImage: row.mainImage,
        price: row.price,
        quantity: row.quantity,
        specText: row.specText
      }))
    } else {
      fromCart.value = false
      const productId = String(route.query.productId || '')
      if (!productId) {
        lines.value = []
        return
      }
      const product = await fetchProductDetail(productId)
      lines.value = [
        {
          productId: product.id,
          productName: product.name,
          productImage: product.mainImage,
          price: product.price,
          quantity: Number(route.query.quantity) || 1,
          specText: String(route.query.specText || '')
        }
      ]
    }
  } catch (e) {
    error.value = e.message || '加载失败'
    lines.value = []
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (submitting.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      fromCart: fromCart.value,
      receiverName: form.receiverName.trim(),
      receiverPhone: form.receiverPhone.trim(),
      receiverAddress: form.receiverAddress.trim(),
      remark: form.remark.trim()
    }
    if (fromCart.value) {
      payload.cartIds = cartIds.value
    } else {
      payload.items = lines.value.map((row) => ({
        productId: row.productId,
        quantity: row.quantity,
        specText: row.specText
      }))
    }

    const result = await createOrder(payload)
    localStorage.setItem(
      ADDRESS_KEY,
      JSON.stringify({
        receiverName: payload.receiverName,
        receiverPhone: payload.receiverPhone,
        receiverAddress: payload.receiverAddress
      })
    )
    await cartStore.refresh()
    ElMessage.success('下单成功，请尽快完成支付')
    router.replace(`/orders/${result.orderId}`)
  } catch (e) {
    // 库存不足/商品下架等由请求层提示，停留在本页供用户调整
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fillReceiver()
  load()
})
</script>

<style scoped>
.confirm {
  display: flex;
  flex-direction: column;
  gap: var(--mall-space-2);
}

.confirm__title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: var(--mall-space-2);
}

.confirm__form {
  max-width: 760px;
}

.confirm__skeleton {
  height: 80px;
  margin-bottom: 12px;
}

.confirm-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.confirm-item__image {
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  border-radius: var(--mall-radius-sm);
  overflow: hidden;
  background: var(--mall-bg);
}

.confirm-item__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-text-4);
}

.confirm-item__name {
  font-size: 13px;
}

.confirm-item__spec {
  font-size: 12px;
}

.confirm__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mall-space-2);
  flex-wrap: wrap;
}

.confirm__summary {
  display: flex;
  align-items: baseline;
  gap: var(--mall-space-2);
}

.confirm__amount-value {
  font-size: 22px;
}

.confirm__actions {
  display: flex;
  gap: 10px;
}

.confirm__hint {
  text-align: right;
  font-size: 12px;
}

@media (max-width: 767px) {
  .confirm__bar {
    flex-direction: column;
    align-items: stretch;
  }

  .confirm__actions {
    justify-content: flex-end;
  }

  .confirm__hint {
    text-align: left;
  }
}
</style>
