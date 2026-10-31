<template>
  <div class="home">
    <section class="hero">
      <div class="mall-container hero__inner">
        <div class="hero__text">
          <h1 class="hero__title">校园商城 · 一站购齐</h1>
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
                <el-button type="primary" @click="onSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
        </div>
        <ul class="hero__points">
          <li><el-icon><CircleCheck /></el-icon> 正品保障</li>
          <li><el-icon><Van /></el-icon> 校内配送</li>
          <li><el-icon><RefreshLeft /></el-icon> 7 天无理由</li>
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
  background: linear-gradient(135deg, #e8f3ff 0%, #f5f7fa 55%, #eef7ee 100%);
  border-bottom: 1px solid var(--mall-border-light);
}

.hero__inner {
  padding: 40px 16px;
  display: flex;
  align-items: center;
  gap: var(--mall-space-3);
  flex-wrap: wrap;
}

.hero__text {
  flex: 1;
  min-width: 260px;
}

.hero__title {
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 1px;
}

.hero__desc {
  margin-top: 8px;
  color: var(--mall-text-2);
}

.hero__search {
  margin-top: 20px;
  max-width: 460px;
}

.hero__points {
  display: flex;
  gap: var(--mall-space-3);
  color: var(--mall-text-2);
  font-size: 13px;
}

.hero__points li {
  display: flex;
  align-items: center;
  gap: 4px;
}

.section {
  margin-top: var(--mall-space-3);
}

.category-bar {
  display: flex;
  align-items: center;
  gap: var(--mall-space-2);
  background: var(--mall-bg-white);
  border-radius: var(--mall-radius);
  padding: 14px var(--mall-space-2);
}

.category-bar__label {
  color: var(--mall-text-3);
  font-size: 13px;
  white-space: nowrap;
}

.category-bar__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.section__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--mall-space-2);
}

.section__title {
  font-size: 18px;
  font-weight: 600;
}

.section__more {
  display: flex;
  align-items: center;
  gap: 2px;
  color: var(--mall-text-3);
  font-size: 13px;
}

.section__more:hover {
  color: var(--mall-primary);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--mall-space-2);
}

.skeleton-card {
  background: var(--mall-bg-white);
  border-radius: var(--mall-radius);
  padding: 12px;
}

.skeleton-card__image {
  width: 100%;
  aspect-ratio: 1 / 1;
  margin-bottom: 12px;
}

.skeleton-card__line {
  height: 14px;
  margin-bottom: 8px;
}

.skeleton-card__line--short {
  width: 60%;
}

@media (max-width: 767px) {
  .hero__inner {
    padding: 24px 12px;
  }

  .hero__title {
    font-size: 22px;
  }

  .hero__points {
    gap: var(--mall-space-2);
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }
}
</style>
