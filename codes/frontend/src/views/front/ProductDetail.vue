<template>
  <div class="mall-container detail">
    <el-breadcrumb separator="/" class="detail__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/products' }">全部商品</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product?.name || '商品详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 加载骨架 -->
    <div v-if="loading" class="page-card detail__skeleton">
      <div class="skeleton-block skeleton-block--image"></div>
      <div class="detail__skeleton-info">
        <div class="skeleton-block skeleton-block--title"></div>
        <div class="skeleton-block skeleton-block--line"></div>
        <div class="skeleton-block skeleton-block--price"></div>
        <div class="skeleton-block skeleton-block--line"></div>
        <div class="skeleton-block skeleton-block--line"></div>
      </div>
    </div>

    <!-- 错误/下架 -->
    <div v-else-if="error" class="page-card">
      <EmptyState
        type="error"
        :title="errorTitle"
        :desc="error"
        action-text="返回商品列表"
        @action="router.push('/products')"
        @retry="load"
      />
    </div>

    <!-- 正常 -->
    <div v-else-if="product" class="page-card detail__main">
      <div class="detail__gallery">
        <el-image :src="product.mainImage" fit="cover" class="detail__image">
          <template #error>
            <div class="detail__fallback">
              <el-icon :size="40"><Picture /></el-icon>
            </div>
          </template>
        </el-image>
      </div>

      <div class="detail__info">
        <h1 class="detail__name">{{ product.name }}</h1>
        <p class="detail__subtitle">{{ product.subtitle }}</p>

        <div class="detail__price-box">
          <span class="detail__price price">{{ formatMoney(product.price) }}</span>
          <span class="text-sub">已售 {{ product.sales }} 件</span>
          <span class="text-sub">库存 {{ product.stock }} 件</span>
        </div>

        <div v-if="product.specJson" class="detail__row">
          <SpecSelector
            ref="specRef"
            v-model="specText"
            :spec-json="product.specJson"
          />
        </div>

        <div v-if="product.specJson" class="detail__divider"></div>

        <div class="detail__row detail__row--inline">
          <span class="detail__label">数量</span>
          <el-input-number
            v-model="quantity"
            :min="1"
            :max="Math.max(product.stock, 1)"
            :disabled="product.stock === 0"
          />
          <span v-if="product.stock > 0 && product.stock <= 5" class="text-sub detail__stock-tip">
            仅剩 {{ product.stock }} 件
          </span>
          <span v-if="product.stock === 0" class="detail__stock-tip detail__stock-tip--empty">
            该商品暂时缺货
          </span>
        </div>

        <div class="detail__actions">
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            :disabled="product.stock === 0"
            @click="onAddCart"
          >
            加入购物车
          </el-button>
          <el-button
            type="danger"
            size="large"
            plain
            :disabled="product.stock === 0"
            @click="onBuyNow"
          >
            立即购买
          </el-button>
          <el-button
            size="large"
            :type="product.favorited ? 'warning' : 'default'"
            :loading="favoriteLoading"
            @click="toggleFavorite"
          >
            <el-icon class="detail__fav-icon"><StarFilled v-if="product.favorited" /><Star v-else /></el-icon>
            {{ product.favorited ? '已收藏' : '收藏' }}
          </el-button>
        </div>

        <ul class="detail__tips">
          <li><el-icon><CircleCheck /></el-icon> 正品保障，7 天无理由退换</li>
          <li><el-icon><Van /></el-icon> 校内仓储发货，工作日 16:00 前付款当天出库</li>
        </ul>
      </div>
    </div>

    <div v-if="product" class="page-card detail__desc">
      <h2 class="detail__desc-title">商品详情</h2>
      <p class="detail__desc-body">{{ product.detail || '商家暂未填写详情' }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchProductDetail } from '@/api/product'
import { addToCart as apiAddToCart } from '@/api/cart'
import { addFavorite, removeFavorite } from '@/api/favorite'
import { formatMoney } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import SpecSelector from '@/components/SpecSelector.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const cart = useCartStore()

const product = ref(null)
const loading = ref(true)
const error = ref('')
const errorTitle = ref('商品加载失败')
const quantity = ref(1)
const specText = ref('')
const specRef = ref(null)
const submitting = ref(false)
const favoriteLoading = ref(false)

const productId = computed(() => String(route.params.id))

const specComplete = computed(() => !product.value?.specJson || Boolean(specText.value))

function requireLogin() {
  if (user.isLogin) return true
  ElMessage.warning('请先登录')
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    product.value = await fetchProductDetail(productId.value)
  } catch (e) {
    product.value = null
    error.value = e.message || '商品不存在或已下架'
    errorTitle.value = e.code === 3005 ? '商品已下架' : '商品加载失败'
  } finally {
    loading.value = false
  }
}

function validateBeforeTrade() {
  if (!product.value) return false
  if (product.value.stock === 0) {
    ElMessage.warning('该商品暂时缺货')
    return false
  }
  if (!specComplete.value) {
    ElMessage.warning('请选择完整的商品规格')
    return false
  }
  if (quantity.value > product.value.stock) {
    ElMessage.warning(`库存不足，仅剩 ${product.value.stock} 件`)
    return false
  }
  return true
}

async function onAddCart() {
  if (!requireLogin()) return
  if (!validateBeforeTrade()) return
  submitting.value = true
  try {
    await apiAddToCart({
      productId: product.value.id,
      quantity: quantity.value,
      specText: specText.value
    })
    cart.increase(quantity.value)
    ElMessage.success('已加入购物车')
  } catch (e) {
    // 请求层已提示
  } finally {
    submitting.value = false
  }
}

function onBuyNow() {
  if (!requireLogin()) return
  if (!validateBeforeTrade()) return
  router.push({
    path: '/orders/confirm',
    query: {
      productId: product.value.id,
      quantity: String(quantity.value),
      specText: specText.value
    }
  })
}

async function toggleFavorite() {
  if (!requireLogin()) return
  favoriteLoading.value = true
  try {
    if (product.value.favorited) {
      await removeFavorite(product.value.id)
      product.value.favorited = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(product.value.id)
      product.value.favorited = true
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    // 请求层已提示
  } finally {
    favoriteLoading.value = false
  }
}

// 换商品时重置交互状态
watch(
  () => route.params.id,
  () => {
    quantity.value = 1
    specText.value = ''
    load()
  }
)

onMounted(load)
</script>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: var(--mall-space-2);
}

.detail__crumb {
  padding: 0 4px;
}

.detail__main {
  display: flex;
  gap: var(--mall-space-3);
}

.detail__gallery {
  flex: 0 0 380px;
}

.detail__image {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: var(--mall-radius);
  overflow: hidden;
  background: var(--mall-bg);
}

.detail__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-text-4);
}

.detail__info {
  flex: 1;
  min-width: 0;
}

.detail__name {
  font-size: 22px;
  font-weight: 600;
  line-height: 1.4;
}

.detail__subtitle {
  margin-top: 6px;
  color: var(--mall-text-3);
}

.detail__price-box {
  margin-top: 16px;
  background: #fff7f7;
  border-radius: var(--mall-radius);
  padding: 12px 16px;
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.detail__price {
  font-size: 28px;
}

.detail__row {
  margin-top: 18px;
}

.detail__row--inline {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail__label {
  color: var(--mall-text-3);
  font-size: 13px;
  flex: 0 0 56px;
}

.detail__stock-tip {
  color: var(--mall-warning);
}

.detail__stock-tip--empty {
  color: var(--mall-danger);
}

.detail__divider {
  height: 1px;
  background: var(--mall-border-light);
  margin: 18px 0 0;
}

.detail__actions {
  margin-top: var(--mall-space-3);
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.detail__fav-icon {
  margin-right: 4px;
}

.detail__tips {
  margin-top: var(--mall-space-2);
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--mall-text-3);
  font-size: 13px;
}

.detail__tips li {
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail__desc-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.detail__desc-body {
  white-space: pre-line;
  color: var(--mall-text-2);
  line-height: 1.9;
}

/* 骨架 */
.detail__skeleton {
  display: flex;
  gap: var(--mall-space-3);
}

.skeleton-block--image {
  width: 380px;
  aspect-ratio: 1 / 1;
  flex: 0 0 380px;
}

.detail__skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.skeleton-block--title {
  height: 28px;
  width: 70%;
}

.skeleton-block--line {
  height: 16px;
  width: 100%;
}

.skeleton-block--price {
  height: 48px;
  width: 40%;
}

@media (max-width: 991px) {
  .detail__main {
    flex-direction: column;
  }

  .detail__gallery,
  .skeleton-block--image {
    flex: none;
    width: 100%;
    max-width: 420px;
  }

  .detail__skeleton {
    flex-direction: column;
  }
}

@media (max-width: 767px) {
  .detail__name {
    font-size: 18px;
  }

  .detail__price {
    font-size: 24px;
  }

  .detail__actions :deep(.el-button) {
    flex: 1 1 calc(50% - 6px);
  }
}
</style>
