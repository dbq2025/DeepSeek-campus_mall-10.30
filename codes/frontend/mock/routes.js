/**
 * Mock 路由表。与 design/30-接口契约.md 一一对应，每条标注契约编号。
 * handler 返回 { code, msg, data }；业务失败用 fail() 抛出，由中间件转成统一返回体。
 */
import {
  CARTS,
  CATEGORIES,
  FAVORITES,
  ORDERS,
  PRODUCTS,
  USERS,
  issueToken,
  makeOrderNo,
  nextId,
  now,
  publicUser,
  today,
  daysAgo
} from './db.js'

/* ---------------- 通用 ---------------- */

export class MockError extends Error {
  constructor(code, msg, status = 200) {
    super(msg)
    this.code = code
    this.status = status
  }
}

const ok = (data = null) => ({ code: 0, msg: 'ok', data })

function fail(code, msg) {
  throw new MockError(code, msg)
}

function pageOf(list, query = {}) {
  const page = Math.max(1, Number(query.page) || 1)
  const size = Math.max(1, Number(query.size) || 10)
  const total = list.length
  const pages = Math.ceil(total / size)
  return {
    records: list.slice((page - 1) * size, page * size),
    total,
    current: page,
    size,
    pages
  }
}

function cents(price) {
  return Math.round(Number(price) * 100)
}

function money(value) {
  return (value / 100).toFixed(2)
}

function sumMoney(list) {
  return money(list.reduce((acc, item) => acc + cents(item.price) * Number(item.quantity || 0), 0))
}

const STATUS_TEXT = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }

function categoryName(categoryId) {
  const hit = CATEGORIES.find((c) => c.id === categoryId)
  return hit ? hit.name : ''
}

function productItem(product) {
  return {
    id: product.id,
    name: product.name,
    subtitle: product.subtitle,
    mainImage: product.mainImage,
    price: product.price,
    stock: product.stock,
    sales: product.sales,
    categoryId: product.categoryId,
    categoryName: categoryName(product.categoryId)
  }
}

function productAdminVO(product) {
  return {
    ...productItem(product),
    status: product.status,
    createTime: product.createTime
  }
}

function parseSpecs(specJson) {
  if (!specJson) return []
  try {
    const list = JSON.parse(specJson)
    return Array.isArray(list) ? list : []
  } catch (e) {
    return []
  }
}

/** 规格校验：有 spec_json 必须给全且合法；无 spec_json 不允许带规格 */
function specTextValid(product, specText) {
  const specs = parseSpecs(product.specJson)
  if (!specs.length) return !specText
  const picked = {}
  String(specText || '')
    .split(';')
    .filter(Boolean)
    .forEach((pair) => {
      const index = pair.indexOf(':')
      if (index > 0) picked[pair.slice(0, index).trim()] = pair.slice(index + 1).trim()
    })
  return specs.every((spec) => Array.isArray(spec.values) && spec.values.includes(picked[spec.name]))
}

/** 规格规范化：按 spec_json 定义顺序重排，保证购物车唯一键稳定 */
function normalizeSpecText(product, specText) {
  const specs = parseSpecs(product.specJson)
  if (!specs.length) return ''
  const picked = {}
  String(specText || '')
    .split(';')
    .filter(Boolean)
    .forEach((pair) => {
      const index = pair.indexOf(':')
      if (index > 0) picked[pair.slice(0, index).trim()] = pair.slice(index + 1).trim()
    })
  return specs.map((spec) => `${spec.name}:${picked[spec.name]}`).join(';')
}

function userActions(order) {
  if (order.status === 0) {
    return [
      { action: 'pay', label: '去支付', type: 'primary' },
      { action: 'cancel', label: '取消订单', type: 'default' }
    ]
  }
  if (order.status === 2) return [{ action: 'confirm', label: '确认收货', type: 'primary' }]
  return []
}

function adminActions(order) {
  if (order.status === 1) return [{ action: 'ship', label: '发货', type: 'primary' }]
  if (order.status === 2) return [{ action: 'confirm', label: '确认收货', type: 'primary' }]
  return []
}

function orderVO(order, admin = false) {
  const vo = {
    id: order.id,
    orderNo: order.orderNo,
    status: order.status,
    statusText: STATUS_TEXT[order.status],
    payAmount: order.payAmount,
    createTime: order.createTime,
    items: order.items.map((item) => ({ ...item })),
    actions: admin ? adminActions(order) : userActions(order)
  }
  if (admin) {
    const owner = USERS.find((u) => u.id === order.userId)
    vo.userName = owner ? owner.nickname || owner.username : '未知用户'
  }
  return vo
}

function orderDetailVO(order, admin = false) {
  return {
    ...orderVO(order, admin),
    totalAmount: order.totalAmount,
    receiverName: order.receiverName,
    receiverPhone: order.receiverPhone,
    receiverAddress: order.receiverAddress,
    remark: order.remark,
    trackingNo: order.trackingNo,
    payTime: order.payTime,
    shipTime: order.shipTime,
    finishTime: order.finishTime,
    cancelTime: order.cancelTime,
    cancelReason: order.cancelReason
  }
}

function cartVO(userId) {
  const rows = CARTS[userId] || []
  const items = rows.map((row) => {
    const product = PRODUCTS.find((p) => p.id === row.productId) || {}
    return {
      id: row.id,
      productId: row.productId,
      productName: product.name || '商品已删除',
      mainImage: product.mainImage || '',
      price: product.price || '0.00',
      quantity: row.quantity,
      specText: row.specText,
      stock: product.stock ?? 0,
      productStatus: product.status ?? 0
    }
  })
  return {
    items,
    totalQuantity: items.reduce((acc, item) => acc + Number(item.quantity || 0), 0),
    totalAmount: sumMoney(items)
  }
}

function findProduct(id) {
  return PRODUCTS.find((p) => p.id === String(id) && !p.deleted)
}

function requireProduct(id) {
  const product = findProduct(id)
  if (!product) fail(3003, '商品不存在')
  return product
}

function findOrder(id, user) {
  const order = ORDERS.find((o) => o.id === String(id))
  if (!order) fail(3003, '订单不存在')
  if (user && user.role !== 'ADMIN' && order.userId !== user.id) fail(2003, '无权查看该订单')
  return order
}

/* ---------------- 认证 ---------------- */

function doLogin(body, requireAdmin) {
  const username = String(body.username || '').trim()
  const password = String(body.password || '')
  if (!username || !password) fail(1001, '请输入账号和密码')
  const user = USERS.find((u) => u.username === username)
  if (!user || user.password !== password) fail(2004, '账号或密码错误')
  if (user.status !== 1) fail(2003, '账号已被禁用')
  if (requireAdmin && user.role !== 'ADMIN') fail(2003, '该账号没有后台权限')
  return ok({ token: issueToken(user.id), userInfo: publicUser(user) })
}

const authHandlers = {
  // AUTH-01
  login: ({ body }) => doLogin(body || {}, false),
  // AUTH-02
  register: ({ body }) => {
    const username = String(body.username || '').trim()
    const password = String(body.password || '')
    const nickname = String(body.nickname || '').trim()
    if (!/^\w{4,20}$/.test(username)) fail(1001, '用户名为 4–20 位字母、数字或下划线')
    if (!/^\S{6,20}$/.test(password)) fail(1001, '密码为 6–20 位且不含空格')
    if (!nickname) fail(1001, '请输入昵称')
    if (USERS.some((u) => u.username === username)) fail(3004, '用户名已被占用')
    const user = {
      id: nextId(),
      username,
      password,
      nickname,
      email: body.email || '',
      phone: '',
      avatar: '',
      role: 'USER',
      status: 1
    }
    USERS.push(user)
    return ok({ token: issueToken(user.id), userInfo: publicUser(user) })
  },
  // AUTH-03
  adminLogin: ({ body }) => doLogin(body || {}, true),
  // AUTH-04
  me: ({ user }) => ok(publicUser(user)),
  // AUTH-05
  logout: () => ok(null)
}

/* ---------------- 前台商品 ---------------- */

const productHandlers = {
  // FPRD-01
  list: ({ query }) => {
    let list = PRODUCTS.filter((p) => !p.deleted && p.status === 1)
    const keyword = String(query.keyword || '').trim()
    if (keyword) {
      const lower = keyword.toLowerCase()
      list = list.filter(
        (p) =>
          p.name.toLowerCase().includes(lower) ||
          String(p.subtitle || '').toLowerCase().includes(lower)
      )
    }
    if (query.categoryId) list = list.filter((p) => p.categoryId === String(query.categoryId))

    const sort = query.sort || 'default'
    const sorters = {
      price_asc: (a, b) => cents(a.price) - cents(b.price),
      price_desc: (a, b) => cents(b.price) - cents(a.price),
      sales_desc: (a, b) => b.sales - a.sales,
      new: (a, b) => String(b.createTime).localeCompare(String(a.createTime))
    }
    if (sorters[sort]) list = [...list].sort(sorters[sort])

    const page = pageOf(list, query)
    return ok({ ...page, records: page.records.map(productItem) })
  },

  // FPRD-02
  detail: ({ params, user }) => {
    const product = PRODUCTS.find((p) => p.id === String(params.id) && !p.deleted)
    if (!product) fail(3003, '商品不存在')
    if (product.status !== 1) fail(3005, '商品已下架')
    const favorited = user ? FAVORITES.some((f) => f.userId === user.id && f.productId === product.id) : false
    return ok({
      ...productItem(product),
      specJson: product.specJson,
      detail: product.detail,
      createTime: product.createTime,
      favorited
    })
  },

  // FPRD-03
  categories: () => ok([...CATEGORIES].sort((a, b) => a.sort - b.sort).map((c) => ({ ...c })))
}

/* ---------------- 购物车 ---------------- */

const cartHandlers = {
  // CART-01
  list: ({ user }) => ok(cartVO(user.id)),

  // CART-02
  add: ({ body, user }) => {
    const product = requireProduct(body.productId)
    if (product.status !== 1) fail(3005, '商品已下架')
    const quantity = Number(body.quantity)
    if (!Number.isInteger(quantity) || quantity < 1) fail(1001, '购买数量必须为正整数')
    if (!specTextValid(product, body.specText)) fail(1001, '请选择完整的商品规格')
    const specText = normalizeSpecText(product, body.specText)

    if (!CARTS[user.id]) CARTS[user.id] = []
    const exist = CARTS[user.id].find((r) => r.productId === product.id && r.specText === specText)
    if (exist) {
      exist.quantity += quantity
    } else {
      CARTS[user.id].push({
        id: nextId(),
        productId: product.id,
        quantity,
        specText,
        createTime: now()
      })
    }
    const vo = cartVO(user.id)
    const hit = vo.items.find((i) => i.productId === product.id && i.specText === specText)
    return ok(hit || null)
  },

  // CART-03
  update: ({ params, body, user }) => {
    const rows = CARTS[user.id] || []
    const row = rows.find((r) => r.id === String(params.id))
    if (!row) fail(3003, '购物车记录不存在')
    const quantity = Number(body.quantity)
    if (!Number.isInteger(quantity) || quantity < 1) fail(1001, '购买数量必须为正整数')
    const product = findProduct(row.productId)
    if (product && quantity > product.stock) fail(3001, `库存不足，仅剩 ${product.stock} 件`)
    row.quantity = quantity
    const vo = cartVO(user.id)
    return ok(vo.items.find((i) => i.id === row.id) || null)
  },

  // CART-04
  remove: ({ params, user }) => {
    const rows = CARTS[user.id] || []
    const index = rows.findIndex((r) => r.id === String(params.id))
    if (index < 0) fail(3003, '购物车记录不存在')
    rows.splice(index, 1)
    return ok(null)
  },

  // CART-05
  clear: ({ user }) => {
    CARTS[user.id] = []
    return ok(null)
  }
}

/* ---------------- 前台订单 ---------------- */

const orderHandlers = {
  // FORD-01
  create: ({ body, user }) => {
    const data = body || {}
    const receiverName = String(data.receiverName || '').trim()
    const receiverPhone = String(data.receiverPhone || '').trim()
    const receiverAddress = String(data.receiverAddress || '').trim()
    if (!receiverName) fail(1001, '请输入收货人')
    if (!/^1[3-9]\d{9}$/.test(receiverPhone)) fail(1001, '手机号格式不正确')
    if (!receiverAddress) fail(1001, '请输入收货地址')

    let source = []
    let cartIds = []
    if (data.fromCart) {
      cartIds = Array.isArray(data.cartIds) ? data.cartIds.map(String) : []
      if (!cartIds.length) fail(1001, '请选择要结算的商品')
      const rows = CARTS[user.id] || []
      const chosen = rows.filter((r) => cartIds.includes(r.id))
      if (!chosen.length) fail(3003, '购物车记录不存在')
      source = chosen.map((r) => ({ productId: r.productId, quantity: r.quantity, specText: r.specText }))
    } else {
      const items = Array.isArray(data.items) ? data.items : []
      if (!items.length) fail(1001, '下单商品不能为空')
      source = items.map((i) => ({
        productId: String(i.productId),
        quantity: Number(i.quantity),
        specText: i.specText || ''
      }))
    }

    // 先全量校验，全部通过后再扣减，避免部分扣减
    const prepared = source.map((row) => {
      const product = findProduct(row.productId)
      if (!product) fail(3003, '商品不存在')
      if (product.status !== 1) fail(3005, `「${product.name}」已下架`)
      if (!Number.isInteger(row.quantity) || row.quantity < 1) fail(1001, '购买数量必须为正整数')
      if (product.stock < row.quantity) {
        fail(3001, `「${product.name}」库存不足，仅剩 ${product.stock} 件`)
      }
      if (!specTextValid(product, row.specText)) fail(1001, '请选择完整的商品规格')
      return { product, quantity: row.quantity, specText: normalizeSpecText(product, row.specText) }
    })

    prepared.forEach((row) => {
      row.product.stock -= row.quantity
      row.product.sales += row.quantity
    })

    const items = prepared.map((row) => ({
      id: nextId(),
      productId: row.product.id,
      productName: row.product.name,
      productImage: row.product.mainImage,
      price: row.product.price,
      quantity: row.quantity,
      specText: row.specText,
      totalAmount: money(cents(row.product.price) * row.quantity)
    }))

    const totalAmount = money(items.reduce((acc, item) => acc + cents(item.totalAmount), 0))
    const order = {
      id: nextId(),
      orderNo: makeOrderNo(),
      userId: user.id,
      totalAmount,
      payAmount: totalAmount,
      status: 0,
      receiverName,
      receiverPhone,
      receiverAddress,
      remark: String(data.remark || ''),
      trackingNo: null,
      payTime: null,
      shipTime: null,
      finishTime: null,
      cancelTime: null,
      cancelReason: null,
      createTime: now(),
      items
    }
    ORDERS.unshift(order)

    if (data.fromCart) {
      CARTS[user.id] = (CARTS[user.id] || []).filter((r) => !cartIds.includes(r.id))
    }

    return ok({ orderId: order.id, orderNo: order.orderNo, payAmount: order.payAmount })
  },

  // FORD-02
  list: ({ query, user }) => {
    let list = ORDERS.filter((o) => o.userId === user.id)
    if (query.status !== undefined && query.status !== '') {
      list = list.filter((o) => o.status === Number(query.status))
    }
    list = [...list].sort((a, b) => String(b.createTime).localeCompare(String(a.createTime)))
    const page = pageOf(list, query)
    return ok({ ...page, records: page.records.map((o) => orderVO(o)) })
  },

  // FORD-03
  detail: ({ params, user }) => ok(orderDetailVO(findOrder(params.id, user))),

  // FORD-04
  pay: ({ params, user }) => {
    const order = findOrder(params.id, user)
    if (order.status === 1) return ok(null) // 幂等
    if (order.status !== 0) fail(3002, '当前状态不允许支付')
    order.status = 1
    order.payTime = now()
    order.payAmount = order.totalAmount
    return ok(null)
  },

  // FORD-05
  cancel: ({ params, body, user }) => {
    const order = findOrder(params.id, user)
    if (order.status === 4) return ok(null) // 幂等，且不会二次回补
    if (order.status !== 0) fail(3002, '已支付的订单不能直接取消')
    order.status = 4
    order.cancelTime = now()
    order.cancelReason = String(body?.reason || '用户取消')
    // 回补库存：只在这一条路径上执行一次
    order.items.forEach((item) => {
      const product = PRODUCTS.find((p) => p.id === item.productId)
      if (product) {
        product.stock += item.quantity
        product.sales = Math.max(0, product.sales - item.quantity)
      }
    })
    return ok(null)
  },

  // FORD-06
  confirm: ({ params, user }) => {
    const order = findOrder(params.id, user)
    if (order.status === 3) return ok(null)
    if (order.status !== 2) fail(3002, '当前状态不允许确认收货')
    order.status = 3
    order.finishTime = now()
    return ok(null)
  }
}

/* ---------------- 收藏 ---------------- */

const favoriteHandlers = {
  // FAV-01
  list: ({ query, user }) => {
    const rows = FAVORITES.filter((f) => f.userId === user.id).sort((a, b) =>
      String(b.createTime).localeCompare(String(a.createTime))
    )
    const mapped = rows.map((f) => {
      const product = PRODUCTS.find((p) => p.id === f.productId && !p.deleted)
      return product
        ? { ...productItem(product), favoriteId: f.id, favoriteTime: f.createTime }
        : null
    })
    const list = mapped.filter(Boolean)
    const page = pageOf(list, query)
    return ok(page)
  },

  // FAV-02
  add: ({ body, user }) => {
    const product = requireProduct(body.productId)
    if (product.status !== 1) fail(3005, '商品已下架')
    if (FAVORITES.some((f) => f.userId === user.id && f.productId === product.id)) {
      fail(3004, '已经收藏过了')
    }
    FAVORITES.push({ id: nextId(), userId: user.id, productId: product.id, createTime: now() })
    return ok(null)
  },

  // FAV-03
  remove: ({ params, user }) => {
    const index = FAVORITES.findIndex((f) => f.userId === user.id && f.productId === String(params.productId))
    if (index < 0) fail(3003, '尚未收藏该商品')
    FAVORITES.splice(index, 1)
    return ok(null)
  },

  // FAV-04
  check: ({ query, user }) => {
    const favorited = FAVORITES.some((f) => f.userId === user.id && f.productId === String(query.productId))
    return ok({ favorited })
  }
}

/* ---------------- 后台类型 ---------------- */

const categoryHandlers = {
  // CAT-01
  options: () => ok([...CATEGORIES].sort((a, b) => a.sort - b.sort).map((c) => ({ ...c }))),

  // CAT-02
  page: ({ query }) => {
    let list = [...CATEGORIES]
    const keyword = String(query.keyword || '').trim()
    if (keyword) list = list.filter((c) => c.name.includes(keyword))
    list.sort((a, b) => a.sort - b.sort)
    const page = pageOf(list, query)
    return ok({
      ...page,
      records: page.records.map((c) => ({
        ...c,
        productCount: PRODUCTS.filter((p) => p.categoryId === c.id && !p.deleted).length
      }))
    })
  },

  // CAT-03
  create: ({ body }) => {
    const name = String(body.name || '').trim()
    if (!name) fail(1001, '请输入类型名称')
    if (CATEGORIES.some((c) => c.name === name)) fail(3004, '类型名称已存在')
    CATEGORIES.push({
      id: nextId(),
      name,
      sort: Number(body.sort) || 0,
      createTime: now()
    })
    return ok(null)
  },

  // CAT-04
  update: ({ params, body }) => {
    const category = CATEGORIES.find((c) => c.id === String(params.id))
    if (!category) fail(3003, '类型不存在')
    const name = String(body.name || '').trim()
    if (!name) fail(1001, '请输入类型名称')
    if (CATEGORIES.some((c) => c.id !== category.id && c.name === name)) fail(3004, '类型名称已存在')
    category.name = name
    category.sort = Number(body.sort) || 0
    return ok(null)
  },

  // CAT-05
  remove: ({ params }) => {
    const index = CATEGORIES.findIndex((c) => c.id === String(params.id))
    if (index < 0) fail(3003, '类型不存在')
    const used = PRODUCTS.some((p) => p.categoryId === String(params.id) && !p.deleted)
    if (used) fail(3002, '该类型下还有商品，无法删除')
    CATEGORIES.splice(index, 1)
    return ok(null)
  }
}

/* ---------------- 后台商品 ---------------- */

function validateProductForm(body) {
  const name = String(body.name || '').trim()
  if (!name) fail(1001, '请输入商品名称')
  if (!CATEGORIES.some((c) => c.id === String(body.categoryId))) fail(1001, '请选择商品类型')
  if (!/^\d+(\.\d{1,2})?$/.test(String(body.price))) fail(1001, '价格格式不正确')
  if (!/^\d+$/.test(String(body.stock))) fail(1001, '库存必须为非负整数')
  if (body.specJson) {
    let parsed
    try {
      parsed = JSON.parse(body.specJson)
    } catch (e) {
      fail(1001, '规格 JSON 格式不正确')
    }
    if (!Array.isArray(parsed)) fail(1001, '规格必须是数组')
    parsed.forEach((spec) => {
      if (!spec || !spec.name || !Array.isArray(spec.values) || !spec.values.length) {
        fail(1001, '规格项缺少名称或取值')
      }
    })
  }
}

const adminProductHandlers = {
  // PRD-01
  page: ({ query }) => {
    let list = PRODUCTS.filter((p) => !p.deleted)
    const keyword = String(query.keyword || '').trim()
    if (keyword) list = list.filter((p) => p.name.includes(keyword) || String(p.subtitle || '').includes(keyword))
    if (query.categoryId) list = list.filter((p) => p.categoryId === String(query.categoryId))
    if (query.status !== undefined && query.status !== '') {
      list = list.filter((p) => p.status === Number(query.status))
    }
    list = [...list].sort((a, b) => String(b.createTime).localeCompare(String(a.createTime)))
    const page = pageOf(list, query)
    return ok({ ...page, records: page.records.map(productAdminVO) })
  },

  // PRD-02
  detail: ({ params }) => {
    const product = findProduct(params.id)
    if (!product) fail(3003, '商品不存在')
    return ok({
      ...productAdminVO(product),
      specJson: product.specJson,
      detail: product.detail
    })
  },

  // PRD-03
  create: ({ body }) => {
    validateProductForm(body)
    const product = {
      id: nextId(),
      categoryId: String(body.categoryId),
      name: String(body.name).trim(),
      subtitle: body.subtitle || '',
      mainImage: body.mainImage || '',
      price: Number(body.price).toFixed(2),
      stock: Number(body.stock),
      specJson: body.specJson || null,
      detail: body.detail || '',
      status: Number(body.status) === 0 ? 0 : 1,
      sales: 0,
      deleted: 0,
      createTime: now()
    }
    PRODUCTS.unshift(product)
    return ok({ id: product.id })
  },

  // PRD-04
  update: ({ params, body }) => {
    const product = findProduct(params.id)
    if (!product) fail(3003, '商品不存在')
    validateProductForm(body)
    product.categoryId = String(body.categoryId)
    product.name = String(body.name).trim()
    product.subtitle = body.subtitle || ''
    product.mainImage = body.mainImage || ''
    product.price = Number(body.price).toFixed(2)
    product.stock = Number(body.stock)
    product.specJson = body.specJson || null
    product.detail = body.detail || ''
    product.status = Number(body.status) === 0 ? 0 : 1
    return ok(null)
  },

  // PRD-05
  updateStatus: ({ params, body }) => {
    const product = findProduct(params.id)
    if (!product) fail(3003, '商品不存在')
    product.status = Number(body.status) === 0 ? 0 : 1
    return ok(null)
  },

  // PRD-06
  batchStatus: ({ body }) => {
    const ids = Array.isArray(body.ids) ? body.ids.map(String) : []
    if (!ids.length) fail(1001, '请选择商品')
    const status = Number(body.status) === 0 ? 0 : 1
    PRODUCTS.forEach((p) => {
      if (ids.includes(p.id) && !p.deleted) p.status = status
    })
    return ok(null)
  },

  // PRD-07
  remove: ({ params }) => {
    const product = findProduct(params.id)
    if (!product) fail(3003, '商品不存在')
    product.deleted = 1
    return ok(null)
  }
}

/* ---------------- 后台订单 ---------------- */

const adminOrderHandlers = {
  // ORD-01
  page: ({ query }) => {
    let list = [...ORDERS]
    const orderNo = String(query.orderNo || '').trim()
    if (orderNo) list = list.filter((o) => o.orderNo.includes(orderNo))
    if (query.status !== undefined && query.status !== '') {
      list = list.filter((o) => o.status === Number(query.status))
    }
    list.sort((a, b) => String(b.createTime).localeCompare(String(a.createTime)))
    const page = pageOf(list, query)
    return ok({ ...page, records: page.records.map((o) => orderVO(o, true)) })
  },

  // ORD-02
  detail: ({ params }) => ok(orderDetailVO(findOrder(params.id, null), true)),

  // ORD-03
  ship: ({ params, body }) => {
    const order = findOrder(params.id, null)
    if (order.status !== 1) fail(3002, '只有待发货订单可以发货')
    order.status = 2
    order.shipTime = now()
    order.trackingNo = String(body?.trackingNo || '').trim() || null
    return ok(null)
  },

  // ORD-04
  confirm: ({ params }) => {
    const order = findOrder(params.id, null)
    if (order.status === 3) return ok(null)
    if (order.status !== 2) fail(3002, '只有待收货订单可以确认收货')
    order.status = 3
    order.finishTime = now()
    return ok(null)
  }
}

/* ---------------- 后台统计 ---------------- */

const statHandlers = {
  // STAT-01
  overview: () => {
    const day = today()
    const todayOrders = ORDERS.filter((o) => String(o.createTime).startsWith(day) && o.status !== 4)
    const todaySales = ORDERS.filter((o) => String(o.payTime || '').startsWith(day))
    return ok({
      todayOrderCount: todayOrders.length,
      todaySalesAmount: money(todaySales.reduce((acc, o) => acc + cents(o.payAmount), 0)),
      pendingShipmentCount: ORDERS.filter((o) => o.status === 1).length,
      totalProductCount: PRODUCTS.filter((p) => !p.deleted).length,
      totalUserCount: USERS.length
    })
  },

  // STAT-02
  sales: ({ query }) => {
    const days = Math.min(Math.max(Number(query.days) || 7, 1), 30)
    const dates = []
    const amounts = []
    for (let i = days - 1; i >= 0; i -= 1) {
      const date = daysAgo(i)
      dates.push(date)
      const total = ORDERS.filter((o) => String(o.payTime || '').startsWith(date)).reduce(
        (acc, o) => acc + cents(o.payAmount),
        0
      )
      amounts.push(money(total))
    }
    return ok({ dates, amounts })
  },

  // STAT-03
  orderStatus: () =>
    ok(
      [0, 1, 2, 3, 4].map((status) => ({
        status,
        statusText: STATUS_TEXT[status],
        count: ORDERS.filter((o) => o.status === status).length
      }))
    ),

  // STAT-04
  topProducts: ({ query }) => {
    const limit = Math.min(Math.max(Number(query.limit) || 5, 1), 20)
    return ok(
      [...PRODUCTS]
        .filter((p) => !p.deleted)
        .sort((a, b) => b.sales - a.sales)
        .slice(0, limit)
        .map((p) => ({ productId: p.id, productName: p.name, sales: p.sales }))
    )
  }
}

/* ---------------- 文件 ---------------- */

let uploadSeed = 0

const fileHandlers = {
  // FILE-01：不真正落盘，返回一张本地占位图，保证离线可预览
  upload: ({ req }) => {
    req.resume()
    uploadSeed += 1
    return ok({ url: `/files/p${(uploadSeed % 8) + 1}.svg` })
  }
}

/* ---------------- 路由表 ---------------- */

export const ROUTES = [
  // 认证
  { method: 'POST', path: '/auth/login', handler: authHandlers.login },
  { method: 'POST', path: '/auth/register', handler: authHandlers.register },
  { method: 'POST', path: '/auth/admin/login', handler: authHandlers.adminLogin },
  { method: 'GET', path: '/auth/me', auth: 'USER', handler: authHandlers.me },
  { method: 'POST', path: '/auth/logout', handler: authHandlers.logout },

  // 前台商品
  { method: 'GET', path: '/front/products', handler: productHandlers.list },
  { method: 'GET', path: '/front/products/:id', handler: productHandlers.detail, optionalAuth: true },
  { method: 'GET', path: '/front/categories', handler: productHandlers.categories },

  // 购物车
  { method: 'GET', path: '/front/cart', auth: 'USER', handler: cartHandlers.list },
  { method: 'POST', path: '/front/cart', auth: 'USER', handler: cartHandlers.add },
  { method: 'PUT', path: '/front/cart/:id', auth: 'USER', handler: cartHandlers.update },
  { method: 'DELETE', path: '/front/cart/:id', auth: 'USER', handler: cartHandlers.remove },
  { method: 'DELETE', path: '/front/cart', auth: 'USER', handler: cartHandlers.clear },

  // 订单
  { method: 'POST', path: '/front/orders', auth: 'USER', handler: orderHandlers.create },
  { method: 'GET', path: '/front/orders', auth: 'USER', handler: orderHandlers.list },
  { method: 'GET', path: '/front/orders/:id', auth: 'USER', handler: orderHandlers.detail },
  { method: 'POST', path: '/front/orders/:id/pay', auth: 'USER', handler: orderHandlers.pay },
  { method: 'POST', path: '/front/orders/:id/cancel', auth: 'USER', handler: orderHandlers.cancel },
  { method: 'PUT', path: '/front/orders/:id/confirm', auth: 'USER', handler: orderHandlers.confirm },

  // 收藏
  { method: 'GET', path: '/front/favorites', auth: 'USER', handler: favoriteHandlers.list },
  { method: 'GET', path: '/front/favorites/check', auth: 'USER', handler: favoriteHandlers.check },
  { method: 'POST', path: '/front/favorites', auth: 'USER', handler: favoriteHandlers.add },
  { method: 'DELETE', path: '/front/favorites/:productId', auth: 'USER', handler: favoriteHandlers.remove },

  // 后台类型
  { method: 'GET', path: '/admin/categories', auth: 'ADMIN', handler: categoryHandlers.options },
  { method: 'GET', path: '/admin/categories/page', auth: 'ADMIN', handler: categoryHandlers.page },
  { method: 'POST', path: '/admin/categories', auth: 'ADMIN', handler: categoryHandlers.create },
  { method: 'PUT', path: '/admin/categories/:id', auth: 'ADMIN', handler: categoryHandlers.update },
  { method: 'DELETE', path: '/admin/categories/:id', auth: 'ADMIN', handler: categoryHandlers.remove },

  // 后台商品
  { method: 'GET', path: '/admin/products', auth: 'ADMIN', handler: adminProductHandlers.page },
  { method: 'PUT', path: '/admin/products/status', auth: 'ADMIN', handler: adminProductHandlers.batchStatus },
  { method: 'GET', path: '/admin/products/:id', auth: 'ADMIN', handler: adminProductHandlers.detail },
  { method: 'POST', path: '/admin/products', auth: 'ADMIN', handler: adminProductHandlers.create },
  { method: 'PUT', path: '/admin/products/:id', auth: 'ADMIN', handler: adminProductHandlers.update },
  { method: 'PUT', path: '/admin/products/:id/status', auth: 'ADMIN', handler: adminProductHandlers.updateStatus },
  { method: 'DELETE', path: '/admin/products/:id', auth: 'ADMIN', handler: adminProductHandlers.remove },

  // 后台订单
  { method: 'GET', path: '/admin/orders', auth: 'ADMIN', handler: adminOrderHandlers.page },
  { method: 'GET', path: '/admin/orders/:id', auth: 'ADMIN', handler: adminOrderHandlers.detail },
  { method: 'PUT', path: '/admin/orders/:id/ship', auth: 'ADMIN', handler: adminOrderHandlers.ship },
  { method: 'PUT', path: '/admin/orders/:id/confirm', auth: 'ADMIN', handler: adminOrderHandlers.confirm },

  // 后台统计
  { method: 'GET', path: '/admin/stats/overview', auth: 'ADMIN', handler: statHandlers.overview },
  { method: 'GET', path: '/admin/stats/sales', auth: 'ADMIN', handler: statHandlers.sales },
  { method: 'GET', path: '/admin/stats/order-status', auth: 'ADMIN', handler: statHandlers.orderStatus },
  { method: 'GET', path: '/admin/stats/top-products', auth: 'ADMIN', handler: statHandlers.topProducts },

  // 文件
  { method: 'POST', path: '/admin/files', auth: 'ADMIN', handler: fileHandlers.upload }
]
