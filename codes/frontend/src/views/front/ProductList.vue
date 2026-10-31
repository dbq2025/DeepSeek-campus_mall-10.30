<template>
  <div class="mall-container product-list">
    <div class="page-card toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索商品名称或卖点"
        clearable
        style="width: 240px"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px" @change="onSearch">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select v-model="query.sort" style="width: 140px" @change="onSearch">
        <el-option label="综合排序" value="default" />
        <el-option label="价格从低到高" value="price_asc" />
        <el-option label="价格从高到低" value="price_desc" />
        <el-option label="销量优先" value="sales_desc" />
        <el-option label="最新上架" value="new" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
      <span class="toolbar__right text-sub">共 {{ total }} 件商品</span>
    </div>

    <div v-if="loading" class="product-grid">
      <div v-for="n in 8" :key="n" class="skeleton-card">
        <div class="skeleton-block skeleton-card__image"></div>
        <div class="skeleton-block skeleton-card__line"></div>
        <div class="skeleton-block skeleton-card__line skeleton-card__line--short"></div>
      </div>
    </div>

    <div v-else-if="error" class="page-card">
      <EmptyState type="error" title="商品加载失败" :desc="error" @retry="load" />
    </div>

    <div v-else-if="!list.length" class="page-card">
      <EmptyState
        title="没有找到匹配的商品"
        desc="试试更换关键词，或清空筛选条件"
        action-text="清空筛选"
        @action="onReset"
      />
    </div>

    <template v-else>
      <div class="product-grid">
        <ProductCard
          v-for="product in list"
          :key="product.id"
          :product="product"
          @add-cart="onAddCart"
        />
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
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { fetchCategories, fetchProducts } from '@/api/product'
import { useListQuery } from '@/composables/useListQuery'
import { useAddToCart } from '@/composables/useAddToCart'
import ProductCard from '@/views/front/components/ProductCard.vue'
import BasePager from '@/components/BasePager.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const { add } = useAddToCart()

const categories = ref([])

const { query, list, total, loading, error, load, search, reset, setPage, setSize } = useListQuery(
  (params) => fetchProducts(params),
  {
    keyword: String(route.query.keyword || ''),
    categoryId: String(route.query.categoryId || ''),
    sort: 'default'
  }
)

/** 查询条件同步到 URL，保证刷新/分享后条件不丢 */
function syncUrl() {
  const next = {}
  if (query.keyword) next.keyword = query.keyword
  if (query.categoryId) next.categoryId = query.categoryId
  if (query.sort !== 'default') next.sort = query.sort
  router.replace({ path: '/products', query: next })
}

function onSearch() {
  syncUrl()
  search()
}

function onReset() {
  query.keyword = ''
  query.categoryId = ''
  query.sort = 'default'
  syncUrl()
  reset()
}

function onPageChange({ page, size }) {
  if (size !== query.size) setSize(size)
  else setPage(page)
}

function onAddCart(product) {
  add(product)
}

onMounted(async () => {
  try {
    categories.value = await fetchCategories()
  } catch (e) {
    categories.value = []
  }
})
</script>

<style scoped>
.product-list {
  display: flex;
  flex-direction: column;
  gap: var(--mall-space-2);
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
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }
}
</style>
