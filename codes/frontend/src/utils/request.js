import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一请求层。
 * 职责：注入 token → 解包 data → 业务码分流 → 认证失效跳转 → 消息去重。
 * 页面与 api 层拿到的是 data 本身；错误一律 reject(Error)，err.code 为业务码。
 */

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000
})

const TOKEN_KEY = 'mall_token'
const USER_KEY = 'mall_user'

/** 同屏只弹一条相同提示（连续 401 或重复点击时避免刷屏） */
let lastMessage = { text: '', at: 0 }
function toast(message, type = 'error') {
  const now = Date.now()
  if (lastMessage.text === message && now - lastMessage.at < 1500) return
  lastMessage = { text: message, at: now }
  ElMessage({ message, type, duration: 2000 })
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/** 认证失效：按当前所在区域跳对应的登录页，并带回跳地址 */
async function redirectToLogin() {
  clearAuth()
  const { default: router } = await import('@/router')
  const current = router.currentRoute.value
  const isAdminArea = current.path.startsWith('/admin')
  const loginPath = isAdminArea ? '/admin/login' : '/login'
  if (current.path === loginPath) return
  await router.replace({ path: loginPath, query: { redirect: current.fullPath } })
}

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

service.interceptors.response.use(
  (response) => {
    const body = response.data
    // 非标准返回（如文件流）直接透传
    if (!body || typeof body.code !== 'number') return body

    if (body.code === 0) return body.data

    if (body.code === 2001 || body.code === 2002) {
      toast(body.msg || '登录已过期，请重新登录')
      redirectToLogin()
      return Promise.reject(withCode(body.msg || '登录已过期', body.code))
    }

    if (body.code === 2003) {
      toast(body.msg || '没有操作权限', 'warning')
      return Promise.reject(withCode(body.msg || '没有操作权限', body.code))
    }

    toast(body.msg || '请求失败')
    return Promise.reject(withCode(body.msg || '请求失败', body.code))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      toast('登录已过期，请重新登录')
      redirectToLogin()
      return Promise.reject(withCode('登录已过期', 2001))
    }
    if (status === 403) {
      toast('没有操作权限', 'warning')
      return Promise.reject(withCode('没有操作权限', 2003))
    }
    const message =
      error.response?.data?.msg ||
      (error.code === 'ECONNABORTED' ? '请求超时，请稍后重试' : '') ||
      error.message ||
      '网络异常'
    toast(message)
    return Promise.reject(withCode(message, status || 0))
  }
)

function withCode(message, code) {
  const err = new Error(message)
  err.code = code
  return err
}

export default service
