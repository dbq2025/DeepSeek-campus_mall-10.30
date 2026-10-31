import request from '@/utils/request'

/** PRD-01 商品分页（含下架） */
export function fetchProductPage(params) {
  return request.get('/admin/products', { params })
}

/** PRD-02 商品详情（含下架，编辑回填用） */
export function fetchProductById(id) {
  return request.get(`/admin/products/${id}`)
}

/** PRD-03 新增商品 */
export function createProduct(data) {
  return request.post('/admin/products', data)
}

/** PRD-04 编辑商品 */
export function updateProduct(id, data) {
  return request.put(`/admin/products/${id}`, data)
}

/** PRD-05 单个上下架 */
export function updateProductStatus(id, status) {
  return request.put(`/admin/products/${id}/status`, { status })
}

/** PRD-06 批量上下架 */
export function batchUpdateProductStatus(ids, status) {
  return request.put('/admin/products/status', { ids, status })
}

/** PRD-07 删除（逻辑删除） */
export function removeProduct(id) {
  return request.delete(`/admin/products/${id}`)
}
