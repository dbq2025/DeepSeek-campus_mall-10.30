import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'

const TOKEN_KEY = 'mall_token'
const USER_KEY = 'mall_user'

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch (e) {
    return null
  }
}

/**
 * 用户状态：token + userInfo + role。
 * 不引持久化插件，手写两个 localStorage key，便于排查与清理。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(readStoredUser())

  const isLogin = computed(() => Boolean(token.value))
  const role = computed(() => userInfo.value?.role || '')
  const isAdmin = computed(() => role.value === 'ADMIN')
  const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '未登录')

  function setSession(data) {
    token.value = data?.token || ''
    userInfo.value = data?.userInfo || null
    if (token.value) localStorage.setItem(TOKEN_KEY, token.value)
    else localStorage.removeItem(TOKEN_KEY)
    if (userInfo.value) localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
    else localStorage.removeItem(USER_KEY)
  }

  function clearSession() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  /** AUTH-01 */
  async function login(payload) {
    const data = await authApi.login(payload)
    setSession(data)
    return data
  }

  /** AUTH-03 */
  async function adminLogin(payload) {
    const data = await authApi.adminLogin(payload)
    setSession(data)
    return data
  }

  /** AUTH-02 */
  async function register(payload) {
    const data = await authApi.register(payload)
    setSession(data)
    return data
  }

  /** 刷新页面后用 AUTH-04 校验 token 是否仍有效 */
  async function refreshProfile() {
    if (!token.value) return null
    try {
      const data = await authApi.fetchMe()
      userInfo.value = data
      localStorage.setItem(USER_KEY, JSON.stringify(data))
      return data
    } catch (e) {
      clearSession()
      return null
    }
  }

  /** AUTH-05：服务端无状态，失败也要清本地 */
  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // 令牌已失效时忽略，本地清理照常执行
    }
    clearSession()
  }

  return {
    token,
    userInfo,
    isLogin,
    role,
    isAdmin,
    displayName,
    setSession,
    clearSession,
    login,
    adminLogin,
    register,
    refreshProfile,
    logout
  }
})
