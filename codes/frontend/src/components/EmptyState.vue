<template>
  <div class="empty-state">
    <span class="empty-state__badge" :class="{ 'is-error': type === 'error' }">
      <el-icon :size="34">
        <WarningFilled v-if="type === 'error'" />
        <Box v-else />
      </el-icon>
    </span>
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
  padding: 56px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.empty-state__badge {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-primary);
  background: radial-gradient(circle at 32% 28%, #ffffff 0%, var(--mall-primary-soft) 62%, #e3e6ff 100%);
  box-shadow: inset 0 0 0 1px rgba(79, 70, 229, 0.12), var(--mall-shadow-xs);
}

.empty-state__badge.is-error {
  color: var(--mall-danger);
  background: radial-gradient(circle at 32% 28%, #ffffff 0%, var(--mall-danger-soft) 62%, #fbdad7 100%);
  box-shadow: inset 0 0 0 1px rgba(240, 68, 56, 0.14), var(--mall-shadow-xs);
}

.empty-state__title {
  margin-top: 18px;
  font-size: 15px;
  font-weight: 600;
  color: var(--mall-text-1);
}

.empty-state__desc {
  margin-top: 8px;
  font-size: 13px;
  color: var(--mall-text-3);
  max-width: 420px;
  line-height: 1.6;
}

.empty-state__actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
</style>
