import request from '@/utils/request'

/** AUTH-01 用户登录 */
export function login(data) {
  return request.post('/auth/login', data)
}

/** AUTH-02 用户注册（注册即登录） */
export function register(data) {
  return request.post('/auth/register', data)
}

/** AUTH-03 管理员登录 */
export function adminLogin(data) {
  return request.post('/auth/admin/login', data)
}

/** AUTH-04 当前登录用户（刷新页面后校验 token） */
export function fetchMe() {
  return request.get('/auth/me')
}

/** AUTH-05 退出登录 */
export function logout() {
  return request.post('/auth/logout')
}
