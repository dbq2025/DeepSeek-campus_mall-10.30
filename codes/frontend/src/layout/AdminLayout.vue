<template>
  <div class="admin-layout">
    <aside class="sider" :class="{ 'sider--collapsed': collapsed }">
      <div class="brand">
        <span class="brand__mark">校</span>
        <span v-show="!collapsed" class="brand__text">商城后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        class="menu"
      >
        <el-menu-item index="/admin">
          <el-icon><DataLine /></el-icon>
          <template #title>数据概览</template>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Files /></el-icon>
          <template #title>类型管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <template #title>商品管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/stats">
          <el-icon><TrendCharts /></el-icon>
          <template #title>数据统计</template>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="body">
      <header class="topbar">
        <el-button text :icon="collapsed ? Expand : Fold" aria-label="折叠菜单" @click="toggle" />
        <span class="topbar__title">{{ pageTitle }}</span>
        <div class="topbar__right">
          <el-button link @click="goFront">前台首页</el-button>
          <el-dropdown trigger="click" @command="onCommand">
            <span class="user-entry">
              <el-avatar :size="26">{{ (user.displayName || 'A').slice(0, 1) }}</el-avatar>
              <span class="user-entry__name">{{ user.displayName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Expand, Fold } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const collapsed = ref(false)
const activeMenu = computed(() => route.meta.activeMenu || route.path)
const pageTitle = computed(() => route.meta.title || '后台管理')

function toggle() {
  collapsed.value = !collapsed.value
}

function goFront() {
  router.push('/')
}

async function onCommand(command) {
  if (command === 'logout') {
    await user.logout()
    ElMessage.success('已退出登录')
    router.replace('/admin/login')
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--mall-bg);
}

.sider {
  width: var(--mall-admin-sider);
  flex: 0 0 var(--mall-admin-sider);
  background: linear-gradient(180deg, #111827 0%, #0b1220 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  transition: width 0.2s var(--mall-ease), flex-basis 0.2s var(--mall-ease);
  overflow-x: hidden;
}

.sider--collapsed {
  width: var(--mall-admin-sider-collapsed);
  flex-basis: var(--mall-admin-sider-collapsed);
}

.brand {
  height: var(--mall-admin-header-h);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  color: #fff;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
  white-space: nowrap;
}

.brand__mark {
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  border-radius: 9px;
  background: var(--mall-primary-gradient);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  box-shadow: 0 6px 14px -6px rgba(79, 70, 229, 0.9);
}

.brand__text {
  font-size: 15px;
  letter-spacing: 0.01em;
}

.menu {
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #cbd5e1;
  --el-menu-active-color: #ffffff;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.06);
  --el-menu-item-height: 42px;
  border-right: none;
  padding: 10px 10px 16px;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  height: 42px;
  line-height: 42px;
  margin-bottom: 4px;
  border-radius: 10px;
  color: #cbd5e1;
  font-size: 14px;
}

.menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(79, 70, 229, 0.95) 0%, rgba(124, 58, 237, 0.88) 100%);
  color: #fff;
  font-weight: 500;
  box-shadow: 0 10px 24px -12px rgba(79, 70, 229, 0.9);
}

.sider--collapsed .menu {
  padding: 10px 8px;
}

.sider--collapsed .menu :deep(.el-menu-item) {
  padding: 0 !important;
  justify-content: center;
}

.body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: var(--mall-admin-header-h);
  flex: 0 0 var(--mall-admin-header-h);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: saturate(180%) blur(12px);
  -webkit-backdrop-filter: saturate(180%) blur(12px);
  border-bottom: 1px solid var(--mall-border-light);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
}

.topbar__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--mall-text-1);
}

.topbar__right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.topbar__right :deep(.el-button.is-link) {
  color: var(--mall-text-2);
  font-weight: 400;
}

.topbar__right :deep(.el-button.is-link:hover) {
  color: var(--mall-primary);
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border-radius: 999px;
  cursor: pointer;
  color: var(--mall-text-2);
  transition: background-color var(--mall-dur-fast) ease-out;
}

.user-entry:hover {
  background: var(--mall-bg-subtle);
}

.user-entry :deep(.el-avatar) {
  background: var(--mall-primary-soft);
  color: var(--mall-primary);
  font-weight: 600;
  border: 2px solid #fff;
  box-shadow: var(--mall-shadow-xs);
}

.content {
  flex: 1;
  padding: 20px 24px 28px;
  min-width: 0;
}

@media (max-width: 1023px) {
  .sider {
    width: var(--mall-admin-sider-collapsed);
    flex-basis: var(--mall-admin-sider-collapsed);
  }

  .brand {
    padding: 0 17px;
  }

  .brand__text,
  .user-entry__name {
    display: none;
  }

  .topbar {
    padding: 0 12px;
  }

  .content {
    padding: 14px 12px 20px;
  }
}
</style>
