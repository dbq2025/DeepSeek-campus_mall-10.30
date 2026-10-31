import request from '@/utils/request'

/** STAT-01 概览指标 */
export function fetchOverview() {
  return request.get('/admin/stats/overview')
}

/** STAT-02 销售趋势（days=7|30） */
export function fetchSalesTrend(days = 7) {
  return request.get('/admin/stats/sales', { params: { days } })
}

/** STAT-03 订单状态分布 */
export function fetchOrderStatusStat() {
  return request.get('/admin/stats/order-status')
}

/** STAT-04 热销排行 */
export function fetchTopProducts(limit = 5) {
  return request.get('/admin/stats/top-products', { params: { limit } })
}
