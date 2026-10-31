<template>
  <div class="front-layout">
    <header class="header">
      <div class="mall-container header__inner">
        <router-link to="/" class="logo">
          <span class="logo__mark">校</span>
          <span class="logo__text">校园商城</span>
        </router-link>

        <nav class="nav">
          <router-link to="/" class="nav__item" :class="{ 'is-active': route.path === '/' }">
            首页
          </router-link>
          <router-link
            to="/products"
            class="nav__item"
            :class="{ 'is-active': route.path.startsWith('/products') }"
          >
            全部商品
          </router-link>
          <router-link
            v-if="user.isLogin"
            to="/orders"
            class="nav__item"
            :class="{ 'is-active': route.path.startsWith('/orders') }"
          >
            我的订单
          </router-link>
        </nav>

        <div class="header__search">
          <el-input
            v-model="keyword"
            placeholder="搜索商品名称"
            clearable
            @keyup.enter="onSearch"
          >
            <template #append>
              <el-button :icon="Search" aria-label="搜索" @click="onSearch" />
            </template>
          </el-input>
        </div>

        <div class="header__actions">
          <router-link to="/cart" class="cart-link">
            <el-badge :value="cart.count" :hidden="cart.count === 0" :max="99">
              <el-icon :size="20"><ShoppingCart /></el-icon>
            </el-badge>
            <span class="cart-link__text">购物车</span>
          </router-link>

          <el-dropdown v-if="user.isLogin" trigger="click" @command="onCommand">
            <span class="user-entry">
              <el-avatar :size="26" :src="user.userInfo?.avatar || ''">
                {{ (user.displayName || 'U').slice(0, 1) }}
              </el-avatar>
              <span class="user-entry__name text-ellipsis">{{ user.displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item v-if="user.isAdmin" command="admin" divided>后台管理</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <template v-else>
            <el-button text @click="go('/login')">登录</el-button>
            <el-button type="primary" @click="go('/register')">注册</el-button>
          </template>

          <el-button class="nav-toggle" text :icon="Menu" aria-label="菜单" @click="drawerVisible = true" />
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <footer class="footer">
      <div class="mall-container footer__inner">
        <p>校园电商平台 · 软件工程专业课程设计</p>
        <p class="text-sub">前端 Vue 3 + Element Plus ｜ 后端 Spring Boot 3 + MyBatis-Plus</p>
      </div>
    </footer>

    <el-drawer v-model="drawerVisible" title="导航" size="70%" direction="rtl">
      <el-menu
        class="drawer-menu"
        :default-active="activePath"
        router
        @select="drawerVisible = false"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/products">全部商品</el-menu-item>
        <el-menu-item v-if="user.isLogin" index="/cart">购物车</el-menu-item>
        <el-menu-item v-if="user.isLogin" index="/orders">我的订单</el-menu-item>
        <el-menu-item v-if="user.isLogin" index="/favorites">我的收藏</el-menu-item>
        <el-menu-item v-if="user.isLogin" index="/profile">个人中心</el-menu-item>
        <el-menu-item v-if="!user.isLogin" index="/login">登录</el-menu-item>
        <el-menu-item v-if="!user.isLogin" index="/register">注册</el-menu-item>
      </el-menu>
      <div class="drawer-search">
        <el-input v-model="keyword" placeholder="搜索商品名称" @keyup.enter="onSearch">
          <template #append>
            <el-button :icon="Search" @click="onSearch" />
          </template>
        </el-input>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Menu, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const cart = useCartStore()

const keyword = ref('')
const drawerVisible = ref(false)
const activePath = computed(() => route.path)

function go(path) {
  router.push(path)
}

function onSearch() {
  const word = keyword.value.trim()
  drawerVisible.value = false
  router.push({ path: '/products', query: word ? { keyword: word } : {} })
}

async function onCommand(command) {
  if (command === 'logout') {
    await user.logout()
    cart.reset()
    ElMessage.success('已退出登录')
    router.push('/')
    return
  }
  if (command === 'admin') {
    router.push('/admin')
    return
  }
  router.push(`/${command}`)
}

onMounted(() => {
  if (user.isLogin) cart.refresh()
  keyword.value = String(route.query.keyword || '')
})
</script>

<style scoped>
.front-layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: var(--mall-header-h);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: saturate(180%) blur(14px);
  -webkit-backdrop-filter: saturate(180%) blur(14px);
  border-bottom: 1px solid var(--mall-border-light);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
}

.header__inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 28px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--mall-text-1);
  white-space: nowrap;
}

.logo__mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: var(--mall-primary-gradient);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--mall-shadow-brand);
  transition: transform var(--mall-dur) var(--mall-ease);
}

.logo:hover .logo__mark {
  transform: translateY(-1px) scale(1.03);
}

.nav {
  display: flex;
  gap: 2px;
}

.nav__item {
  padding: 7px 14px;
  border-radius: 10px;
  color: var(--mall-text-2);
  white-space: nowrap;
  transition: color var(--mall-dur-fast) ease-out, background-color var(--mall-dur-fast) ease-out;
}

.nav__item:hover {
  color: var(--mall-primary);
  background: var(--mall-primary-soft);
}

.nav__item.is-active {
  color: var(--mall-primary);
  background: var(--mall-primary-soft);
  font-weight: 600;
}

.header__search {
  flex: 1;
  max-width: 380px;
  margin-left: auto;
}

.header__search :deep(.el-input__wrapper) {
  height: 40px;
  padding-left: 16px;
  border-radius: var(--mall-radius-pill) 0 0 var(--mall-radius-pill);
  background: var(--mall-bg-subtle);
  box-shadow: inset 0 0 0 1px var(--mall-border-light);
  transition: background-color var(--mall-dur-fast) ease-out,
    box-shadow var(--mall-dur-fast) ease-out;
}

.header__search :deep(.el-input__wrapper:hover) {
  box-shadow: inset 0 0 0 1px var(--mall-border);
}

.header__search :deep(.el-input__wrapper.is-focus) {
  background: var(--mall-bg-white);
  box-shadow: inset 0 0 0 1px var(--mall-primary), 0 0 0 3px var(--mall-primary-ring);
}

.header__search :deep(.el-input-group__append) {
  padding: 0;
  border: none;
  background: var(--mall-primary-soft);
  border-radius: 0 var(--mall-radius-pill) var(--mall-radius-pill) 0;
  box-shadow: inset 0 0 0 1px var(--mall-border-light);
}

.header__search :deep(.el-input-group__append .el-button) {
  width: 50px;
  height: 40px;
  margin: 0;
  color: var(--mall-primary);
  border-radius: 0 var(--mall-radius-pill) var(--mall-radius-pill) 0;
  transition: background-color var(--mall-dur-fast) ease-out, color var(--mall-dur-fast) ease-out;
}

.header__search :deep(.el-input-group__append .el-button:hover) {
  background: var(--mall-primary);
  color: #fff;
}

.header__actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.cart-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  border-radius: 10px;
  color: var(--mall-text-2);
  transition: color var(--mall-dur-fast) ease-out, background-color var(--mall-dur-fast) ease-out;
}

.cart-link:hover {
  color: var(--mall-primary);
  background: var(--mall-primary-soft);
}

.cart-link__text {
  font-size: 14px;
}

.cart-link :deep(.el-badge__content) {
  border: none;
  font-weight: 600;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px 5px 5px;
  border-radius: var(--mall-radius-pill);
  cursor: pointer;
  color: var(--mall-text-2);
  max-width: 160px;
  transition: background-color var(--mall-dur-fast) ease-out;
}

.user-entry:hover {
  background: var(--mall-bg-subtle);
}

.user-entry :deep(.el-avatar) {
  border: 2px solid #fff;
  box-shadow: var(--mall-shadow-xs);
  background: var(--mall-primary-soft);
  color: var(--mall-primary);
  font-weight: 600;
}

.nav-toggle {
  display: none;
}

.main {
  flex: 1;
  padding: var(--mall-space-3) 0 var(--mall-space-7);
}

.footer {
  position: relative;
  background: var(--mall-bg-white);
  border-top: 1px solid var(--mall-border-light);
  padding: var(--mall-space-4) 0;
  text-align: center;
  color: var(--mall-text-2);
  font-size: 13px;
}

.footer::before {
  content: '';
  position: absolute;
  top: -1px;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(79, 70, 229, 0.22), transparent);
}

.footer__inner p + p {
  margin-top: 6px;
}

.drawer-menu {
  border-right: none;
  padding: 0 8px;
}

.drawer-menu :deep(.el-menu-item) {
  height: 46px;
  margin-bottom: 4px;
  border-radius: 10px;
  color: var(--mall-text-2);
}

.drawer-menu :deep(.el-menu-item:hover) {
  background: var(--mall-bg-subtle);
}

.drawer-menu :deep(.el-menu-item.is-active) {
  background: var(--mall-primary-soft);
  color: var(--mall-primary);
  font-weight: 600;
}

.drawer-search {
  margin-top: var(--mall-space-3);
}

@media (max-width: 991px) {
  .header__inner {
    gap: 16px;
  }

  .header__search {
    display: none;
  }

  .nav {
    display: none;
  }

  .nav-toggle {
    display: inline-flex;
  }

  .header__actions {
    margin-left: auto;
  }

  .cart-link__text {
    display: none;
  }
}

@media (max-width: 767px) {
  .header__actions {
    gap: 2px;
  }

  .logo {
    font-size: 16px;
  }

  .logo__mark {
    width: 30px;
    height: 30px;
    border-radius: 9px;
  }

  .user-entry__name {
    display: none;
  }

  .main {
    padding: var(--mall-space-2) 0 var(--mall-space-5);
  }
}
</style>
