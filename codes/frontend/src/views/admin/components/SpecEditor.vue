<template>
  <div class="spec-editor">
    <div v-for="(spec, index) in specs" :key="index" class="spec-editor__item">
      <el-input
        v-model="spec.name"
        placeholder="规格名，如 颜色"
        class="spec-editor__name"
        maxlength="20"
        @input="sync"
      />

      <div class="spec-editor__values">
        <el-tag
          v-for="(value, valueIndex) in spec.values"
          :key="valueIndex"
          closable
          type="info"
          @close="removeValue(index, valueIndex)"
        >
          {{ value }}
        </el-tag>

        <el-input
          v-if="addingIndex === index"
          v-model="draft"
          size="small"
          class="spec-editor__input"
          placeholder="回车确认"
          maxlength="20"
          @keyup.enter="confirmValue(index)"
          @blur="confirmValue(index)"
        />
        <el-button v-else link type="primary" size="small" @click="startAdd(index)">+ 添加值</el-button>
      </div>

      <el-button link type="danger" class="spec-editor__remove" @click="removeSpec(index)">
        删除
      </el-button>
    </div>

    <div class="spec-editor__foot">
      <el-button size="small" :icon="Plus" @click="addSpec">添加规格项</el-button>
      <el-button size="small" link @click="showRaw = !showRaw">
        {{ showRaw ? '收起 JSON' : '查看 JSON' }}
      </el-button>
      <span class="text-sub spec-editor__hint">留空表示该商品无规格</span>
    </div>

    <el-input
      v-if="showRaw"
      :model-value="modelValue || '（无规格）'"
      type="textarea"
      :rows="3"
      readonly
      class="spec-editor__raw"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { parseSpecJson } from '@/utils/validate'

/**
 * spec_json 可视化编辑器。
 * 对外仍是契约里的 JSON 字符串，内部转换成数组编辑，避免手写 JSON 出错。
 */
const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const specs = ref([])
const showRaw = ref(false)
const addingIndex = ref(-1)
const draft = ref('')

function parse(raw) {
  const { ok, list } = parseSpecJson(raw)
  if (!ok) return []
  return list.map((item) => ({ name: item.name, values: [...item.values] }))
}

function serialize() {
  const cleaned = specs.value
    .filter((spec) => spec.name && spec.name.trim())
    .map((spec) => ({ name: spec.name.trim(), values: spec.values.filter(Boolean) }))
  return cleaned.length ? JSON.stringify(cleaned) : ''
}

function sync() {
  emit('update:modelValue', serialize())
}

watch(
  () => props.modelValue,
  (value) => {
    if (value === serialize()) return
    specs.value = parse(value)
  },
  { immediate: true }
)

function addSpec() {
  specs.value.push({ name: '', values: [] })
  sync()
}

function removeSpec(index) {
  specs.value.splice(index, 1)
  sync()
}

function startAdd(index) {
  addingIndex.value = index
  draft.value = ''
}

function confirmValue(index) {
  const value = draft.value.trim()
  const spec = specs.value[index]
  if (value && spec && !spec.values.includes(value)) {
    spec.values.push(value)
    sync()
  }
  addingIndex.value = -1
  draft.value = ''
}

function removeValue(specIndex, valueIndex) {
  specs.value[specIndex]?.values.splice(valueIndex, 1)
  sync()
}
</script>

<style scoped>
.spec-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.spec-editor__item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border: 1px dashed var(--mall-border);
  border-radius: var(--mall-radius-sm);
  background: var(--mall-bg);
}

.spec-editor__name {
  width: 150px;
  flex: 0 0 150px;
}

.spec-editor__values {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.spec-editor__input {
  width: 110px;
}

.spec-editor__remove {
  flex: 0 0 auto;
}

.spec-editor__foot {
  display: flex;
  align-items: center;
  gap: 10px;
}

.spec-editor__hint {
  font-size: 12px;
}

.spec-editor__raw {
  margin-top: 4px;
}

@media (max-width: 767px) {
  .spec-editor__item {
    flex-wrap: wrap;
  }

  .spec-editor__name {
    flex: 1 1 100%;
    width: 100%;
  }
}
</style>
