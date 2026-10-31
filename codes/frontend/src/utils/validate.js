/**
 * 表单校验：Element Plus 校验器的可复用实现 + 规格 JSON 解析。
 * 后端仍会二次校验，这里只做交互层的即时反馈。
 */

/** 用户名：4–20 位字母数字下划线 */
export function checkUsername(rule, value, callback) {
  if (!value) return callback(new Error('请输入用户名'))
  if (!/^\w{4,20}$/.test(value)) return callback(new Error('用户名为 4–20 位字母、数字或下划线'))
  callback()
}

/** 密码：6–20 位，不含空格 */
export function checkPassword(rule, value, callback) {
  if (!value) return callback(new Error('请输入密码'))
  if (!/^\S{6,20}$/.test(value)) return callback(new Error('密码为 6–20 位且不含空格'))
  callback()
}

/** 昵称：1–20 字符 */
export function checkNickname(rule, value, callback) {
  if (!value) return callback(new Error('请输入昵称'))
  if (String(value).length > 20) return callback(new Error('昵称不超过 20 个字符'))
  callback()
}

/** 手机号：11 位大陆手机号 */
export function checkPhone(rule, value, callback) {
  if (!value) return callback(new Error('请输入手机号'))
  if (!/^1[3-9]\d{9}$/.test(value)) return callback(new Error('手机号格式不正确'))
  callback()
}

/** 邮箱：允许留空 */
export function checkOptionalEmail(rule, value, callback) {
  if (!value) return callback()
  if (!/^[\w.-]+@[\w-]+(\.[\w-]+)+$/.test(value)) return callback(new Error('邮箱格式不正确'))
  callback()
}

/** 密码确认 */
export function makeConfirmPassword(getPassword) {
  return (rule, value, callback) => {
    if (!value) return callback(new Error('请再次输入密码'))
    if (value !== getPassword()) return callback(new Error('两次输入的密码不一致'))
    callback()
  }
}

/** 金额字符串：非负、最多两位小数 */
export function checkPrice(rule, value, callback) {
  if (value === '' || value === null || value === undefined) return callback(new Error('请输入价格'))
  if (!/^\d+(\.\d{1,2})?$/.test(String(value))) return callback(new Error('价格为非负数，最多两位小数'))
  callback()
}

/** 库存：非负整数 */
export function checkStock(rule, value, callback) {
  if (value === '' || value === null || value === undefined) return callback(new Error('请输入库存'))
  if (!/^\d+$/.test(String(value))) return callback(new Error('库存为非负整数'))
  callback()
}

/**
 * 解析 spec_json。返回 { ok, list, error }。
 * 合法形态：[{ "name": "颜色", "values": ["黑", "白"] }]
 */
export function parseSpecJson(raw) {
  if (raw === null || raw === undefined || String(raw).trim() === '') {
    return { ok: true, list: [], error: '' }
  }
  let parsed
  try {
    parsed = JSON.parse(raw)
  } catch (e) {
    return { ok: false, list: [], error: '不是合法的 JSON' }
  }
  if (!Array.isArray(parsed)) return { ok: false, list: [], error: '规格必须是数组' }
  if (parsed.length === 0) return { ok: true, list: [], error: '' }

  const names = new Set()
  for (const item of parsed) {
    if (!item || typeof item !== 'object') return { ok: false, list: [], error: '每一项必须是对象' }
    if (typeof item.name !== 'string' || !item.name.trim()) {
      return { ok: false, list: [], error: '规格名称不能为空' }
    }
    if (names.has(item.name.trim())) return { ok: false, list: [], error: `规格名称「${item.name}」重复` }
    names.add(item.name.trim())
    if (!Array.isArray(item.values) || item.values.length === 0) {
      return { ok: false, list: [], error: `规格「${item.name}」至少需要一个值` }
    }
    const values = new Set()
    for (const v of item.values) {
      if (typeof v !== 'string' || !v.trim()) {
        return { ok: false, list: [], error: `规格「${item.name}」存在空值` }
      }
      if (values.has(v.trim())) return { ok: false, list: [], error: `规格「${item.name}」的值「${v}」重复` }
      values.add(v.trim())
    }
  }
  return { ok: true, list: parsed, error: '' }
}

/** Element Plus 表单校验器包装：spec_json 合法性 */
export function checkSpecJson(rule, value, callback) {
  const { ok, error } = parseSpecJson(value)
  if (!ok) return callback(new Error(`规格格式错误：${error}`))
  callback()
}
