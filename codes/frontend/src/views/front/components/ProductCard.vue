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
  border: 1px solid var(--mall-border-light);
  border-radius: var(--mall-radius-lg);
  box-shadow: var(--mall-shadow-xs);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform var(--mall-dur) var(--mall-ease), box-shadow var(--mall-dur) var(--mall-ease),
    border-color var(--mall-dur) var(--mall-ease);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--mall-shadow-hover);
  border-color: rgba(79, 70, 229, 0.22);
}

.product-card__image {
  position: relative;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: var(--mall-bg-subtle);
}

.product-card__image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.product-card__image :deep(img) {
  transition: transform 420ms var(--mall-ease);
}

.product-card:hover .product-card__image :deep(img) {
  transform: scale(1.04);
}

.product-card__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-text-4);
  background: var(--mall-bg-subtle);
}

.product-card__badge {
  position: absolute;
  left: 10px;
  top: 10px;
  border: none;
  border-radius: 999px;
  font-weight: 500;
  box-shadow: var(--mall-shadow-xs);
}

.product-card__body {
  padding: 14px 16px 0;
  flex: 1;
}

.product-card__name {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.45;
  height: 44px;
  color: var(--mall-text-1);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--mall-dur-fast) ease-out;
}

.product-card:hover .product-card__name {
  color: var(--mall-primary);
}

.product-card__subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: var(--mall-text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-card__meta {
  margin-top: 10px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.product-card__meta .price {
  font-size: 20px;
  line-height: 1.2;
}

.product-card__actions {
  padding: 12px 16px 16px;
}

.product-card__actions .el-button {
  width: 100%;
  height: 36px;
  border-radius: 10px;
  font-weight: 500;
}

.product-card__actions :deep(.el-button) {
  background: var(--mall-primary-soft);
  border-color: transparent;
  color: var(--mall-primary);
}

.product-card__actions :deep(.el-button:not(.is-disabled):hover) {
  background: var(--mall-primary);
  border-color: var(--mall-primary);
  color: #fff;
  box-shadow: var(--mall-shadow-brand);
}

.product-card__actions :deep(.el-button.is-disabled) {
  background: var(--mall-bg-subtle);
  border-color: transparent;
  color: var(--mall-text-4);
}

@media (max-width: 767px) {
  .product-card__body {
    padding: 10px 12px 0;
  }

  .product-card__name {
    font-size: 14px;
    height: 41px;
  }

  .product-card__meta .price {
    font-size: 17px;
  }

  .product-card__actions {
    padding: 10px 12px 12px;
  }

  .product-card__actions .el-button {
    height: 32px;
  }
}
</style>
