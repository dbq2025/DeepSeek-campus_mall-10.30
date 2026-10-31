/**
 * Mock 全链路冒烟脚本（开发期工具，不参与生产构建）。
 * 用法：先 npm run dev，再 node scripts/smoke.mjs
 */
const BASE = 'http://localhost:5173/api'

let pass = 0
let fail = 0

function check(name, cond, extra = '') {
  if (cond) {
    pass += 1
    console.log(`  OK   ${name}`)
  } else {
    fail += 1
    console.log(`  FAIL ${name} ${extra}`)
  }
}

async function call(method, path, { token, body } = {}) {
  const headers = {}
  if (body) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`
  const res = await fetch(BASE + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })
  let json = null
  try {
    json = await res.json()
  } catch (e) {
    json = null
  }
  return { status: res.status, ...(json || {}) }
}

async function main() {
  console.log('\n[0] 复位')
  const reset = await call('POST', '/__mock/reset')
  check('reset', reset.code === 0)

  console.log('\n[1] 未登录访问受保护接口')
  const unauth = await call('GET', '/front/cart')
  check('未登录返回 401', unauth.status === 401 && unauth.code === 2001, JSON.stringify(unauth))

  console.log('\n[2] 商品与详情')
  const list = await call('GET', '/front/products?page=1&size=4&categoryId=201')
  check('分页返回 4 条', list.data?.records?.length === 4, JSON.stringify(list.data?.total))
  const detail = await call('GET', '/front/products/10001')
  check('详情含 specJson', Boolean(detail.data?.specJson), JSON.stringify(detail))
  const offShelf = await call('GET', '/front/products/10014')
  check('下架商品返回 3005', offShelf.code === 3005, JSON.stringify(offShelf))

  console.log('\n[3] 登录与购物车')
  const login = await call('POST', '/auth/login', { body: { username: 'student', password: '123456' } })
  check('登录成功', login.code === 0 && Boolean(login.data?.token))
  const token = login.data.token

  const badLogin = await call('POST', '/auth/login', { body: { username: 'student', password: 'x' } })
  check('错误密码返回 2004', badLogin.code === 2004, JSON.stringify(badLogin))

  const cart1 = await call('GET', '/front/cart', { token })
  check('购物车 2 行', cart1.data?.items?.length === 2, JSON.stringify(cart1.data?.totalQuantity))

  const specMissing = await call('POST', '/front/cart', {
    token,
    body: { productId: '10001', quantity: 1 }
  })
  check('有规格商品缺规格被拒 1001', specMissing.code === 1001, JSON.stringify(specMissing))

  const added = await call('POST', '/front/cart', {
    token,
    body: { productId: '10001', quantity: 2, specText: '颜色:月光白' }
  })
  check('加购成功（新规格行）', added.code === 0 && added.data?.quantity === 2, JSON.stringify(added))

  const cart2 = await call('GET', '/front/cart', { token })
  check('购物车变为 3 行', cart2.data?.items?.length === 3, JSON.stringify(cart2.data?.items?.length))
  const stockBad = await call('PUT', `/front/cart/${cart2.data.items[0].id}`, {
    token,
    body: { quantity: 9999 }
  })
  check('改量超库存返回 3001', stockBad.code === 3001, JSON.stringify(stockBad))

  console.log('\n[4] 下单（购物车）')
  const ids = cart2.data.items.map((item) => item.id)
  const created = await call('POST', '/front/orders', {
    token,
    body: {
      fromCart: true,
      cartIds: ids,
      receiverName: '同学甲',
      receiverPhone: '13800001111',
      receiverAddress: '广东省广州市海珠区测试路 1 号'
    }
  })
  check('创建订单成功', created.code === 0 && Boolean(created.data?.orderId), JSON.stringify(created))
  const orderId = created.data.orderId

  const cartAfter = await call('GET', '/front/cart', { token })
  check('下单后购物车已清空', cartAfter.data?.items?.length === 0)

  const stockAfter = await call('GET', '/front/products/10001')
  check('库存已扣减 (50-1-2=47)', stockAfter.data?.stock === 47, String(stockAfter.data?.stock))

  console.log('\n[5] 订单状态机')
  const detail1 = await call('GET', `/front/orders/${orderId}`, { token })
  check('新订单为待付款 + 两个动作', detail1.data?.status === 0 && detail1.data?.actions?.length === 2)

  const paid = await call('POST', `/front/orders/${orderId}/pay`, { token })
  check('支付成功', paid.code === 0)
  const paidAgain = await call('POST', `/front/orders/${orderId}/pay`, { token })
  check('重复支付幂等', paidAgain.code === 0)
  const cancelPaid = await call('POST', `/front/orders/${orderId}/cancel`, { token, body: {} })
  check('已支付不可取消 3002', cancelPaid.code === 3002, JSON.stringify(cancelPaid))

  console.log('\n[6] 后台发货与用户确认')
  const adminLogin = await call('POST', '/auth/admin/login', {
    body: { username: 'admin', password: '123456' }
  })
  check('管理员登录', adminLogin.code === 0)
  const adminToken = adminLogin.data.token

  const userToAdmin = await call('POST', '/auth/admin/login', {
    body: { username: 'student', password: '123456' }
  })
  check('普通用户登录后台被拒 2003', userToAdmin.code === 2003, JSON.stringify(userToAdmin))

  const shipped = await call('PUT', `/admin/orders/${orderId}/ship`, {
    token: adminToken,
    body: { trackingNo: 'SF0001' }
  })
  check('发货成功', shipped.code === 0, JSON.stringify(shipped))

  const confirmed = await call('PUT', `/front/orders/${orderId}/confirm`, { token })
  check('用户确认收货', confirmed.code === 0, JSON.stringify(confirmed))

  const finalDetail = await call('GET', `/front/orders/${orderId}`, { token })
  check('订单已完成且无动作', finalDetail.data?.status === 3 && finalDetail.data?.actions?.length === 0)

  console.log('\n[7] 取消回补库存')
  const created2 = await call('POST', '/front/orders', {
    token,
    body: {
      fromCart: false,
      items: [{ productId: '10012', quantity: 2, specText: '' }],
      receiverName: '同学甲',
      receiverPhone: '13800001111',
      receiverAddress: '测试地址'
    }
  })
  check('立即购买下单成功', created2.code === 0)
  const stockMid = await call('GET', '/front/products/10012')
  check('下单后库存 3-2=1', stockMid.data?.stock === 1, String(stockMid.data?.stock))
  const overStock = await call('POST', '/front/orders', {
    token,
    body: {
      fromCart: false,
      items: [{ productId: '10012', quantity: 5, specText: '' }],
      receiverName: '同学甲',
      receiverPhone: '13800001111',
      receiverAddress: '测试地址'
    }
  })
  check('超库存下单返回 3001', overStock.code === 3001, JSON.stringify(overStock))
  await call('POST', `/front/orders/${created2.data.orderId}/cancel`, { token, body: {} })
  const stockRestored = await call('GET', '/front/products/10012')
  check('取消后库存回补为 3', stockRestored.data?.stock === 3, String(stockRestored.data?.stock))

  console.log('\n[8] 收藏 / 后台 CRUD / 统计')
  const favAdd = await call('POST', '/front/favorites', { token, body: { productId: '10001' } })
  check('收藏成功', favAdd.code === 0)
  const favDup = await call('POST', '/front/favorites', { token, body: { productId: '10001' } })
  check('重复收藏 3004', favDup.code === 3004)
  const favPage = await call('GET', '/front/favorites?page=1&size=10', { token })
  check('收藏列表 3 条', favPage.data?.records?.length === 3, String(favPage.data?.total))

  const catCreate = await call('POST', '/admin/categories', { token: adminToken, body: { name: '测试分类', sort: 9 } })
  check('新增类型', catCreate.code === 0, JSON.stringify(catCreate))
  const catPage = await call('GET', '/admin/categories/page?page=1&size=20', { token: adminToken })
  const testCat = catPage.data?.records?.find((c) => c.name === '测试分类')
  const catDelete = await call('DELETE', `/admin/categories/${testCat?.id}`, { token: adminToken })
  check('删除空类型', catDelete.code === 0)

  const catUsed = await call('DELETE', '/admin/categories/201', { token: adminToken })
  check('删除有商品的类型被拒 3002', catUsed.code === 3002, JSON.stringify(catUsed))

  const prodCreate = await call('POST', '/admin/products', {
    token: adminToken,
    body: {
      categoryId: '201',
      name: '冒烟测试商品',
      subtitle: '自动化脚本创建',
      mainImage: '/files/p1.svg',
      price: '9.90',
      stock: 10,
      specJson: '',
      detail: '测试',
      status: 1
    }
  })
  check('新增商品', prodCreate.code === 0, JSON.stringify(prodCreate))
  const newId = prodCreate.data?.id
  const prodUpdate = await call('PUT', `/admin/products/${newId}`, {
    token: adminToken,
    body: {
      categoryId: '202',
      name: '冒烟测试商品-改',
      subtitle: '已修改',
      mainImage: '/files/p2.svg',
      price: '19.90',
      stock: 5,
      specJson: '[{"name":"颜色","values":["红","蓝"]}]',
      detail: '测试',
      status: 0
    }
  })
  check('编辑商品', prodUpdate.code === 0, JSON.stringify(prodUpdate))
  const prodDelete = await call('DELETE', `/admin/products/${newId}`, { token: adminToken })
  check('删除商品', prodDelete.code === 0)

  const overview = await call('GET', '/admin/stats/overview', { token: adminToken })
  check('概览返回 5 个指标', Object.keys(overview.data || {}).length === 5, JSON.stringify(overview.data))
  const trend = await call('GET', '/admin/stats/sales?days=7', { token: adminToken })
  check('趋势返回 7 天', trend.data?.dates?.length === 7)
  const statsForbidden = await call('GET', '/admin/stats/overview', { token })
  check('普通用户访问后台 403', statsForbidden.status === 403 && statsForbidden.code === 2003)

  console.log(`\n结果：通过 ${pass}，失败 ${fail}`)
  process.exit(fail === 0 ? 0 : 1)
}

main().catch((e) => {
  console.error('脚本异常：', e)
  process.exit(1)
})
