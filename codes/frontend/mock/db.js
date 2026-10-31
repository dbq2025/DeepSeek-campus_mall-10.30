/**
 * Mock 内存数据层。
 * 约定：字段名严格按 design/30-接口契约.md；ID 与金额一律字符串。
 * 数据是"有状态"的：下单会真扣库存、取消会真回补、状态会真流转，保证整条闭环可演示。
 */

/* ---------------- 工具 ---------------- */

let idSeed = 90000

export function nextId() {
  idSeed += 1
  return String(idSeed)
}

function pad(n) {
  return String(n).padStart(2, '0')
}

export function now() {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(
    d.getMinutes()
  )}:${pad(d.getSeconds())}`
}

export function today() {
  return now().slice(0, 10)
}

export function daysAgo(n) {
  const d = new Date(Date.now() - n * 86400000)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function makeOrderNo() {
  const d = new Date()
  const stamp = `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}${pad(d.getHours())}${pad(
    d.getMinutes()
  )}${pad(d.getSeconds())}`
  return `${stamp}${String(Math.floor(Math.random() * 1000000)).padStart(6, '0')}`
}

/* ---------------- 种子数据 ---------------- */

export const USERS = [
  {
    id: '1',
    username: 'admin',
    password: '123456',
    nickname: '平台管理员',
    email: 'admin@campus.edu',
    phone: '13800000000',
    avatar: '',
    role: 'ADMIN',
    status: 1
  },
  {
    id: '2',
    username: 'student',
    password: '123456',
    nickname: '同学甲',
    email: 'student@campus.edu',
    phone: '13800001111',
    avatar: '',
    role: 'USER',
    status: 1
  },
  {
    id: '3',
    username: 'test',
    password: '123456',
    nickname: '测试账号',
    email: 'test@campus.edu',
    phone: '13800002222',
    avatar: '',
    role: 'USER',
    status: 1
  }
]

export const CATEGORIES = [
  { id: '201', name: '数码电子', sort: 1, createTime: '2026-09-01 09:00:00' },
  { id: '202', name: '文具办公', sort: 2, createTime: '2026-09-01 09:00:00' },
  { id: '203', name: '生活日用', sort: 3, createTime: '2026-09-01 09:00:00' },
  { id: '204', name: '运动户外', sort: 4, createTime: '2026-09-01 09:00:00' }
]

const DETAIL = (name) =>
  `【${name}】校园精选正品，支持 7 天无理由退换。\n下单后由校内仓储统一发货，工作日 16:00 前付款当天出库。\n如遇质量问题请在订单详情页发起售后咨询。`

export const PRODUCTS = [
  {
    id: '10001',
    categoryId: '201',
    name: '无线蓝牙耳机 Pro',
    subtitle: '主动降噪 · 单次续航 8 小时',
    mainImage: '/files/p1.svg',
    price: '299.00',
    stock: 50,
    specJson: '[{"name":"颜色","values":["曜石黑","月光白"]}]',
    detail: DETAIL('无线蓝牙耳机 Pro'),
    status: 1,
    sales: 128,
    deleted: 0,
    createTime: '2026-09-05 10:12:00'
  },
  {
    id: '10002',
    categoryId: '201',
    name: '机械键盘 87 键',
    subtitle: '热插拔轴座 · 双色注塑键帽',
    mainImage: '/files/p2.svg',
    price: '259.00',
    stock: 30,
    specJson: '[{"name":"轴体","values":["青轴","红轴","茶轴"]}]',
    detail: DETAIL('机械键盘 87 键'),
    status: 1,
    sales: 86,
    deleted: 0,
    createTime: '2026-09-06 14:30:00'
  },
  {
    id: '10003',
    categoryId: '201',
    name: '便携充电宝 10000mAh',
    subtitle: '双向快充 · 可上飞机',
    mainImage: '/files/p3.svg',
    price: '129.00',
    stock: 60,
    specJson: null,
    detail: DETAIL('便携充电宝 10000mAh'),
    status: 1,
    sales: 210,
    deleted: 0,
    createTime: '2026-09-07 09:40:00'
  },
  {
    id: '10004',
    categoryId: '201',
    name: '蓝牙音箱 mini',
    subtitle: '360° 环绕声 · IPX5 防水',
    mainImage: '/files/p4.svg',
    price: '159.00',
    stock: 4,
    specJson: null,
    detail: DETAIL('蓝牙音箱 mini'),
    status: 1,
    sales: 45,
    deleted: 0,
    createTime: '2026-09-08 16:05:00'
  },
  {
    id: '10005',
    categoryId: '203',
    name: '大学生双肩包',
    subtitle: '15.6 寸电脑仓 · 防泼水面料',
    mainImage: '/files/p5.svg',
    price: '189.00',
    stock: 25,
    specJson: '[{"name":"颜色","values":["藏青","军绿","浅灰"]}]',
    detail: DETAIL('大学生双肩包'),
    status: 1,
    sales: 320,
    deleted: 0,
    createTime: '2026-09-09 11:20:00'
  },
  {
    id: '10006',
    categoryId: '203',
    name: '保温杯 500ml',
    subtitle: '316 不锈钢 · 12 小时保温',
    mainImage: '/files/p6.svg',
    price: '79.00',
    stock: 100,
    specJson: null,
    detail: DETAIL('保温杯 500ml'),
    status: 1,
    sales: 156,
    deleted: 0,
    createTime: '2026-09-10 08:15:00'
  },
  {
    id: '10007',
    categoryId: '203',
    name: '护眼台灯',
    subtitle: '无频闪 · 三档色温',
    mainImage: '/files/p7.svg',
    price: '139.00',
    stock: 18,
    specJson: null,
    detail: DETAIL('护眼台灯'),
    status: 1,
    sales: 64,
    deleted: 0,
    createTime: '2026-09-11 19:00:00'
  },
  {
    id: '10008',
    categoryId: '202',
    name: '中性笔 0.5mm 10 支装',
    subtitle: '速干墨水 · 顺滑不断墨',
    mainImage: '/files/p8.svg',
    price: '12.90',
    stock: 500,
    specJson: null,
    detail: DETAIL('中性笔 0.5mm 10 支装'),
    status: 1,
    sales: 890,
    deleted: 0,
    createTime: '2026-09-12 10:00:00'
  },
  {
    id: '10009',
    categoryId: '202',
    name: 'A5 硬壳笔记本',
    subtitle: '100g 道林纸 · 180° 平摊',
    mainImage: '/files/p1.svg',
    price: '19.90',
    stock: 200,
    specJson: '[{"name":"颜色","values":["米白","墨绿"]}]',
    detail: DETAIL('A5 硬壳笔记本'),
    status: 1,
    sales: 430,
    deleted: 0,
    createTime: '2026-09-13 15:45:00'
  },
  {
    id: '10010',
    categoryId: '202',
    name: '制图尺套装',
    subtitle: '四件套 · 刻度清晰',
    mainImage: '/files/p2.svg',
    price: '25.00',
    stock: 80,
    specJson: null,
    detail: DETAIL('制图尺套装'),
    status: 1,
    sales: 40,
    deleted: 0,
    createTime: '2026-09-14 09:30:00'
  },
  {
    id: '10011',
    categoryId: '204',
    name: '瑜伽垫 6mm',
    subtitle: 'TPE 材质 · 防滑纹理',
    mainImage: '/files/p3.svg',
    price: '89.00',
    stock: 40,
    specJson: '[{"name":"颜色","values":["紫罗兰","雾霾蓝"]}]',
    detail: DETAIL('瑜伽垫 6mm'),
    status: 1,
    sales: 75,
    deleted: 0,
    createTime: '2026-09-15 13:10:00'
  },
  {
    id: '10012',
    categoryId: '204',
    name: '竞速跳绳',
    subtitle: '轴承钢丝绳 · 可调长度',
    mainImage: '/files/p4.svg',
    price: '29.90',
    stock: 3,
    specJson: null,
    detail: DETAIL('竞速跳绳'),
    status: 1,
    sales: 120,
    deleted: 0,
    createTime: '2026-09-16 18:20:00'
  },
  {
    id: '10013',
    categoryId: '204',
    name: '运动水壶 750ml',
    subtitle: '弹盖设计 · 单手开合',
    mainImage: '/files/p5.svg',
    price: '49.00',
    stock: 0,
    specJson: null,
    detail: DETAIL('运动水壶 750ml'),
    status: 1,
    sales: 33,
    deleted: 0,
    createTime: '2026-09-17 07:50:00'
  },
  {
    id: '10014',
    categoryId: '203',
    name: '折叠雨伞',
    subtitle: '黑胶防晒 · 一键开收',
    mainImage: '/files/p6.svg',
    price: '59.00',
    stock: 0,
    specJson: null,
    detail: DETAIL('折叠雨伞'),
    status: 0,
    sales: 12,
    deleted: 0,
    createTime: '2026-09-18 12:00:00'
  }
]

/** 购物车：按 userId 分组 */
export const CARTS = {
  // 同学甲的购物车：含 1 行带规格、1 行无规格，方便演示
  2: [
    { id: '301', productId: '10001', quantity: 1, specText: '颜色:曜石黑', createTime: '2026-10-30 10:00:00' },
    { id: '302', productId: '10006', quantity: 2, specText: '', createTime: '2026-10-30 10:05:00' }
  ]
}

function item(productId, quantity, specText, price, name, image) {
  const priceNum = Number(price)
  const total = (priceNum * quantity).toFixed(2)
  return {
    id: nextId(),
    productId,
    productName: name,
    productImage: image,
    price,
    quantity,
    specText,
    totalAmount: total
  }
}

/** 订单：覆盖 4 种状态，便于演示筛选与动作按钮 */
export const ORDERS = [
  {
    id: '401',
    orderNo: '20261028101201000001',
    userId: '2',
    totalAmount: '299.00',
    payAmount: '299.00',
    status: 0,
    receiverName: '同学甲',
    receiverPhone: '13800001111',
    receiverAddress: '广东省广州市海珠区某某路 1 号 3 栋 402',
    remark: '请放在宿舍门口',
    trackingNo: null,
    payTime: null,
    shipTime: null,
    finishTime: null,
    cancelTime: null,
    cancelReason: null,
    createTime: '2026-10-28 10:12:01',
    items: [item('10001', 1, '颜色:曜石黑', '299.00', '无线蓝牙耳机 Pro', '/files/p1.svg')]
  },
  {
    id: '402',
    orderNo: '20261026153012000002',
    userId: '2',
    totalAmount: '291.90',
    payAmount: '291.90',
    status: 1,
    receiverName: '同学甲',
    receiverPhone: '13800001111',
    receiverAddress: '广东省广州市海珠区某某路 1 号 3 栋 402',
    remark: '',
    trackingNo: null,
    payTime: '2026-10-26 15:32:00',
    shipTime: null,
    finishTime: null,
    cancelTime: null,
    cancelReason: null,
    createTime: '2026-10-26 15:30:12',
    items: [
      item('10005', 1, '颜色:藏青', '189.00', '大学生双肩包', '/files/p5.svg'),
      item('10008', 2, '', '12.90', '中性笔 0.5mm 10 支装', '/files/p8.svg')
    ]
  },
  {
    id: '403',
    orderNo: '20261020110103000003',
    userId: '2',
    totalAmount: '139.00',
    payAmount: '139.00',
    status: 2,
    receiverName: '同学甲',
    receiverPhone: '13800001111',
    receiverAddress: '广东省广州市海珠区某某路 1 号 3 栋 402',
    remark: '',
    trackingNo: 'SF1234567890',
    payTime: '2026-10-20 11:02:00',
    shipTime: '2026-10-21 09:20:00',
    finishTime: null,
    cancelTime: null,
    cancelReason: null,
    createTime: '2026-10-20 11:01:03',
    items: [item('10007', 1, '', '139.00', '护眼台灯', '/files/p7.svg')]
  },
  {
    id: '404',
    orderNo: '20261018094504000004',
    userId: '2',
    totalAmount: '86.90',
    payAmount: '86.90',
    status: 3,
    receiverName: '同学甲',
    receiverPhone: '13800001111',
    receiverAddress: '广东省广州市海珠区某某路 1 号 3 栋 402',
    remark: '',
    trackingNo: 'YT9988776655',
    payTime: '2026-10-18 09:46:00',
    shipTime: '2026-10-18 15:00:00',
    finishTime: '2026-10-19 12:30:00',
    cancelTime: null,
    cancelReason: null,
    createTime: '2026-10-18 09:45:04',
    items: [
      item('10006', 1, '', '79.00', '保温杯 500ml', '/files/p6.svg'),
      item('10008', 1, '', '12.90', '中性笔 0.5mm 10 支装', '/files/p8.svg')
    ]
  },
  {
    id: '405',
    orderNo: '20261015083005000005',
    userId: '3',
    totalAmount: '159.00',
    payAmount: '159.00',
    status: 4,
    receiverName: '测试账号',
    receiverPhone: '13800002222',
    receiverAddress: '广东省广州市番禺区某某大道 2 号',
    remark: '',
    trackingNo: null,
    payTime: null,
    shipTime: null,
    finishTime: null,
    cancelTime: '2026-10-15 09:10:00',
    cancelReason: '用户取消',
    createTime: '2026-10-15 08:30:05',
    items: [item('10004', 1, '', '159.00', '蓝牙音箱 mini', '/files/p4.svg')]
  }
]

export const FAVORITES = [
  { id: '501', userId: '2', productId: '10005', createTime: '2026-10-25 20:00:00' },
  { id: '502', userId: '2', productId: '10009', createTime: '2026-10-27 21:30:00' }
]

/* ---------------- 会话 ---------------- */

export const TOKENS = {}

export function issueToken(userId) {
  const token = `mock-${userId}-${Math.random().toString(36).slice(2, 10)}`
  TOKENS[token] = userId
  return token
}

export function userByToken(token) {
  if (!token) return null
  const raw = String(token).replace(/^Bearer\s+/i, '')
  const userId = TOKENS[raw]
  if (!userId) return null
  return USERS.find((u) => u.id === userId) || null
}

export function publicUser(user) {
  return {
    id: user.id,
    username: user.username,
    nickname: user.nickname,
    email: user.email,
    phone: user.phone,
    avatar: user.avatar,
    role: user.role
  }
}

/* ---------------- 复位 ---------------- */

const SNAPSHOT = JSON.parse(
  JSON.stringify({ products: PRODUCTS, carts: CARTS, orders: ORDERS, favorites: FAVORITES, categories: CATEGORIES })
)

/** 把有状态数据恢复到种子值（订单/库存/购物车/收藏） */
export function resetState() {
  const snapshot = JSON.parse(JSON.stringify(SNAPSHOT))
  PRODUCTS.splice(0, PRODUCTS.length, ...snapshot.products)
  Object.keys(CARTS).forEach((key) => delete CARTS[key])
  Object.assign(CARTS, snapshot.carts)
  ORDERS.splice(0, ORDERS.length, ...snapshot.orders)
  FAVORITES.splice(0, FAVORITES.length, ...snapshot.favorites)
  CATEGORIES.splice(0, CATEGORIES.length, ...snapshot.categories)
  Object.keys(TOKENS).forEach((key) => delete TOKENS[key])
}
