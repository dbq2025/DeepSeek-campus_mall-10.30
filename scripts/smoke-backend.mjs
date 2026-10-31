/**
 * 阶段 5 后端接口冒烟脚本
 * 前置：后端已启动在 http://localhost:8080，MySQL/Redis 可用，data.sql 已导入。
 * 运行：node scripts/smoke-backend.mjs
 * 说明：只做接口层验收（状态码/业务码/关键字段），覆盖 44 个接口与主要边界。
 */
const BASE = process.env.SMOKE_BASE || 'http://localhost:8080/api'

let pass = 0
const failures = []

function check(name, cond, extra = '') {
  if (cond) {
    pass++
    console.log(`  ok   ${name}`)
  } else {
    failures.push(`${name}${extra ? ' -> ' + extra : ''}`)
    console.log(`  FAIL ${name}${extra ? ' -> ' + extra : ''}`)
  }
}

async function api(method, path, { token, body, form } = {}) {
  const headers = {}
  if (token) headers.Authorization = `Bearer ${token}`
  let payload
  if (form) {
    payload = form
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
    payload = JSON.stringify(body)
  }
  const res = await fetch(`${BASE}${path}`, { method, headers, body: payload })
  let json = null
  const text = await res.text()
  try {
    json = text ? JSON.parse(text) : null
  } catch {
    json = { raw: text }
  }
  return { status: res.status, body: json, data: json?.data, code: json?.code, msg: json?.msg }
}

function section(title) {
  console.log(`\n== ${title}`)
}

function tinyPng() {
  const b64 =
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=='
  return new Blob([Buffer.from(b64, 'base64')], { type: 'image/png' })
}

const run = async () => {
  section('0. 健康检查')
  const health = await api('GET', '/health')
  check('health 200/0', health.status === 200 && health.code === 0, `${health.status}/${health.code}`)
  check('MySQL 连通', health.data?.dbOk === true)
  check('Redis 生效', health.data?.cacheType === 'redis' && health.data?.cacheOk === true,
    JSON.stringify({ t: health.data?.cacheType, ok: health.data?.cacheOk }))

  section('1. AUTH 认证（5）')
  const admin = await api('POST', '/auth/admin/login', { body: { username: 'admin', password: '123456' } })
  check('AUTH-03 管理员登录', admin.code === 0 && admin.data?.token, JSON.stringify(admin.body).slice(0, 120))
  const adminToken = admin.data?.token

  const badPwd = await api('POST', '/auth/login', { body: { username: 'test', password: 'wrong' } })
  check('2004 密码错误', badPwd.code === 2004, String(badPwd.code))

  const userLogin = await api('POST', '/auth/login', { body: { username: 'test', password: '123456' } })
  check('AUTH-01 用户登录', userLogin.code === 0 && userLogin.data?.token)
  const userToken = userLogin.data?.token

  const adminAsUser = await api('POST', '/auth/login', { body: { username: 'admin', password: '123456' } })
  check('AUTH-01 管理员也可普通登录', adminAsUser.code === 0)

  const suffix = String(Date.now()).slice(-6)
  const newUser = `u${suffix}`
  const reg = await api('POST', '/auth/register', { body: { username: newUser, password: '123456', nickname: '冒烟用户' } })
  check('AUTH-02 注册即登录', reg.code === 0 && reg.data?.token && reg.data?.userInfo?.username === newUser,
    JSON.stringify(reg.body).slice(0, 120))

  const regDup = await api('POST', '/auth/register', { body: { username: newUser, password: '123456' } })
  check('3004 重复用户名', regDup.code === 3004, String(regDup.code))

  const regBad = await api('POST', '/auth/register', { body: { username: 'ab', password: '123456' } })
  check('1001 用户名过短 -> HTTP200', regBad.status === 200 && regBad.code === 1001, `${regBad.status}/${regBad.code}`)

  const me = await api('GET', '/auth/me', { token: userToken })
  check('AUTH-04 当前用户', me.code === 0 && me.data?.username === 'test')

  const meNoToken = await api('GET', '/auth/me')
  check('2001 未登录访问 /auth/me', meNoToken.status === 401 && meNoToken.code === 2001, `${meNoToken.status}/${meNoToken.code}`)

  const meBadToken = await api('GET', '/auth/me', { token: 'not-a-jwt' })
  check('2002 坏 token -> 2002', meBadToken.status === 401 && meBadToken.code === 2002, `${meBadToken.status}/${meBadToken.code}`)

  const logout = await api('POST', '/auth/logout', { token: userToken })
  check('AUTH-05 退出登录', logout.code === 0)

  section('2. FPRD 前台商品（3）')
  const list = await api('GET', '/front/products?page=1&size=5')
  check('FPRD-01 分页', list.code === 0 && list.data?.records?.length === 5 && typeof list.data.total === 'number',
    JSON.stringify({ len: list.data?.records?.length, total: list.data?.total }))
  check('分页字段是 number', typeof list.data?.total === 'number' && typeof list.data?.current === 'number')
  check('列表不含下架商品', list.data?.records?.every((p) => p.id !== '2012'))

  const kw = await api('GET', '/front/products?keyword=' + encodeURIComponent('耳机'))
  check('FPRD-01 关键词搜索', kw.code === 0 && kw.data.records.length >= 1)

  const byCat = await api('GET', '/front/products?categoryId=1002')
  check('FPRD-01 分类筛选', byCat.code === 0 && byCat.data.records.every((p) => p.categoryId === '1002'))

  const sorted = await api('GET', '/front/products?sort=price_asc&size=20')
  const prices = sorted.data.records.map((p) => Number(p.price))
  check('FPRD-01 价格升序', prices.every((v, i) => i === 0 || prices[i - 1] <= v))

  const detail = await api('GET', '/front/products/2001')
  check('FPRD-02 详情', detail.code === 0 && detail.data?.specJson && detail.data?.favorited === false)
  check('详情 ID 是字符串', typeof detail.data?.id === 'string')

  const offShelf = await api('GET', '/front/products/2012')
  check('3005 下架商品详情', offShelf.code === 3005, String(offShelf.code))

  const notExist = await api('GET', '/front/products/999999')
  check('3003 商品不存在', notExist.code === 3003, String(notExist.code))

  const cats = await api('GET', '/front/categories')
  check('FPRD-03 类型列表', cats.code === 0 && cats.data.length === 5 && cats.data[0].sort <= cats.data[1].sort)

  section('3. CART 购物车（5）')
  await api('DELETE', '/front/cart', { token: userToken })
  const add1 = await api('POST', '/front/cart', { token: userToken, body: { productId: '2001', quantity: 2, specText: '颜色:云雾白' } })
  check('CART-02 加购', add1.code === 0 && add1.data?.quantity === 2, JSON.stringify(add1.body).slice(0, 140))

  const add2 = await api('POST', '/front/cart', { token: userToken, body: { productId: '2001', quantity: 3, specText: '颜色:云雾白' } })
  check('同商品同规格合并', add2.code === 0 && add2.data?.quantity === 5, String(add2.data?.quantity))

  const add3 = await api('POST', '/front/cart', { token: userToken, body: { productId: '2001', quantity: 1, specText: '颜色:曜石黑' } })
  check('同商品不同规格两行', add3.code === 0 && add3.data?.id !== add1.data?.id)

  const badSpec = await api('POST', '/front/cart', { token: userToken, body: { productId: '2001', quantity: 1, specText: '颜色:紫色' } })
  check('1001 规格值非法', badSpec.code === 1001, String(badSpec.code))

  const noSpec = await api('POST', '/front/cart', { token: userToken, body: { productId: '2001', quantity: 1 } })
  check('1001 有规格却未选', noSpec.code === 1001, String(noSpec.code))

  const specOnNoSpec = await api('POST', '/front/cart', { token: userToken, body: { productId: '2003', quantity: 1, specText: '颜色:白' } })
  check('1001 无规格商品带规格', specOnNoSpec.code === 1001, String(specOnNoSpec.code))

  const cart = await api('GET', '/front/cart', { token: userToken })
  check('CART-01 列表', cart.code === 0 && cart.data.items.length === 2)
  check('CART-01 合计数量', cart.data.totalQuantity === 6, String(cart.data.totalQuantity))
  check('CART-01 合计金额为字符串两位小数', /^\d+\.\d{2}$/.test(cart.data.totalAmount), cart.data.totalAmount)

  const cartId = add1.data.id
  const upd = await api('PUT', `/front/cart/${cartId}`, { token: userToken, body: { quantity: 4 } })
  check('CART-03 改数量', upd.code === 0 && upd.data?.quantity === 4, String(upd.data?.quantity))

  const overStock = await api('PUT', `/front/cart/${cartId}`, { token: userToken, body: { quantity: 99999 } })
  check('3001 超库存改量', overStock.code === 3001, String(overStock.code))

  const cartNoToken = await api('GET', '/front/cart')
  check('2001 游客访问购物车', cartNoToken.status === 401 && cartNoToken.code === 2001, `${cartNoToken.status}/${cartNoToken.code}`)

  // 他人购物车行
  const otherLogin = await api('POST', '/auth/login', { body: { username: 'lisi', password: '123456' } })
  const otherToken = otherLogin.data?.token
  const steal = await api('PUT', `/front/cart/${cartId}`, { token: otherToken, body: { quantity: 1 } })
  check('3003 操作他人购物车行', steal.code === 3003, String(steal.code))

  section('4. FAV 收藏（4）')
  const favAdd = await api('POST', '/front/favorites', { token: userToken, body: { productId: '2001' } })
  check('FAV-02 添加收藏', favAdd.code === 0, String(favAdd.code))
  const favDup = await api('POST', '/front/favorites', { token: userToken, body: { productId: '2001' } })
  check('3004 重复收藏', favDup.code === 3004, String(favDup.code))
  const favCheck = await api('GET', '/front/favorites/check?productId=2001', { token: userToken })
  check('FAV-04 收藏状态', favCheck.data?.favorited === true)
  const favPage = await api('GET', '/front/favorites', { token: userToken })
  check('FAV-01 收藏分页', favPage.code === 0 && favPage.data.records[0]?.favoriteId && favPage.data.records[0]?.name)
  const favDel = await api('DELETE', '/front/favorites/2001', { token: userToken })
  check('FAV-03 取消收藏', favDel.code === 0)
  const favCheck2 = await api('GET', '/front/favorites/check?productId=2001', { token: userToken })
  check('FAV-04 取消后状态', favCheck2.data?.favorited === false)

  section('5. FORD 订单（6）')
  const stockBefore = (await api('GET', '/front/products/2001')).data.stock
  const orderCreate = await api('POST', '/front/orders', {
    token: userToken,
    body: {
      fromCart: false,
      items: [{ productId: '2001', quantity: 2, specText: '颜色:云雾白' }],
      receiverName: '小明',
      receiverPhone: '13900000000',
      receiverAddress: '广东财经大学 1 栋 101',
      remark: '冒烟测试'
    }
  })
  check('FORD-01 立即购买下单', orderCreate.code === 0 && orderCreate.data?.orderId && orderCreate.data?.orderNo,
    JSON.stringify(orderCreate.body).slice(0, 160))
  const orderId = orderCreate.data?.orderId
  const orderNo = orderCreate.data?.orderNo
  check('下单返回金额两位小数字符串', /^\d+\.\d{2}$/.test(orderCreate.data?.payAmount || ''), orderCreate.data?.payAmount)

  const stockAfter = (await api('GET', '/front/products/2001')).data.stock
  check('下单即扣库存', stockAfter === stockBefore - 2, `${stockBefore} -> ${stockAfter}`)

  const fromCart = await api('POST', '/front/orders', {
    token: userToken,
    body: {
      fromCart: true,
      cartIds: [cartId],
      receiverName: '小明',
      receiverPhone: '13900000000',
      receiverAddress: '广东财经大学 1 栋 101'
    }
  })
  check('FORD-01 购物车下单', fromCart.code === 0, JSON.stringify(fromCart.body).slice(0, 140))
  const cartAfter = await api('GET', '/front/cart', { token: userToken })
  check('下单后清理购物车行', !cartAfter.data.items.some((i) => i.id === cartId))

  const badCartId = await api('POST', '/front/orders', {
    token: userToken,
    body: {
      fromCart: true,
      cartIds: ['999999999'],
      receiverName: '小明',
      receiverPhone: '13900000000',
      receiverAddress: 'x'
    }
  })
  check('3003 无效购物车行整单拒绝', badCartId.code === 3003, String(badCartId.code))

  const offShelfOrder = await api('POST', '/front/orders', {
    token: userToken,
    body: {
      fromCart: false,
      items: [{ productId: '2012', quantity: 1 }],
      receiverName: '小明',
      receiverPhone: '13900000000',
      receiverAddress: 'x'
    }
  })
  check('3005 下架商品下单', offShelfOrder.code === 3005, String(offShelfOrder.code))

  const noStockOrder = await api('POST', '/front/orders', {
    token: userToken,
    body: {
      fromCart: false,
      items: [{ productId: '2009', quantity: 9999 }],
      receiverName: '小明',
      receiverPhone: '13900000000',
      receiverAddress: 'x'
    }
  })
  check('3001 库存不足', noStockOrder.code === 3001, String(noStockOrder.code))

  const badPhone = await api('POST', '/front/orders', {
    token: userToken,
    body: { fromCart: false, items: [{ productId: '2001', quantity: 1, specText: '颜色:云雾白' }], receiverName: 'a', receiverPhone: '123', receiverAddress: 'x' }
  })
  check('1001 手机号非法 -> HTTP200', badPhone.status === 200 && badPhone.code === 1001, `${badPhone.status}/${badPhone.code}`)

  const orderPage = await api('GET', '/front/orders?page=1&size=10', { token: userToken })
  check('FORD-02 我的订单分页', orderPage.code === 0 && orderPage.data.records.length >= 2)
  const firstOrder = orderPage.data.records.find((o) => o.id === orderId)
  check('FORD-02 statusText/actions 由后端给出',
    firstOrder?.statusText === '待付款' && firstOrder?.actions?.some((a) => a.action === 'pay'),
    JSON.stringify({ s: firstOrder?.statusText, a: firstOrder?.actions }))
  check('FORD-02 订单项含 id 与行小计',
    !!firstOrder?.items?.[0]?.id && /^\d+\.\d{2}$/.test(firstOrder?.items?.[0]?.totalAmount || ''))

  const orderDetail = await api('GET', `/front/orders/${orderId}`, { token: userToken })
  check('FORD-03 订单详情', orderDetail.code === 0 && orderDetail.data?.receiverName === '小明')

  const otherSeeOrder = await api('GET', `/front/orders/${orderId}`, { token: otherToken })
  check('2003 他人订单详情', otherSeeOrder.code === 2003, String(otherSeeOrder.code))

  const pay1 = await api('POST', `/front/orders/${orderId}/pay`, { token: userToken })
  check('FORD-04 支付', pay1.code === 0)
  const pay2 = await api('POST', `/front/orders/${orderId}/pay`, { token: userToken })
  check('重复支付幂等', pay2.code === 0)
  const paidDetail = await api('GET', `/front/orders/${orderId}`, { token: userToken })
  check('支付后状态=1 且 actions 为空',
    paidDetail.data?.status === 1 && paidDetail.data?.actions?.length === 0)
  const salesAfterPay = (await api('GET', '/front/products/2001')).data.sales
  check('支付成功累加销量', typeof salesAfterPay === 'number' && salesAfterPay > 0, String(salesAfterPay))

  const payThenCancel = await api('POST', `/front/orders/${orderId}/cancel`, { token: userToken, body: { reason: '不想买了' } })
  check('3002 已支付订单不可取消', payThenCancel.code === 3002, String(payThenCancel.code))

  // 取消订单 + 重复取消（库存只回补一次）
  const cancelOrder = await api('POST', '/front/orders', {
    token: userToken,
    body: {
      fromCart: false,
      items: [{ productId: '2005', quantity: 3, specText: '颜色:雾霾蓝' }],
      receiverName: '小明', receiverPhone: '13900000000', receiverAddress: 'x'
    }
  })
  const cancelId = cancelOrder.data?.orderId
  const stockB = (await api('GET', '/front/products/2005')).data.stock
  const c1 = await api('POST', `/front/orders/${cancelId}/cancel`, { token: userToken, body: { reason: '冒烟取消' } })
  check('FORD-05 取消订单', c1.code === 0)
  const stockC = (await api('GET', '/front/products/2005')).data.stock
  check('取消回补库存', stockC === stockB + 3, `${stockB} -> ${stockC}`)
  const c2 = await api('POST', `/front/orders/${cancelId}/cancel`, { token: userToken })
  check('重复取消幂等', c2.code === 0)
  const stockD = (await api('GET', '/front/products/2005')).data.stock
  check('重复取消不二次回补', stockD === stockC, `${stockC} -> ${stockD}`)

  section('6. ADM-CAT 后台类型（5）')
  const catOptions = await api('GET', '/admin/categories', { token: adminToken })
  check('CAT-01 类型下拉', catOptions.code === 0 && catOptions.data.length >= 5)
  const catPage = await api('GET', '/admin/categories/page?page=1&size=10', { token: adminToken })
  check('CAT-02 类型分页（含 productCount）', catPage.code === 0 && typeof catPage.data.records[0]?.productCount === 'number')

  const catName = `冒烟类型${suffix}`
  const catCreate = await api('POST', '/admin/categories', { token: adminToken, body: { name: catName, sort: 99 } })
  check('CAT-03 新增类型', catCreate.code === 0, String(catCreate.code))
  const catDup = await api('POST', '/admin/categories', { token: adminToken, body: { name: catName } })
  check('3004 类型重名', catDup.code === 3004, String(catDup.code))

  const catList2 = await api('GET', '/admin/categories', { token: adminToken })
  const newCat = catList2.data.find((c) => c.name === catName)
  check('新增类型出现在下拉中', !!newCat)

  const catUpd = await api('PUT', `/admin/categories/${newCat.id}`, { token: adminToken, body: { name: catName + '改', sort: 98 } })
  check('CAT-04 编辑类型', catUpd.code === 0)

  const catDel = await api('DELETE', `/admin/categories/${newCat.id}`, { token: adminToken })
  check('CAT-05 删除空类型', catDel.code === 0)

  const catDelRef = await api('DELETE', '/admin/categories/1001', { token: adminToken })
  check('3002 删除有商品的类型', catDelRef.code === 3002, String(catDelRef.code))

  section('7. ADM-PRD 后台商品（7）')
  const prdPage = await api('GET', '/admin/products?page=1&size=10', { token: adminToken })
  check('PRD-01 商品分页（含下架）', prdPage.code === 0 && prdPage.data.records.some((p) => p.status === 0))

  const newProduct = await api('POST', '/admin/products', {
    token: adminToken,
    body: {
      categoryId: '1001',
      name: `冒烟商品${suffix}`,
      subtitle: '联调专用',
      mainImage: '/files/p1.svg',
      price: 12.34,
      stock: 5,
      specJson: '[{"name":"颜色","values":["红","蓝"]}]',
      detail: '冒烟测试商品',
      status: 1
    }
  })
  check('PRD-03 新增商品', newProduct.code === 0 && newProduct.data?.id, JSON.stringify(newProduct.body).slice(0, 120))
  const newProductId = newProduct.data?.id

  const badSpecJson = await api('POST', '/admin/products', {
    token: adminToken,
    body: { categoryId: '1001', name: 'x', price: 1, stock: 1, specJson: 'not-json', status: 1 }
  })
  check('1001 规格 JSON 非法', badSpecJson.code === 1001, String(badSpecJson.code))

  const prdDetail = await api('GET', `/admin/products/${newProductId}`, { token: adminToken })
  check('PRD-02 商品详情', prdDetail.code === 0 && prdDetail.data?.name === `冒烟商品${suffix}`)

  const prdUpd = await api('PUT', `/admin/products/${newProductId}`, {
    token: adminToken,
    body: {
      categoryId: '1001', name: `冒烟商品${suffix}改`, subtitle: '改过', mainImage: '/files/p1.svg',
      price: 23.45, stock: 8, specJson: '[{"name":"颜色","values":["红","蓝"]}]', detail: 'd', status: 1
    }
  })
  check('PRD-04 编辑商品', prdUpd.code === 0)

  const prdStatus = await api('PUT', `/admin/products/${newProductId}/status`, { token: adminToken, body: { status: 0 } })
  check('PRD-05 单个下架', prdStatus.code === 0)
  const afterOff = await api('GET', `/admin/products/${newProductId}`, { token: adminToken })
  check('下架后后台仍可查', afterOff.data?.status === 0)

  const prdBatch = await api('PUT', '/admin/products/status', { token: adminToken, body: { ids: [newProductId], status: 1 } })
  check('PRD-06 批量上架', prdBatch.code === 0)

  const prdDel = await api('DELETE', `/admin/products/${newProductId}`, { token: adminToken })
  check('PRD-07 逻辑删除', prdDel.code === 0)
  const afterDel = await api('GET', `/admin/products/${newProductId}`, { token: adminToken })
  check('删除后不可查', afterDel.code === 3003, String(afterDel.code))

  const userToAdmin = await api('GET', '/admin/products', { token: userToken })
  check('2003 普通用户访问后台', userToAdmin.status === 403 && userToAdmin.code === 2003,
    `${userToAdmin.status}/${userToAdmin.code}`)

  section('8. ADM-ORD 后台订单（4）')
  const admOrders = await api('GET', '/admin/orders?page=1&size=10', { token: adminToken })
  check('ORD-01 订单分页', admOrders.code === 0 && admOrders.data.records.length >= 1)
  check('ORD-01 含 userName', !!admOrders.data.records[0]?.userName, String(admOrders.data.records[0]?.userName))

  const byNo = await api('GET', `/admin/orders?orderNo=${orderNo}`, { token: adminToken })
  check('ORD-01 按单号筛选', byNo.code === 0 && byNo.data.records.length === 1)

  const admDetail = await api('GET', `/admin/orders/${orderId}`, { token: adminToken })
  check('ORD-02 订单详情', admDetail.code === 0 && admDetail.data?.statusText === '待发货')

  const ship = await api('PUT', `/admin/orders/${orderId}/ship`, { token: adminToken, body: { trackingNo: 'SF123456789' } })
  check('ORD-03 发货', ship.code === 0)
  const shipAgain = await api('PUT', `/admin/orders/${orderId}/ship`, { token: adminToken, body: { trackingNo: 'SF999' } })
  check('重复发货幂等', shipAgain.code === 0)
  const afterShip = await api('GET', `/admin/orders/${orderId}`, { token: adminToken })
  check('重复发货不覆盖原单号', afterShip.data?.trackingNo === 'SF123456789', String(afterShip.data?.trackingNo))

  const userConfirm = await api('PUT', `/front/orders/${orderId}/confirm`, { token: userToken })
  check('FORD-06 确认收货', userConfirm.code === 0)
  const confirmAgain = await api('PUT', `/front/orders/${orderId}/confirm`, { token: userToken })
  check('重复确认收货幂等', confirmAgain.code === 0)
  const finished = await api('GET', `/front/orders/${orderId}`, { token: userToken })
  check('完成后状态=3', finished.data?.status === 3 && finished.data?.statusText === '已完成')

  // 后台代确认
  const o2 = await api('POST', '/front/orders', {
    token: userToken,
    body: { fromCart: false, items: [{ productId: '2006', quantity: 1 }], receiverName: '小明', receiverPhone: '13900000000', receiverAddress: 'x' }
  })
  const o2id = o2.data?.orderId
  await api('POST', `/front/orders/${o2id}/pay`, { token: userToken })
  await api('PUT', `/admin/orders/${o2id}/ship`, { token: adminToken, body: {} })
  const admConfirm = await api('PUT', `/admin/orders/${o2id}/confirm`, { token: adminToken })
  check('ORD-04 后台代确认收货', admConfirm.code === 0)

  const shipWrongState = await api('PUT', `/admin/orders/${o2id}/ship`, { token: adminToken, body: {} })
  check('3002 已完成订单发货', shipWrongState.code === 3002, String(shipWrongState.code))

  section('9. ADM-STAT 统计（4）')
  const overview = await api('GET', '/admin/stats/overview', { token: adminToken })
  check('STAT-01 概览 5 项', overview.code === 0
    && typeof overview.data?.todayOrderCount === 'number'
    && typeof overview.data?.todaySalesAmount === 'string'
    && typeof overview.data?.pendingShipmentCount === 'number'
    && typeof overview.data?.totalProductCount === 'number'
    && typeof overview.data?.totalUserCount === 'number',
  JSON.stringify(overview.data))

  const trend7 = await api('GET', '/admin/stats/sales?days=7', { token: adminToken })
  check('STAT-02 7 天趋势长度=7',
    trend7.code === 0 && trend7.data.dates.length === 7 && trend7.data.amounts.length === 7)
  const trend30 = await api('GET', '/admin/stats/sales?days=30', { token: adminToken })
  check('STAT-02 30 天趋势长度=30', trend30.data?.dates?.length === 30)
  check('趋势金额两位小数', trend30.data.amounts.every((a) => /^\d+\.\d{2}$/.test(a)))

  const dist = await api('GET', '/admin/stats/order-status', { token: adminToken })
  check('STAT-03 恒 5 项状态', dist.code === 0 && dist.data.length === 5
    && dist.data.every((d) => typeof d.count === 'number' && d.statusText))

  const tops = await api('GET', '/admin/stats/top-products?limit=5', { token: adminToken })
  check('STAT-04 热销 TOP5', tops.code === 0 && tops.data.length <= 5 && tops.data[0]?.productName !== undefined)

  section('10. FILE 上传（1）')
  const form = new FormData()
  form.append('file', tinyPng(), 'smoke.png')
  const upload = await api('POST', '/admin/files', { token: adminToken, form })
  check('FILE-01 上传图片', upload.code === 0 && /^\/files\/\d{4}\/\d{2}\/.+\.png$/.test(upload.data?.url || ''),
    JSON.stringify(upload.body).slice(0, 140))

  const badForm = new FormData()
  badForm.append('file', new Blob(['hello'], { type: 'text/plain' }), 'a.txt')
  const uploadBad = await api('POST', '/admin/files', { token: adminToken, form: badForm })
  check('1001 非图片类型拒绝', uploadBad.code === 1001, String(uploadBad.code))

  if (upload.data?.url) {
    const fileRes = await fetch(`http://localhost:8080/api${upload.data.url}`)
    check('上传文件可访问', fileRes.status === 200, String(fileRes.status))
  }
  const seedImg = await fetch('http://localhost:8080/api/files/p1.svg')
  check('seed 图片可访问（/files 代理目标）', seedImg.status === 200, String(seedImg.status))

  section('11. 并发超卖保护（3 并发）')
  const conc = await api('POST', '/admin/products', {
    token: adminToken,
    body: { categoryId: '1001', name: `并发商品${suffix}`, price: 1, stock: 1, detail: 'x', status: 1 }
  })
  const concId = conc.data?.id
  const results = await Promise.all([1, 2, 3].map(() => api('POST', '/front/orders', {
    token: userToken,
    body: { fromCart: false, items: [{ productId: concId, quantity: 1 }], receiverName: 'a', receiverPhone: '13900000000', receiverAddress: 'x' }
  })))
  const success = results.filter((r) => r.code === 0).length
  check('并发下单仅 1 单成功', success === 1, `success=${success}, codes=${results.map((r) => r.code)}`)
  check('并发后库存不为负', (await api('GET', `/admin/products/${concId}`, { token: adminToken })).data.stock === 0)
  await api('DELETE', `/admin/products/${concId}`, { token: adminToken })

  console.log(`\n===== 结果：通过 ${pass} 项，失败 ${failures.length} 项 =====`)
  if (failures.length) {
    console.log('失败清单：')
    failures.forEach((f) => console.log(' - ' + f))
    process.exit(1)
  }
}

run().catch((e) => {
  console.error('脚本异常：', e)
  process.exit(2)
})
