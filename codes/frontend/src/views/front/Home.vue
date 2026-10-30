<template>
  <div class="page">
    <header class="hero">
      <h1>校园电商平台</h1>
      <p>前端脚手架已就绪 · 环境自检页（开发期用，后续可替换为真实首页）</p>
    </header>

    <el-card class="card" shadow="never">
      <template #header>
        <div class="card-head">
          <span>后端连通性</span>
          <el-button size="small" :loading="loading" @click="checkBackend">重新检测</el-button>
        </div>
      </template>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="接口地址">{{ apiBase }}/health</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="health ? 'success' : 'danger'" size="small">
            {{ health ? '已连通' : '未连通' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据库">{{ health?.dbProduct || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Schema">{{ health?.dbSchema || '—' }}</el-descriptions-item>
        <el-descriptions-item label="缓存实现">{{ health?.cacheType || '—' }}</el-descriptions-item>
        <el-descriptions-item label="缓存可用">
          {{ health ? (health.cacheOk ? '是' : '否') : '—' }}
        </el-descriptions-item>
      </el-descriptions>
      <el-alert
        v-if="error"
        class="mt"
        type="error"
        :title="error"
        :closable="false"
        show-icon
      />
      <p class="hint">若显示未连通：确认后端已启动（8080）、MySQL 已运行、Vite 代理指向正确。</p>
    </el-card>

    <el-card class="card" shadow="never">
      <template #header>
        <div class="card-head">
          <span>Mock 数据（{{ useMock ? '已开启' : '已关闭' }}）</span>
          <el-button size="small" :loading="mockLoading" @click="loadMock">拉取示例商品</el-button>
        </div>
      </template>
      <el-table v-loading="mockLoading" :data="products" size="small" border>
        <el-table-column prop="name" label="商品名" min-width="150" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="subtitle" label="说明" min-width="180" />
      </el-table>
      <p class="hint">
        Mock 由 <code>mock/index.js</code> 提供，开关在 <code>.env.development</code> 的
        <code>VITE_USE_MOCK</code>。数据须与 <code>design/</code> 下的接口契约保持一致。
      </p>
    </el-card>

    <div class="links">
      <el-button type="primary" plain @click="$router.push('/admin/login')">后台登录（占位）</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '@/utils/request'

const apiBase = import.meta.env.VITE_API_BASE || '/api'
const useMock = import.meta.env.VITE_USE_MOCK === 'true'

const loading = ref(false)
const mockLoading = ref(false)
const health = ref(null)
const error = ref('')
const products = ref([])

async function checkBackend() {
  loading.value = true
  error.value = ''
  try {
    const res = await request.get('/health')
    health.value = res.data
  } catch (e) {
    health.value = null
    error.value = e.message || '请求失败'
  } finally {
    loading.value = false
  }
}

async function loadMock() {
  mockLoading.value = true
  try {
    const res = await request.get('/front/products', { params: { page: 1, size: 10 } })
    products.value = res.data?.records ?? []
  } finally {
    mockLoading.value = false
  }
}

onMounted(checkBackend)
</script>

<style scoped>
.page {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 20px 56px;
}

.hero h1 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 500;
}

.hero p {
  margin: 0 0 20px;
  color: #909399;
  font-size: 13px;
}

.card {
  margin-bottom: 16px;
  border-radius: 10px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 500;
}

.mt {
  margin-top: 12px;
}

.hint {
  margin: 12px 0 0;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

code {
  background: #f2f3f5;
  border-radius: 3px;
  padding: 1px 4px;
  font-size: 12px;
}

.links {
  margin-top: 8px;
}
</style>
