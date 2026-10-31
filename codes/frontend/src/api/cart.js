import request from '@/utils/request'

/** CART-01 购物车列表 */
export function fetchCart() {
  return request.get('/front/cart')
}

/** CART-02 加入购物车 */
export function addToCart(data) {
  return request.post('/front/cart', data)
}

/** CART-03 修改数量 */
export function updateCartItem(id, quantity) {
  return request.put(`/front/cart/${id}`, { quantity })
}

/** CART-04 删除一行 */
export function removeCartItem(id) {
  return request.delete(`/front/cart/${id}`)
}

/** CART-05 清空购物车 */
export function clearCart() {
  return request.delete('/front/cart')
}
