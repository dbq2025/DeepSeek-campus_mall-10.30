import { ref, reactive } from 'vue'

/**
 * 列表查询通用逻辑：把"查询条件 + 分页 + 四态 + 竞态保护"收在一处，
 * 避免 8 个列表页各写一遍。
 *
 * @param {(params: object) => Promise<{records: any[], total: number}>} fetcher
 * @param {object} initialQuery 初始查询条件（不含 page/size）
 * @param {{ immediate?: boolean, pageSize?: number }} options
 */
export function useListQuery(fetcher, initialQuery = {}, options = {}) {
  const { immediate = true, pageSize = 10 } = options
  const defaults = { ...initialQuery }

  const query = reactive({ ...defaults, page: 1, size: pageSize })
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref('')

  // 竞态保护：只接受最后一次请求的结果（翻页过快时丢弃过期响应）
  let seq = 0

  async function load() {
    const current = ++seq
    loading.value = true
    error.value = ''
    try {
      const data = await fetcher({ ...query })
      if (current !== seq) return
      list.value = data?.records || []
      total.value = Number(data?.total || 0)
    } catch (e) {
      if (current !== seq) return
      list.value = []
      total.value = 0
      error.value = e?.message || '加载失败'
    } finally {
      if (current === seq) loading.value = false
    }
  }

  /** 改条件后从第 1 页查 */
  function search() {
    query.page = 1
    return load()
  }

  /** 恢复初始条件 */
  function reset() {
    Object.keys(query).forEach((key) => {
      if (key !== 'page' && key !== 'size') delete query[key]
    })
    Object.assign(query, defaults)
    query.page = 1
    return load()
  }

  function setPage(page) {
    query.page = page
    return load()
  }

  function setSize(size) {
    query.size = size
    query.page = 1
    return load()
  }

  if (immediate) load()

  return { query, list, total, loading, error, load, search, reset, setPage, setSize }
}
