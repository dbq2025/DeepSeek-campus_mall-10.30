import { createRouter, createWebHistory } from 'vue-router'
import frontRoutes from './front.routes'
import adminRoutes from './admin.routes'
import { setupGuard } from './guard'

/** 独立页（不带前台顶栏/后台侧栏） */
const standaloneRoutes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/front/Login.vue'),
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/front/Register.vue'),
    meta: { title: '注册', guestOnly: true }
  },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '管理员登录', guestOnly: true }
  },
  {
    path: '/dev/health',
    name: 'dev-health',
    component: () => import('@/views/dev/Health.vue'),
    meta: { title: '环境自检' }
  }
]

const routes = [
  ...standaloneRoutes,
  frontRoutes,
  adminRoutes,
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

setupGuard(router)

export default router
