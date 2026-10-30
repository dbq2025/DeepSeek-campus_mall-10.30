import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/front/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '管理员登录' }
  },
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
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫：只做"藏"，真正的权限边界在后端
router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 校园电商平台` : '校园电商平台'
  if (to.meta.requiresAuth && !localStorage.getItem('mall_token')) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
