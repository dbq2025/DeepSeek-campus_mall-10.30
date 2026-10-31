import { ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchCart } from '@/api/cart'
import { useUserStore } from '@/stores/user'

/**
 * 购物车状态：只保存角标数量（totalQuantity）。
 * 购物车列表永远以服务端为准，不在前端缓存明细，避免"加了又没了"的双份真相。
 */
export const useCartStore = defineStore('cart', () => {
  const count = ref(0)

  function setCount(value) {
    const next = Number(value)
    count.value = Number.isFinite(next) && next > 0 ? Math.floor(next) : 0
  }

  /** 以服务端为准刷新角标 */
  async function refresh() {
    const user = useUserStore()
    if (!user.isLogin) {
      setCount(0)
      return
    }
    try {
      const data = await fetchCart()
      setCount(data?.totalQuantity || 0)
    } catch (e) {
      setCount(0)
    }
  }

  /** 局部增减，避免每次加购都拉全量列表 */
  function increase(step = 1) {
    setCount(count.value + Number(step || 1))
  }

  function decrease(step = 1) {
    setCount(count.value - Number(step || 1))
  }

  function reset() {
    count.value = 0
  }

  return { count, setCount, refresh, increase, decrease, reset }
})
