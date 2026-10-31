import request from '@/utils/request'

/** CAT-01 类型下拉（无分页） */
export function fetchCategoryOptions() {
  return request.get('/admin/categories')
}

/** CAT-02 类型分页 */
export function fetchCategoryPage(params) {
  return request.get('/admin/categories/page', { params })
}

/** CAT-03 新增类型 */
export function createCategory(data) {
  return request.post('/admin/categories', data)
}

/** CAT-04 编辑类型 */
export function updateCategory(id, data) {
  return request.put(`/admin/categories/${id}`, data)
}

/** CAT-05 删除类型 */
export function removeCategory(id) {
  return request.delete(`/admin/categories/${id}`)
}
