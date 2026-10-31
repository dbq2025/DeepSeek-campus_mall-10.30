/** 后台路由：统一挂在 AdminLayout 下（侧栏 + 顶栏） */
export default {
  path: '/admin',
  component: () => import('@/layout/AdminLayout.vue'),
  meta: { requiresAdmin: true },
  children: [
    {
      path: '',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/Dashboard.vue'),
      meta: { title: '数据概览', requiresAdmin: true }
    },
    {
      path: 'categories',
      name: 'admin-category',
      component: () => import('@/views/admin/Category.vue'),
      meta: { title: '类型管理', requiresAdmin: true }
    },
    {
      path: 'products',
      name: 'admin-product-list',
      component: () => import('@/views/admin/ProductList.vue'),
      meta: { title: '商品管理', requiresAdmin: true }
    },
    {
      path: 'products/new',
      name: 'admin-product-new',
      component: () => import('@/views/admin/ProductEdit.vue'),
      meta: { title: '新增商品', requiresAdmin: true, activeMenu: '/admin/products' }
    },
    {
      path: 'products/:id/edit',
      name: 'admin-product-edit',
      component: () => import('@/views/admin/ProductEdit.vue'),
      meta: { title: '编辑商品', requiresAdmin: true, activeMenu: '/admin/products' }
    },
    {
      path: 'orders',
      name: 'admin-order-list',
      component: () => import('@/views/admin/OrderList.vue'),
      meta: { title: '订单管理', requiresAdmin: true }
    },
    {
      path: 'orders/:id',
      name: 'admin-order-detail',
      component: () => import('@/views/admin/OrderDetail.vue'),
      meta: { title: '订单详情', requiresAdmin: true, activeMenu: '/admin/orders' }
    },
    {
      path: 'stats',
      name: 'admin-stats',
      component: () => import('@/views/admin/Stats.vue'),
      meta: { title: '数据统计', requiresAdmin: true }
    }
  ]
}
