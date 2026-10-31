<template>
  <article class="product-card" @click="goDetail">
    <div class="product-card__image">
      <el-image :src="product.mainImage" fit="cover" lazy>
        <template #error>
          <div class="product-card__fallback">
            <el-icon :size="28"><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <el-tag v-if="product.stock === 0" class="product-card__badge" type="info" size="small">
        已售罄
      </el-tag>
      <el-tag v-else-if="product.stock <= 5" class="product-card__badge" type="warning" size="small">
        仅剩 {{ product.stock }} 件
      </el-tag>
    </div>

    <div class="product-card__body">
      <h3 class="product-card__name" :title="product.name">{{ product.name }}</h3>
      <p class="product-card__subtitle" :title="product.subtitle">{{ product.subtitle }}</p>
      <div class="product-card__meta">
        <span class="price">{{ formatMoney(product.price) }}</span>
        <span class="text-sub">已售 {{ product.sales }}</span>
      </div>
    </div>

    <div class="product-card__actions">
      <el-button
        type="primary"
        plain
        size="small"
        :disabled="product.stock === 0"
        @click.stop="$emit('add-cart', product)"
      >
        {{ product.stock === 0 ? '暂时缺货' : '加入购物车' }}
      </el-button>
    </div>
  </article>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { formatMoney } from '@/utils/format'

const props = defineProps({
  product: { type: Object, required: true }
})

defineEmits(['add-cart'])

const router = useRouter()

function goDetail() {
  router.push(`/products/${props.product.id}`)
}
</script>

<style scoped>
.product-card {
  background: var(--mall-bg-white);
  border-radius: var(--mall-radius);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid var(--mall-border-light);
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--mall-shadow-hover);
}

.product-card__image {
  position: relative;
  aspect-ratio: 1 / 1;
  background: var(--mall-bg);
}

.product-card__image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.product-card__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-text-4);
  background: var(--mall-bg);
}

.product-card__badge {
  position: absolute;
  left: 8px;
  top: 8px;
}

.product-card__body {
  padding: 10px 12px 0;
  flex: 1;
}

.product-card__name {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  height: 39px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-card__subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: var(--mall-text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-card__meta {
  margin-top: 8px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.product-card__meta .price {
  font-size: 17px;
}

.product-card__actions {
  padding: 10px 12px 12px;
}

.product-card__actions .el-button {
  width: 100%;
}
</style>
