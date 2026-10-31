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
      <el-menu :default-active="activePath" router @select="drawerVisible = false">
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
  background: var(--mall-bg-white);
  border-bottom: 1px solid var(--mall-border-light);
}

.header__inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: var(--mall-space-3);
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: var(--mall-text-1);
  white-space: nowrap;
}

.logo__mark {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: var(--mall-primary);
  color: #fff;
  font-size: 15px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.nav {
  display: flex;
  gap: 4px;
}

.nav__item {
  padding: 6px 12px;
  border-radius: var(--mall-radius-sm);
  color: var(--mall-text-2);
  white-space: nowrap;
}

.nav__item:hover {
  color: var(--mall-primary);
  background: var(--mall-primary-light);
}

.nav__item.is-active {
  color: var(--mall-primary);
  font-weight: 600;
}

.header__search {
  flex: 1;
  max-width: 340px;
  margin-left: auto;
}

.header__actions {
  display: flex;
  align-items: center;
  gap: var(--mall-space-2);
}

.cart-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--mall-text-2);
}

.cart-link:hover {
  color: var(--mall-primary);
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: var(--mall-text-2);
  max-width: 140px;
}

.nav-toggle {
  display: none;
}

.main {
  flex: 1;
  padding: var(--mall-space-3) 0 48px;
}

.footer {
  background: var(--mall-bg-white);
  border-top: 1px solid var(--mall-border-light);
  padding: var(--mall-space-3) 0;
  text-align: center;
  color: var(--mall-text-2);
  font-size: 13px;
}

.footer__inner p + p {
  margin-top: 4px;
}

.drawer-search {
  margin-top: var(--mall-space-3);
}

@media (max-width: 991px) {
  .header__search {
    display: none;
  }

  .nav {
    display: none;
  }

  .nav-toggle {
    display: inline-flex;
  }

  .cart-link__text {
    display: none;
  }
}

@media (max-width: 767px) {
  .header__actions {
    gap: 8px;
  }

  .user-entry__name {
    display: none;
  }

  .main {
    padding: var(--mall-space-2) 0 var(--mall-space-3);
  }
}
</style>
