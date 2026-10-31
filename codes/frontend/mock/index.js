/**
 * 自写 Mock 中间件 v2（替代 vite-plugin-mock）。
 * 原因：vite-plugin-mock 依赖 bundle-require 反复创建/删除临时文件，在本机安全策略下会被拦截导致启动失败。
 *
 * v2 能力：
 *  1. 路径参数 /:id、query、JSON body
 *  2. 有状态数据（下单扣库存、取消回补、状态流转）
 *  3. 认证与角色校验（Bearer token，401/403 与真实后端一致）
 *  4. 统一返回体与错误码（与 Result/ResultCode 一致）
 *  5. 150–300ms 延迟，四态可观察
 *  6. POST /api/__mock/reset 一键复位
 */
import { ROUTES, MockError } from './routes.js'
import { resetState, userByToken } from './db.js'

const MAX_BODY = 2 * 1024 * 1024

function compile(path) {
  const keys = []
  const pattern = path.replace(/\/:([A-Za-z0-9_]+)/g, (_, key) => {
    keys.push(key)
    return '/([^/]+)'
  })
  return { regex: new RegExp(`^${pattern}$`), keys, paramCount: keys.length }
}

// 静态路径优先匹配（/admin/products/status 先于 /admin/products/:id）
const COMPILED = ROUTES.map((route) => ({ ...route, ...compile(route.path) })).sort(
  (a, b) => a.paramCount - b.paramCount
)

function send(res, status, body) {
  res.statusCode = status
  res.setHeader('Content-Type', 'application/json; charset=utf-8')
  res.setHeader('Cache-Control', 'no-store')
  res.end(JSON.stringify(body))
}

function readJsonBody(req) {
  return new Promise((resolve) => {
    const type = String(req.headers['content-type'] || '')
    if (type.includes('multipart/form-data')) return resolve(null)

    let raw = ''
    let done = false
    req.on('data', (chunk) => {
      if (done) return
      raw += chunk
      if (raw.length > MAX_BODY) {
        done = true
        resolve(null)
      }
    })
    req.on('end', () => {
      if (done) return
      done = true
      if (!raw.trim()) return resolve(null)
      try {
        resolve(JSON.parse(raw))
      } catch (e) {
        resolve(null)
      }
    })
    req.on('error', () => resolve(null))
  })
}

function resolveUser(req) {
  const header = req.headers.authorization || ''
  if (!header) return null
  return userByToken(header)
}

function delay() {
  return 120 + Math.floor(Math.random() * 180)
}

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
        let path = url.pathname.replace(/^\/api/, '')
        if (path.length > 1 && path.endsWith('/')) path = path.slice(0, -1)
        const method = (req.method || 'GET').toUpperCase()

        // 复位入口：把库存/订单/购物车恢复到种子状态
        if (path === '/__mock/reset') {
          resetState()
          return send(res, 200, { code: 0, msg: 'ok', data: { reset: true } })
        }

        // 内置健康检查：mock 模式下无需后端也能自检
        if (path === '/health') {
          return send(res, 200, {
            code: 0,
            msg: 'ok',
            data: { status: 'UP', mode: 'mock', time: new Date().toISOString() }
          })
        }

        const hit = COMPILED.find((route) => route.method === method && route.regex.test(path))
        if (!hit) {
          return send(res, 200, {
            code: 3003,
            msg: `mock 未定义该接口：${method} ${path}`,
            data: null
          })
        }

        const matched = hit.regex.exec(path)
        const params = {}
        hit.keys.forEach((key, index) => {
          params[key] = decodeURIComponent(matched[index + 1] || '')
        })
        const query = Object.fromEntries(url.searchParams.entries())

        readJsonBody(req).then((body) => {
          const user = resolveUser(req)
          const finish = () => {
            try {
              // 认证
              if (hit.auth === 'USER' && !user) {
                throw new MockError(2001, '未登录或缺少令牌', 401)
              }
              if (hit.auth === 'ADMIN') {
                if (!user) throw new MockError(2001, '未登录或缺少令牌', 401)
                if (user.role !== 'ADMIN') throw new MockError(2003, '无权限', 403)
              }

              const result = hit.handler({ req, res, params, query, body, user })
              send(res, 200, result)
            } catch (error) {
              if (error instanceof MockError) {
                send(res, error.status, { code: error.code, msg: error.message, data: null })
              } else {
                send(res, 500, { code: 5000, msg: `mock 内部错误：${error.message}`, data: null })
              }
            }
          }
          setTimeout(finish, delay())
        })
      })
    }
  }
}
