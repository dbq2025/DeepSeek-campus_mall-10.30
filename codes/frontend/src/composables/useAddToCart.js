import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { addToCart as apiAddToCart } from '@/api/cart'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

/**
 * "加入购物车"的统一入口。
 * 规则：未登录先引导登录；有规格的商品必须先到详情页选规格（规格是快照，不能在列表页猜）。
 */
export function useAddToCart() {
  const router = useRouter()
  const route = useRoute()
  const user = useUserStore()
  const cart = useCartStore()

  async function add(product, { quantity = 1, specText = '' } = {}) {
    if (!user.isLogin) {
      ElMessage.warning('请先登录')
      router.push({ path: '/login', query: { redirect: route.fullPath } })
      return false
    }

    if (product.specJson) {
      ElMessage.info('该商品有多个规格，请先在详情页选择')
      router.push(`/products/${product.id}`)
      return false
    }

    try {
      await apiAddToCart({ productId: product.id, quantity, specText })
      cart.increase(quantity)
      ElMessage.success('已加入购物车')
      return true
    } catch (e) {
      // 错误提示已由请求层统一处理
      return false
    }
  }

  return { add }
}
