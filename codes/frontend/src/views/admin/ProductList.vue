<template>
  <div class="admin-product">
    <div class="page-card">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索商品名称或卖点"
          clearable
          style="width: 220px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option label="上架中" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
        <div class="toolbar__right">
          <el-button :disabled="!selection.length" @click="onBatch(1)">批量上架</el-button>
          <el-button :disabled="!selection.length" @click="onBatch(0)">批量下架</el-button>
          <el-button type="primary" :icon="Plus" @click="router.push('/admin/products/new')">
            新增商品
          </el-button>
        </div>
      </div>

      <BaseTable
        :columns="columns"
        :data="list"
        :loading="loading"
        :error="error"
        empty-text="还没有商品"
        empty-action-text="新增第一件商品"
        :action-width="200"
        selectable
        @selection-change="onSelectionChange"
        @retry="load"
        @empty-action="router.push('/admin/products/new')"
      >
        <template #name="{ row }">
          <div class="product-cell">
            <el-image :src="row.mainImage" fit="cover" class="product-cell__image">
              <template #error>
                <div class="product-cell__fallback"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="product-cell__info">
              <p class="product-cell__name">{{ row.name }}</p>
              <p class="text-sub product-cell__subtitle">{{ row.subtitle || '—' }}</p>
            </div>
          </div>
        </template>

        <template #price="{ row }">
          <span class="price">{{ formatMoney(row.price) }}</span>
        </template>

        <template #stock="{ row }">
          <span :class="{ 'text-danger': row.stock === 0 }">{{ row.stock }}</span>
        </template>

        <template #status="{ row }">
          <StatusTag type="product" :status="row.status" />
        </template>

        <template #actions="{ row }">
          <el-button link type="primary" @click="router.push(`/admin/products/${row.id}/edit`)">
            编辑
          </el-button>
          <el-button link type="primary" @click="onToggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </BaseTable>

      <BasePager
        v-model:page="query.page"
        v-model:size="query.size"
        :total="total"
        @change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  batchUpdateProductStatus,
  fetchProductPage,
  removeProduct,
  updateProductStatus
} from '@/api/admin/product'
import { fetchCategoryOptions } from '@/api/admin/category'
import { useListQuery } from '@/composables/useListQuery'
import { formatMoney } from '@/utils/format'
import BaseTable from '@/components/BaseTable.vue'
import BasePager from '@/components/BasePager.vue'
import StatusTag from '@/components/StatusTag.vue'

const router = useRouter()

const columns = [
  { prop: 'name', label: '商品', minWidth: 280, slot: 'name' },
  { prop: 'categoryName', label: '分类', width: 110 },
  { prop: 'price', label: '价格', width: 110, slot: 'price' },
  { prop: 'stock', label: '库存', width: 90, align: 'center', slot: 'stock' },
  { prop: 'sales', label: '销量', width: 90, align: 'center' },
  { prop: 'status', label: '状态', width: 100, align: 'center', slot: 'status' }
]

const categories = ref([])
const selection = ref([])

const { query, list, total, loading, error, load, search, reset, setPage, setSize } = useListQuery(
  (params) => fetchProductPage(params),
  { keyword: '', categoryId: '', status: '' }
)

function onReset() {
  query.keyword = ''
  query.categoryId = ''
  query.status = ''
  reset()
}

function onSelectionChange(rows) {
  selection.value = rows
}

function onPageChange({ page, size }) {
  if (size !== query.size) setSize(size)
  else setPage(page)
}

async function onToggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await updateProductStatus(row.id, next)
    ElMessage.success(next === 1 ? '已上架' : '已下架')
    load()
  } catch (e) {
    // 请求层已提示
  }
}

async function onBatch(status) {
  const ids = selection.value.map((row) => row.id)
  if (!ids.length) return
  try {
    await ElMessageBox.confirm(
      `确定将选中的 ${ids.length} 件商品${status === 1 ? '上架' : '下架'}？`,
      '批量操作',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await batchUpdateProductStatus(ids, status)
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    // 请求层已提示
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除商品「${row.name}」？删除后前台不再展示，历史订单不受影响。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await removeProduct(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    // 请求层已提示
  }
}

onMounted(async () => {
  try {
    categories.value = await fetchCategoryOptions()
  } catch (e) {
    categories.value = []
  }
})
</script>

<style scoped>
.admin-product {
  display: flex;
  flex-direction: column;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-cell__image {
  width: 44px;
  height: 44px;
  flex: 0 0 44px;
  border-radius: var(--mall-radius-sm);
  overflow: hidden;
  background: var(--mall-bg);
}

.product-cell__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-text-4);
}

.product-cell__info {
  min-width: 0;
}

.product-cell__name {
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-cell__subtitle {
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-danger {
  color: var(--mall-danger);
  font-weight: 600;
}
</style>
