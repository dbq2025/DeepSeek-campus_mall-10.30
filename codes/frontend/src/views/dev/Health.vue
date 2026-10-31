<template>
  <div class="mall-container health">
    <h1 class="page-title">环境自检</h1>
    <el-alert
      type="info"
      :closable="false"
      title="本页仅用于开发期排查，不进入任何导航"
      description="检查前端构建配置、接口连通性与 Mock 开关状态。"
      show-icon
    />

    <el-descriptions class="health__desc" :column="1" border>
      <el-descriptions-item label="接口前缀">
        {{ apiBase }}
      </el-descriptions-item>
      <el-descriptions-item label="Mock 开关">
        {{ useMock ? '开启（VITE_USE_MOCK=true）' : '关闭（走真实后端）' }}
      </el-descriptions-item>
      <el-descriptions-item label="路由模式"> history </el-descriptions-item>
      <el-descriptions-item label="构建工具">
        Vite（package.json 声明 ^5.2.13）
      </el-descriptions-item>
    </el-descriptions>

    <div class="health__actions">
      <el-button type="primary" :loading="loading" @click="check">检测 /health</el-button>
      <el-button :disabled="!useMock" @click="resetMock">复位 Mock 数据</el-button>
    </div>

    <el-alert
      v-if="result"
      class="health__result"
      :type="result.ok ? 'success' : 'error'"
      :title="result.message"
      :closable="false"
      show-icon
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'

// Vite 在构建时静态替换 import.meta.env，这里直接读取即可
const apiBase = import.meta.env.VITE_API_BASE || '/api'
const useMock = import.meta.env.VITE_USE_MOCK === 'true'

const loading = ref(false)
const result = ref(null)

async function check() {
  loading.value = true
  result.value = null
  try {
    const data = await request.get('/health')
    result.value = { ok: true, message: `接口连通：${JSON.stringify(data)}` }
  } catch (e) {
    result.value = { ok: false, message: `接口不可用：${e.message}` }
  } finally {
    loading.value = false
  }
}

async function resetMock() {
  loading.value = true
  try {
    await request.post('/__mock/reset')
    result.value = { ok: true, message: 'Mock 数据已复位' }
  } catch (e) {
    result.value = { ok: false, message: `复位失败：${e.message}` }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.health {
  padding-top: var(--mall-space-3);
  padding-bottom: var(--mall-space-3);
}

.health__desc {
  margin-top: var(--mall-space-2);
}

.health__actions {
  margin-top: var(--mall-space-2);
}

.health__result {
  margin-top: var(--mall-space-2);
}
</style>
