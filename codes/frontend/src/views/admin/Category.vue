<template>
  <div class="category">
    <div class="page-card">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索类型名称"
          clearable
          style="width: 220px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        <div class="toolbar__right">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增类型</el-button>
        </div>
      </div>

      <BaseTable
        :columns="columns"
        :data="list"
        :loading="loading"
        :error="error"
        empty-text="还没有商品类型"
        empty-action-text="新增第一个类型"
        :action-width="150"
        @retry="load"
        @empty-action="openCreate"
      >
        <template #createTime="{ row }">
          <span class="text-sub">{{ formatDateTime(row.createTime) }}</span>
        </template>
        <template #actions="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" :disabled="row.productCount > 0" @click="onDelete(row)">
            删除
          </el-button>
        </template>
      </BaseTable>

      <BasePager
        v-model:page="query.page"
        v-model:size="query.size"
        :total="total"
        @change="onPageChange"
      />
    </div>

    <BaseDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑类型' : '新增类型'"
      :confirm-loading="submitting"
      width="420px"
      @confirm="onSubmit"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="如：数码电子" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
          <span class="text-sub category__hint">数值越小越靠前</span>
        </el-form-item>
      </el-form>
    </BaseDialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  createCategory,
  fetchCategoryPage,
  removeCategory,
  updateCategory
} from '@/api/admin/category'
import { useListQuery } from '@/composables/useListQuery'
import { formatDateTime } from '@/utils/format'
import BaseTable from '@/components/BaseTable.vue'
import BasePager from '@/components/BasePager.vue'
import BaseDialog from '@/components/BaseDialog.vue'

const columns = [
  { prop: 'name', label: '类型名称', minWidth: 160 },
  { prop: 'sort', label: '排序', width: 100, align: 'center' },
  { prop: 'productCount', label: '商品数', width: 100, align: 'center' },
  { prop: 'createTime', label: '创建时间', width: 180, slot: 'createTime' }
]

const { query, list, total, loading, error, load, search, reset, setPage, setSize } = useListQuery(
  (params) => fetchCategoryPage(params),
  { keyword: '' }
)

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const formRef = ref(null)

const form = reactive({ name: '', sort: 0 })
const rules = {
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }]
}

function openCreate() {
  isEdit.value = false
  editingId.value = ''
  form.name = ''
  form.sort = 0
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editingId.value = row.id
  form.name = row.name
  form.sort = row.sort
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = { name: form.name.trim(), sort: form.sort }
    if (isEdit.value) await updateCategory(editingId.value, payload)
    else await createCategory(payload)
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    // 请求层已提示
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除类型「${row.name}」？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await removeCategory(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    // 请求层已提示（如类型下仍有商品）
  }
}

function onPageChange({ page, size }) {
  if (size !== query.size) setSize(size)
  else setPage(page)
}
</script>

<style scoped>
.category .page-card {
  padding: 20px 24px;
}

.category__hint {
  margin-left: 10px;
  font-size: 12px;
  color: var(--mall-text-3);
}

/* createTime 列统一格式化 */
:deep(.el-table) .cell {
  word-break: break-word;
}

@media (max-width: 767px) {
  .category .page-card {
    padding: 14px 12px;
  }
}
</style>
