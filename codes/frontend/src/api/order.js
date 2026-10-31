import request from '@/utils/request'

/** FORD-01 创建订单（fromCart=true 用 cartIds；false 用 items） */
export function createOrder(data) {
  return request.post('/front/orders', data)
}

/** FORD-02 我的订单分页 */
export function fetchOrders(params) {
  return request.get('/front/orders', { params })
}

/** FORD-03 订单详情 */
export function fetchOrderDetail(id) {
  return request.get(`/front/orders/${id}`)
}

/** FORD-04 支付（模拟） */
export function payOrder(id) {
  return request.post(`/front/orders/${id}/pay`)
}

/** FORD-05 取消订单 */
export function cancelOrder(id, reason) {
  return request.post(`/front/orders/${id}/cancel`, { reason })
}

/** FORD-06 确认收货 */
export function confirmOrder(id) {
  return request.put(`/front/orders/${id}/confirm`)
}
