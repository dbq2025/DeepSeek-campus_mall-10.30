<template>
  <div class="base-pager">
    <el-pagination
      :current-page="page"
      :page-size="size"
      :total="total"
      :page-sizes="pageSizes"
      :layout="layout"
      :background="background"
      :small="small"
      @update:current-page="onPageChange"
      @update:page-size="onSizeChange"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  pageSizes: { type: Array, default: () => [10, 20, 50] },
  layout: { type: String, default: 'total, sizes, prev, pager, next, jumper' },
  background: { type: Boolean, default: true },
  small: { type: Boolean, default: false }
})

const emit = defineEmits(['update:page', 'update:size', 'change'])

function onPageChange(page) {
  emit('update:page', page)
  emit('change', { page, size: props.size })
}

function onSizeChange(size) {
  emit('update:size', size)
  emit('update:page', 1)
  emit('change', { page: 1, size })
}
</script>

<style scoped>
.base-pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
  overflow-x: auto;
}

.base-pager :deep(.el-pagination) {
  --el-pagination-button-bg-color: var(--mall-bg-subtle);
  font-weight: 400;
}

.base-pager :deep(.el-pager li) {
  border-radius: 8px;
  font-weight: 500;
}

@media (max-width: 767px) {
  .base-pager {
    justify-content: center;
    padding-top: 16px;
  }
}
</style>
