/**
 * 自写 Mock 中间件（替代 vite-plugin-mock）。
 * 原因：vite-plugin-mock 依赖 bundle-require 反复创建/删除临时文件，在本机安全策略下会被拦截导致启动失败。
 * 约定：mock 路由不含 /api 前缀，与后端 context-path 之后的路径一致；数据由 design/30-接口契约 派生。
 */

const productList = {
  records: [
    { id: '1001', name: '示例商品 A', subtitle: '演示数据，非真实商品', price: '99.00', stock: 20, mainImage: '/files/demo-a.jpg', status: 1, specJson: null },
    { id: '1002', name: '示例商品 B', subtitle: '演示数据，非真实商品', price: '199.00', stock: 5, mainImage: '/files/demo-b.jpg', status: 1, specJson: '[{"name":"颜色","values":["黑","白"]}]' }
  ],
  total: 2,
  current: 1,
  size: 10,
  pages: 1
}

const routes = [
  {
    method: 'GET',
    path: '/front/products',
    handler: () => ({ code: 0, msg: 'ok', data: productList })
  }
]

export function mockPlugin(env) {
  const enabled = env.VITE_USE_MOCK === 'true'
  return {
    name: 'mall-mock',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (!enabled) return next()
        const raw = req.url || ''
        if (!raw.startsWith('/api/')) return next()

        const url = new URL(raw, 'http://localhost')
        const path = url.pathname.replace(/^\/api/, '')
        const method = (req.method || 'GET').toUpperCase()
        const hit = routes.find((r) => r.method === method && r.path === path)
        if (!hit) return next()

        const query = Object.fromEntries(url.searchParams.entries())
        res.setHeader('Content-Type', 'application/json; charset=utf-8')
        res.statusCode = 200
        res.end(JSON.stringify(hit.handler(query, {})))
      })
    }
  }
}
