import request from '@/utils/request'

/** ORD-01 订单分页（orderNo / status / page / size） */
export function fetchOrderPage(params) {
  return request.get('/admin/orders', { params })
}

/** ORD-02 订单详情 */
export function fetchOrderById(id) {
  return request.get(`/admin/orders/${id}`)
}

/** ORD-03 发货（仅 status=1） */
export function shipOrder(id, trackingNo) {
  return request.put(`/admin/orders/${id}/ship`, { trackingNo })
}

/** ORD-04 确认收货（仅 status=2） */
export function confirmOrder(id) {
  return request.put(`/admin/orders/${id}/confirm`)
}
