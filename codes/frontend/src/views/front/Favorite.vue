<template>
  <div class="mall-container favorite">
    <div class="page-card toolbar">
      <h1 class="page-title favorite__title">我的收藏</h1>
      <span class="toolbar__right text-sub">共 {{ total }} 件</span>
    </div>

    <div v-if="loading" class="fav-grid">
      <div v-for="n in 4" :key="n" class="skeleton-card">
        <div class="skeleton-block skeleton-card__image"></div>
        <div class="skeleton-block skeleton-card__line"></div>
      </div>
    </div>

    <div v-else-if="error" class="page-card">
      <EmptyState type="error" title="收藏加载失败" :desc="error" @retry="load" />
    </div>

    <div v-else-if="!list.length" class="page-card">
      <EmptyState
        title="还没有收藏任何商品"
        desc="在商品详情页点击「收藏」，就能在这里快速找到它"
        action-text="去逛逛"
        @action="router.push('/products')"
      />
    </div>

    <template v-else>
      <div class="fav-grid">
        <div v-for="item in list" :key="item.favoriteId" class="fav-item">
          <ProductCard :product="item" @add-cart="onAddCart" />
          <div class="fav-item__foot">
            <span class="text-sub">收藏于 {{ formatDate(item.favoriteTime) }}</span>
            <el-button link type="danger" @click="onRemove(item)">取消收藏</el-button>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchFavorites, removeFavorite } from '@/api/favorite'
import { useListQuery } from '@/composables/useListQuery'
import { useAddToCart } from '@/composables/useAddToCart'
import { formatDate } from '@/utils/format'
import ProductCard from '@/views/front/components/ProductCard.vue'
import BasePager from '@/components/BasePager.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const { add } = useAddToCart()

const { query, list, total, loading, error, load, setPage, setSize } = useListQuery(
  (params) => fetchFavorites(params),
  {},
  { pageSize: 8 }
)

async function onRemove(item) {
  try {
    await removeFavorite(item.id)
    ElMessage.success('已取消收藏')
    load()
  } catch (e) {
    // 请求层已提示
  }
}

function onPageChange({ page, size }) {
  if (size !== query.size) setSize(size)
  else setPage(page)
}

function onAddCart(product) {
  add(product)
}
</script>

<style scoped>
.favorite {
  display: flex;
  flex-direction: column;
  gap: var(--mall-space-2);
}

.favorite__title {
  margin-bottom: 0;
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--mall-space-2);
}

.fav-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.fav-item__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
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
}

@media (max-width: 767px) {
  .fav-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }
}
</style>
