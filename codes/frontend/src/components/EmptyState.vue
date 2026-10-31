<template>
  <div class="empty-state">
    <el-icon :size="42" :class="['empty-state__icon', { 'is-error': type === 'error' }]">
      <WarningFilled v-if="type === 'error'" />
      <Box v-else />
    </el-icon>
    <p class="empty-state__title">{{ title || (type === 'error' ? '加载失败' : '暂无数据') }}</p>
    <p v-if="desc" class="empty-state__desc">{{ desc }}</p>
    <div class="empty-state__actions">
      <el-button v-if="type === 'error'" type="primary" plain @click="$emit('retry')">重新加载</el-button>
      <el-button
        v-if="actionText"
        :type="type === 'error' ? 'default' : 'primary'"
        plain
        @click="$emit('action')"
      >
        {{ actionText }}
      </el-button>
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  /** empty：无数据；error：请求失败 */
  type: { type: String, default: 'empty' },
  title: { type: String, default: '' },
  desc: { type: String, default: '' },
  /** 空态下的主行动按钮文案，为空则不渲染 */
  actionText: { type: String, default: '' }
})

defineEmits(['retry', 'action'])
</script>

<style scoped>
.empty-state {
  padding: 48px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.empty-state__icon {
  color: var(--mall-text-4);
}

.empty-state__icon.is-error {
  color: var(--mall-danger);
}

.empty-state__title {
  margin-top: 12px;
  font-size: 15px;
  color: var(--mall-text-2);
}

.empty-state__desc {
  margin-top: 6px;
  font-size: 13px;
  color: var(--mall-text-3);
  max-width: 420px;
}

.empty-state__actions {
  margin-top: 16px;
}
</style>
