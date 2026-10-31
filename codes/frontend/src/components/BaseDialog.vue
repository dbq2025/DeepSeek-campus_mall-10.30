<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="$emit('update:modelValue', $event)"
    @closed="$emit('closed')"
  >
    <slot />
    <template #footer>
      <el-button @click="onCancel">取消</el-button>
      <el-button type="primary" :loading="confirmLoading" @click="$emit('confirm')">
        {{ confirmText }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: [String, Number], default: 480 },
  confirmText: { type: String, default: '确定' },
  confirmLoading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'closed'])

function onCancel() {
  if (props.confirmLoading) return
  emit('update:modelValue', false)
}
</script>
