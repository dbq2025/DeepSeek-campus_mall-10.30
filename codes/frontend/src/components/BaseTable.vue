<template>
  <div class="base-table">
    <EmptyState
      v-if="error"
      type="error"
      title="列表加载失败"
      :desc="error"
      @retry="$emit('retry')"
    />

    <el-table
      v-else
      v-loading="loading"
      :data="data"
      :row-key="rowKey"
      :size="size"
      border
      stripe
      class="table-scroll"
      :empty-text="emptyText"
      @selection-change="$emit('selection-change', $event)"
    >
      <el-table-column
        v-if="selectable"
        type="selection"
        width="46"
        :selectable="rowSelectable || undefined"
      />

      <el-table-column
        v-for="col in columns"
        :key="col.prop || col.label"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
        :align="col.align || 'left'"
        :fixed="col.fixed"
        :show-overflow-tooltip="col.showOverflowTooltip !== false"
      >
        <template #default="scope">
          <!-- 有插槽：交给页面自定义渲染；无插槽：按 prop 原样输出 -->
          <slot v-if="col.slot || $slots[col.prop]" :name="col.slot || col.prop" v-bind="scope" />
          <span v-else>{{ scope.row[col.prop] }}</span>
        </template>
      </el-table-column>

      <el-table-column
        v-if="$slots.actions"
        label="操作"
        :width="actionWidth"
        align="center"
        fixed="right"
      >
        <template #default="scope">
          <slot name="actions" v-bind="scope" />
        </template>
      </el-table-column>

      <template #empty>
        <EmptyState
          type="empty"
          :title="emptyText"
          :action-text="emptyActionText"
          @action="$emit('empty-action')"
        />
      </template>
    </el-table>
  </div>
</template>

<script setup>
import EmptyState from '@/components/EmptyState.vue'

defineProps({
  /**
   * 列配置：{ prop, label, width, minWidth, align, slot, showOverflowTooltip, fixed }
   * 需要自定义渲染时给 slot 名，或直接给与 prop 同名的插槽。
   */
  columns: { type: Array, required: true },
  data: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  /** 非空表示请求失败，表格整体替换为错误态 */
  error: { type: String, default: '' },
  rowKey: { type: [String, Function], default: 'id' },
  size: { type: String, default: 'small' },
  emptyText: { type: String, default: '暂无数据' },
  emptyActionText: { type: String, default: '' },
  actionWidth: { type: [Number, String], default: 180 },
  /** 是否显示多选列（批量操作场景） */
  selectable: { type: Boolean, default: false },
  /** 单行是否可选：(row, index) => boolean */
  rowSelectable: { type: Function, default: null }
})

defineEmits(['retry', 'empty-action', 'selection-change'])
</script>

<style scoped>
.base-table {
  width: 100%;
}
</style>
