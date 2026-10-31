import request from '@/utils/request'

/** FPRD-01 商品分页（keyword / categoryId / sort / page / size） */
export function fetchProducts(params) {
  return request.get('/front/products', { params })
}

/** FPRD-02 商品详情 */
export function fetchProductDetail(id) {
  return request.get(`/front/products/${id}`)
}

/** FPRD-03 类型列表 */
export function fetchCategories() {
  return request.get('/front/categories')
}
