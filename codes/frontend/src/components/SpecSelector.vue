<template>
  <div v-if="specList.length" class="spec-selector">
    <div v-for="spec in specList" :key="spec.name" class="spec-row">
      <span class="spec-row__label">{{ spec.name }}</span>
      <el-radio-group v-model="selected[spec.name]" size="default">
        <el-radio-button v-for="value in spec.values" :key="value" :value="value">
          {{ value }}
        </el-radio-button>
      </el-radio-group>
    </div>
    <p v-if="!complete" class="spec-selector__tip">请选择：{{ missingText }}</p>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { parseSpecJson } from '@/utils/validate'
import { splitSpecText } from '@/utils/format'

/**
 * 规格选择器：把 spec_json 渲染成若干单选组，选择结果拼成 spec_text 快照。
 * 只做"展示型规格"，不影响价格与库存（见 design/01 §2）。
 */
const props = defineProps({
  /** 商品 spec_json 字符串，空表示无规格 */
  specJson: { type: String, default: '' },
  /** 已选规格快照，如 '颜色:黑;尺码:M' */
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'change'])

const specList = computed(() => {
  const { ok, list } = parseSpecJson(props.specJson)
  return ok ? list : []
})

const selected = ref({})

const complete = computed(
  () => specList.value.length > 0 && specList.value.every((spec) => Boolean(selected.value[spec.name]))
)

const missingText = computed(() =>
  specList.value
    .filter((spec) => !selected.value[spec.name])
    .map((spec) => spec.name)
    .join('、')
)

const specText = computed(() =>
  complete.value ? specList.value.map((spec) => `${spec.name}:${selected.value[spec.name]}`).join(';') : ''
)

watch(specText, (value) => {
  emit('update:modelValue', value)
  emit('change', { specText: value, complete: complete.value })
})

watch(
  () => props.specJson,
  () => {
    const snapshot = {}
    splitSpecText(props.modelValue).forEach((pair) => {
      const index = pair.indexOf(':')
      if (index > 0) snapshot[pair.slice(0, index).trim()] = pair.slice(index + 1).trim()
    })
    const next = {}
    specList.value.forEach((spec) => {
      if (snapshot[spec.name]) next[spec.name] = snapshot[spec.name]
    })
    selected.value = next
  },
  { immediate: true }
)

defineExpose({ complete, specText })
</script>

<style scoped>
.spec-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.spec-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.spec-row__label {
  flex: 0 0 56px;
  color: var(--mall-text-3);
  line-height: 32px;
  font-size: 13px;
}

.spec-row :deep(.el-radio-button__inner) {
  border-radius: var(--mall-radius-sm);
  border-left: 1px solid var(--el-border-color);
}

.spec-selector__tip {
  font-size: 12px;
  color: var(--mall-warning);
}

@media (max-width: 767px) {
  .spec-row {
    flex-direction: column;
    gap: 6px;
  }

  .spec-row__label {
    line-height: 1.4;
  }
}
</style>
