import request from '@/utils/request'

/** FAV-01 收藏分页 */
export function fetchFavorites(params) {
  return request.get('/front/favorites', { params })
}

/** FAV-02 添加收藏 */
export function addFavorite(productId) {
  return request.post('/front/favorites', { productId })
}

/** FAV-03 取消收藏 */
export function removeFavorite(productId) {
  return request.delete(`/front/favorites/${productId}`)
}

/** FAV-04 收藏状态 */
export function checkFavorite(productId) {
  return request.get('/front/favorites/check', { params: { productId } })
}
