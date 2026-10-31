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
  gap: 20px;
}

.favorite .page-card.toolbar {
  margin-bottom: 0;
  padding: 18px 24px;
}

.favorite__title {
  margin-bottom: 0;
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.fav-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fav-item__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2px;
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
  margin: 14px 16px 18px;
}

@media (max-width: 1279px) {
  .fav-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1023px) {
  .fav-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .favorite {
    gap: 14px;
  }

  .favorite .page-card.toolbar {
    padding: 14px 16px;
  }

  .fav-grid {
    gap: 12px;
  }

  .skeleton-card__line {
    margin: 12px 12px 14px;
  }
}
</style>
