<template>
  <div class="home">
    <section class="hero">
      <div class="hero__bg" aria-hidden="true">
        <span class="hero__blob hero__blob--a"></span>
        <span class="hero__blob hero__blob--b"></span>
        <span class="hero__grid"></span>
      </div>
      <div class="mall-container hero__inner">
        <div class="hero__text">
          <span class="hero__eyebrow"><i class="hero__dot"></i>校内仓储 · 当日出库</span>
          <h1 class="hero__title">
            校园商城<span class="hero__title-accent">· 一站购齐</span>
          </h1>
          <p class="hero__desc">数码、文具、生活、运动，校内仓储当日出库</p>
          <div class="hero__search">
            <el-input
              v-model="keyword"
              size="large"
              placeholder="搜索商品，如「机械键盘」"
              clearable
              @keyup.enter="onSearch"
            >
              <template #append>
                <el-button @click="onSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
        </div>
        <ul class="hero__points">
          <li>
            <span class="hero__point-icon"><el-icon><CircleCheck /></el-icon></span>
            正品保障
          </li>
          <li>
            <span class="hero__point-icon"><el-icon><Van /></el-icon></span>
            校内配送
          </li>
          <li>
            <span class="hero__point-icon"><el-icon><RefreshLeft /></el-icon></span>
            7 天无理由
          </li>
        </ul>
      </div>
    </section>

    <section class="mall-container section">
      <div class="category-bar">
        <span class="category-bar__label">商品分类</span>
        <div class="category-bar__list">
          <el-button
            v-for="item in categories"
            :key="item.id"
            :type="activeCategory === item.id ? 'primary' : 'default'"
            plain
            size="small"
            round
            @click="goCategory(item.id)"
          >
            {{ item.name }}
          </el-button>
          <el-button v-if="!categories.length" size="small" text disabled>暂无分类</el-button>
        </div>
      </div>
    </section>

    <section class="mall-container section">
      <div class="section__head">
        <h2 class="section__title">热销推荐</h2>
        <router-link to="/products" class="section__more">
          查看全部<el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div v-if="loading" class="product-grid">
        <div v-for="n in 8" :key="n" class="skeleton-card">
          <div class="skeleton-block skeleton-card__image"></div>
          <div class="skeleton-block skeleton-card__line"></div>
          <div class="skeleton-block skeleton-card__line skeleton-card__line--short"></div>
        </div>
      </div>

      <EmptyState
        v-else-if="error"
        type="error"
        title="热销商品加载失败"
        :desc="error"
        @retry="load"
      />

      <EmptyState
        v-else-if="!hotProducts.length"
        title="还没有上架的商品"
        desc="管理员在后台发布商品后会展示在这里"
        action-text="刷新看看"
        @action="load"
      />

      <div v-else class="product-grid">
        <ProductCard
          v-for="product in hotProducts"
          :key="product.id"
          :product="product"
          @add-cart="onAddCart"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchCategories, fetchProducts } from '@/api/product'
import ProductCard from '@/views/front/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { useAddToCart } from '@/composables/useAddToCart'

const router = useRouter()
const { add } = useAddToCart()

const keyword = ref('')
const categories = ref([])
const hotProducts = ref([])
const loading = ref(true)
const error = ref('')
const activeCategory = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [cats, page] = await Promise.all([
      fetchCategories(),
      fetchProducts({ page: 1, size: 8, sort: 'sales_desc' })
    ])
    categories.value = cats || []
    hotProducts.value = page?.records || []
  } catch (e) {
    error.value = e.message || '加载失败'
    hotProducts.value = []
  } finally {
    loading.value = false
  }
}

function onSearch() {
  const word = keyword.value.trim()
  router.push({ path: '/products', query: word ? { keyword: word } : {} })
}

function goCategory(id) {
  router.push({ path: '/products', query: { categoryId: id } })
}

function onAddCart(product) {
  add(product)
}

onMounted(load)
</script>

<style scoped>
.hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #f5f6ff 0%, #fbfcfe 100%);
  border-bottom: 1px solid var(--mall-border-light);
}

.hero__bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero__blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(64px);
}

.hero__blob--a {
  width: 420px;
  height: 420px;
  left: -120px;
  top: -180px;
  background: radial-gradient(circle, rgba(79, 70, 229, 0.26), transparent 70%);
}

.hero__blob--b {
  width: 380px;
  height: 380px;
  right: -100px;
  bottom: -200px;
  background: radial-gradient(circle, rgba(34, 184, 207, 0.18), transparent 70%);
}

.hero__grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(15, 23, 42, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(15, 23, 42, 0.035) 1px, transparent 1px);
  background-size: 56px 56px;
  -webkit-mask-image: radial-gradient(ellipse at 50% 30%, #000 30%, transparent 76%);
  mask-image: radial-gradient(ellipse at 50% 30%, #000 30%, transparent 76%);
}

.hero__inner {
  position: relative;
  z-index: 1;
  padding: 56px 24px 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 48px;
}

.hero__text {
  flex: 1;
  min-width: 0;
}

.hero__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--mall-border-light);
  box-shadow: var(--mall-shadow-xs);
  color: var(--mall-text-2);
  font-size: 12px;
  font-weight: 500;
}

.hero__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--mall-success);
  box-shadow: 0 0 0 3px rgba(18, 183, 106, 0.14);
}

.hero__title {
  margin-top: 18px;
  font-size: 40px;
  line-height: 1.18;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--mall-text-1);
}

.hero__title-accent {
  margin-left: 10px;
  background: var(--mall-primary-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero__desc {
  margin-top: 14px;
  max-width: 520px;
  color: var(--mall-text-2);
  font-size: 16px;
}

.hero__search {
  margin-top: 28px;
  max-width: 520px;
}

.hero__search :deep(.el-input__wrapper) {
  height: 48px;
  padding-left: 20px;
  border-radius: 999px 0 0 999px;
  background: var(--mall-bg-white);
  box-shadow: inset 0 0 0 1px var(--mall-border-light), var(--mall-shadow-card);
}

.hero__search :deep(.el-input__wrapper.is-focus) {
  box-shadow: inset 0 0 0 1px var(--mall-primary), 0 0 0 3px var(--mall-primary-ring);
}

.hero__search :deep(.el-input-group__append) {
  padding: 0;
  background: var(--mall-bg-white);
  border: none;
  border-radius: 0 999px 999px 0;
  box-shadow: inset 0 0 0 1px var(--mall-border-light), var(--mall-shadow-card);
}

.hero__search :deep(.el-input-group__append .el-button) {
  height: 40px;
  margin: 4px;
  padding: 0 24px;
  border: none;
  border-radius: 999px;
  background: var(--mall-primary-gradient);
  color: #fff;
  font-weight: 500;
  box-shadow: var(--mall-shadow-brand);
}

.hero__search :deep(.el-input-group__append .el-button:hover) {
  color: #fff;
  filter: brightness(1.06);
}

.hero__points {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 0 0 auto;
}

.hero__points li {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--mall-text-2);
  font-size: 13px;
  white-space: nowrap;
}

.hero__point-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid var(--mall-border-light);
  box-shadow: var(--mall-shadow-xs);
  color: var(--mall-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.section {
  margin-top: 40px;
}

.category-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--mall-bg-white);
  border: 1px solid var(--mall-border-light);
  border-radius: var(--mall-radius-lg);
  box-shadow: var(--mall-shadow-card);
  padding: 14px 20px;
}

.category-bar__label {
  color: var(--mall-text-3);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.category-bar__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-bar__list :deep(.el-button) {
  height: 32px;
  margin: 0;
  padding: 0 16px;
  border-radius: 999px;
  border-color: transparent;
  background: var(--mall-bg-subtle);
  color: var(--mall-text-2);
  font-weight: 500;
}

.category-bar__list :deep(.el-button:hover) {
  background: var(--mall-primary-soft);
  border-color: transparent;
  color: var(--mall-primary);
}

.category-bar__list :deep(.el-button--primary.is-plain) {
  background: var(--mall-primary-soft);
  border-color: rgba(79, 70, 229, 0.22);
  color: var(--mall-primary);
}

.section__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section__title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--mall-text-1);
}

.section__more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--mall-text-3);
  font-size: 13px;
  transition: color var(--mall-dur-fast) ease-out;
}

.section__more:hover {
  color: var(--mall-primary);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.skeleton-card {
  background: var(--mall-bg-white);
  border: 1px solid var(--mall-border-light);
  border-radius: var(--mall-radius-lg);
  overflow: hidden;
}

.skeleton-card__image {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 0;
}

.skeleton-card__line {
  height: 14px;
  margin: 14px 16px 0;
}

.skeleton-card__line--short {
  width: 50%;
  margin-bottom: 18px;
}

@media (max-width: 1279px) {
  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1023px) {
  .hero__inner {
    gap: 32px;
  }

  .hero__title {
    font-size: 34px;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .hero__inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
    padding: 32px 12px 30px;
  }

  .hero__title {
    margin-top: 14px;
    font-size: 26px;
  }

  .hero__desc {
    font-size: 14px;
  }

  .hero__search {
    margin-top: 20px;
  }

  .hero__points {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 12px;
  }

  .hero__points li {
    font-size: 12px;
  }

  .hero__point-icon {
    width: 26px;
    height: 26px;
    border-radius: 8px;
  }

  .section {
    margin-top: 28px;
  }

  .category-bar {
    padding: 14px 14px 16px;
    gap: 10px;
    flex-direction: column;
    align-items: stretch;
  }

  .category-bar__label {
    padding-top: 0;
  }

  .product-grid {
    gap: 12px;
  }

  .skeleton-card__line {
    margin: 12px 12px 0;
  }

  .skeleton-card__line--short {
    margin-bottom: 14px;
  }
}

@media (max-width: 359px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
