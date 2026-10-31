import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { mockPlugin } from './mock/index.js'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.VITE_PROXY_TARGET || 'http://localhost:8080'

  return {
    plugins: [vue(), mockPlugin(env)],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 5173,
      open: false,
      proxy: {
        '/api': {
          target,
          changeOrigin: true
        },
        // 图片走契约路径 /files/**，后端实际挂在 context-path 下（/api/files/**）。
        // 这里补前缀，保证 seed 图片与上传图片都能被浏览器直接访问。
        '/files': {
          target,
          changeOrigin: true,
          rewrite: (path) => `/api${path}`
        }
      }
    },
    build: {
      outDir: 'dist',
      chunkSizeWarningLimit: 1500
    }
  }
})
