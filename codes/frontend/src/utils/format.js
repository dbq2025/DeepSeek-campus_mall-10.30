/**
 * 展示层格式化工具。
 * 铁律：ID 与金额全链路按字符串处理，不做 parseInt、不做浮点乘除。
 * 唯一允许的"运算"是金额求和，实现方式为字符串 → 整数分 → 相加 → 回字符串，不产生浮点误差。
 */

/** 金额字符串规范化：'99' -> '99.00'，非法输入返回 '0.00' */
export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '0.00'
  const str = String(value).trim()
  if (!/^-?\d+(\.\d+)?$/.test(str)) return '0.00'
  const negative = str.startsWith('-')
  const [intPart, decPart = ''] = (negative ? str.slice(1) : str).split('.')
  const cents = `${intPart}${(decPart + '00').slice(0, 2)}`.replace(/^0+(?=\d)/, '')
  const padded = cents.padStart(3, '0')
  const result = `${padded.slice(0, -2)}.${padded.slice(-2)}`
  return negative && result !== '0.00' ? `-${result}` : result
}

/** 金额字符串 → 整数分（仅用于求和预览，不参与业务判定） */
function toCents(value) {
  const normalized = formatMoney(value)
  const negative = normalized.startsWith('-')
  const digits = (negative ? normalized.slice(1) : normalized).replace('.', '')
  const cents = Number(digits)
  if (!Number.isSafeInteger(cents)) return 0
  return negative ? -cents : cents
}

/** 多个金额字符串求和，返回两位小数字符串 */
export function sumMoney(values = []) {
  const total = values.reduce((acc, item) => acc + toCents(item), 0)
  const negative = total < 0
  const abs = Math.abs(total)
  const result = `${Math.floor(abs / 100)}.${String(abs % 100).padStart(2, '0')}`
  return negative ? `-${result}` : result
}

/** 单价 × 数量（字符串金额，整数数量） */
export function multiplyMoney(price, quantity) {
  const cents = toCents(price) * (Number(quantity) || 0)
  const negative = cents < 0
  const abs = Math.abs(cents)
  const result = `${Math.floor(abs / 100)}.${String(abs % 100).padStart(2, '0')}`
  return negative ? `-${result}` : result
}

/** 时间展示：兼容 'yyyy-MM-dd HH:mm:ss' 与 ISO，空值返回 '—' */
export function formatDateTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 19)
}

/** 只保留日期部分 */
export function formatDate(value) {
  if (!value) return '—'
  return String(value).slice(0, 10)
}

/** 手机号脱敏：138****8000 */
export function maskPhone(phone) {
  if (!phone || String(phone).length !== 11) return phone || '—'
  return `${String(phone).slice(0, 3)}****${String(phone).slice(7)}`
}

/** 规格快照 '颜色:黑;尺码:M' → ['颜色:黑', '尺码:M'] */
export function splitSpecText(specText) {
  if (!specText) return []
  return String(specText)
    .split(';')
    .map((item) => item.trim())
    .filter(Boolean)
}

/** 数量展示上限，避免详情页标题被撑开 */
export function truncate(text, max = 60) {
  if (!text) return ''
  const str = String(text)
  return str.length > max ? `${str.slice(0, max)}…` : str
}
