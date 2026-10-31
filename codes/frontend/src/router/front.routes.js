/** 前台路由：统一挂在 FrontLayout 下（顶栏 + 内容 + 页脚） */
export default {
  path: '/',
  component: () => import('@/layout/FrontLayout.vue'),
  children: [
    {
      path: '',
      name: 'home',
      component: () => import('@/views/front/Home.vue'),
      meta: { title: '首页' }
    },
    {
      path: 'products',
      name: 'product-list',
      component: () => import('@/views/front/ProductList.vue'),
      meta: { title: '全部商品' }
    },
    {
      path: 'products/:id',
      name: 'product-detail',
      component: () => import('@/views/front/ProductDetail.vue'),
      meta: { title: '商品详情' }
    },
    {
      path: 'cart',
      name: 'cart',
      component: () => import('@/views/front/Cart.vue'),
      meta: { title: '购物车', requiresAuth: true }
    },
    {
      path: 'orders/confirm',
      name: 'order-confirm',
      component: () => import('@/views/front/OrderConfirm.vue'),
      meta: { title: '确认订单', requiresAuth: true }
    },
    {
      path: 'orders',
      name: 'order-list',
      component: () => import('@/views/front/OrderList.vue'),
      meta: { title: '我的订单', requiresAuth: true }
    },
    {
      path: 'orders/:id',
      name: 'order-detail',
      component: () => import('@/views/front/OrderDetail.vue'),
      meta: { title: '订单详情', requiresAuth: true }
    },
    {
      path: 'favorites',
      name: 'favorite',
      component: () => import('@/views/front/Favorite.vue'),
      meta: { title: '我的收藏', requiresAuth: true }
    },
    {
      path: 'profile',
      name: 'profile',
      component: () => import('@/views/front/Profile.vue'),
      meta: { title: '个人中心', requiresAuth: true }
    }
  ]
}
