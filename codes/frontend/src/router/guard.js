import { useUserStore } from '@/stores/user'

/**
 * 全局守卫：只做"藏"，真正的权限边界在后端。
 * 三类 meta：
 *  - requiresAuth  需登录（前台业务页）
 *  - requiresAdmin 需 ADMIN 角色（后台全部页面）
 *  - guestOnly     仅游客可访问（登录/注册）
 */
export function setupGuard(router) {
  router.beforeEach(async (to) => {
    const user = useUserStore()

    // 本地有 token 但用户信息丢失（换设备/清缓存）：先校验一次
    if (user.isLogin && !user.userInfo) {
      await user.refreshProfile()
    }

    if (to.meta.guestOnly && user.isLogin) {
      return { path: to.path.startsWith('/admin') && user.isAdmin ? '/admin' : '/' }
    }

    if (to.meta.requiresAuth && !user.isLogin) {
      const loginPath = to.path.startsWith('/admin') ? '/admin/login' : '/login'
      return { path: loginPath, query: { redirect: to.fullPath } }
    }

    if (to.meta.requiresAdmin && !user.isAdmin) {
      if (!user.isLogin) return { path: '/admin/login', query: { redirect: to.fullPath } }
      // 普通用户误入后台：回前台首页，不暴露后台结构
      return { path: '/' }
    }

    return true
  })

  router.afterEach((to) => {
    document.title = to.meta.title ? `${to.meta.title} · 校园电商平台` : '校园电商平台'
  })
}
