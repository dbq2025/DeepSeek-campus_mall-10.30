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
        background-color="#1f2d3d"
        text-color="#c0c4cc"
        active-text-color="#ffffff"
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
  background: #1f2d3d;
  transition: width 0.2s, flex-basis 0.2s;
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
  padding: 0 16px;
  color: #fff;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  white-space: nowrap;
}

.brand__mark {
  width: 26px;
  height: 26px;
  flex: 0 0 26px;
  border-radius: 6px;
  background: var(--mall-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.menu {
  border-right: none;
}

.menu :deep(.el-menu-item.is-active) {
  background: var(--mall-primary);
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
  background: var(--mall-bg-white);
  border-bottom: 1px solid var(--mall-border-light);
  display: flex;
  align-items: center;
  gap: var(--mall-space-1);
  padding: 0 var(--mall-space-2);
}

.topbar__title {
  font-size: 15px;
  font-weight: 600;
}

.topbar__right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: var(--mall-space-2);
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: var(--mall-text-2);
}

.content {
  flex: 1;
  padding: var(--mall-space-2);
  min-width: 0;
}

@media (max-width: 1023px) {
  .sider {
    width: var(--mall-admin-sider-collapsed);
    flex-basis: var(--mall-admin-sider-collapsed);
  }

  .brand__text,
  .user-entry__name {
    display: none;
  }

  .content {
    padding: 12px;
  }
}
</style>
