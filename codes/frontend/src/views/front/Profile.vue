<template>
  <div class="mall-container profile">
    <div class="page-card profile__head">
      <el-avatar :size="64" class="profile__avatar">{{ (user.displayName || 'U').slice(0, 1) }}</el-avatar>
      <div class="profile__meta">
        <h1 class="profile__name">{{ user.displayName }}</h1>
        <p class="text-sub">
          {{ user.isAdmin ? '管理员账号' : '注册用户' }} · 用户名 {{ user.userInfo?.username || '—' }}
        </p>
      </div>
      <el-button class="profile__logout" plain type="danger" @click="onLogout">退出登录</el-button>
    </div>

    <div class="page-card">
      <h2 class="page-title">账号资料</h2>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="昵称">{{ user.userInfo?.nickname || '—' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user.userInfo?.username || '—' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user.userInfo?.email || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ user.userInfo?.phone ? maskPhone(user.userInfo.phone) : '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="账号角色">
          {{ user.isAdmin ? 'ADMIN（可进入后台）' : 'USER（普通用户）' }}
        </el-descriptions-item>
        <el-descriptions-item label="资料维护">
          <span class="text-sub">本阶段为只读展示，资料编辑不在课程范围内</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="page-card">
      <h2 class="page-title">快捷入口</h2>
      <div class="profile__shortcuts">
        <button class="shortcut" type="button" @click="router.push('/orders')">
          <el-icon :size="22"><List /></el-icon>
          <span>我的订单</span>
        </button>
        <button class="shortcut" type="button" @click="router.push('/favorites')">
          <el-icon :size="22"><Star /></el-icon>
          <span>我的收藏</span>
        </button>
        <button class="shortcut" type="button" @click="router.push('/cart')">
          <el-icon :size="22"><ShoppingCart /></el-icon>
          <span>购物车</span>
        </button>
        <button v-if="user.isAdmin" class="shortcut" type="button" @click="router.push('/admin')">
          <el-icon :size="22"><Setting /></el-icon>
          <span>后台管理</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { maskPhone } from '@/utils/format'

const router = useRouter()
const user = useUserStore()
const cart = useCartStore()

async function onLogout() {
  await user.logout()
  cart.reset()
  ElMessage.success('已退出登录')
  router.replace('/login')
}

onMounted(() => {
  // 进入个人中心时用服务端资料校正本地缓存（防止改过库或换设备）
  user.refreshProfile()
})
</script>

<style scoped>
.profile {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile .page-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 14px;
}

.profile__head {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px 28px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
}

.profile__avatar {
  background: var(--mall-primary-gradient);
  font-size: 26px;
  font-weight: 600;
  box-shadow: 0 0 0 4px #fff, var(--mall-shadow-card);
}

.profile__meta {
  flex: 1;
  min-width: 0;
}

.profile__name {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.profile__meta .text-sub {
  margin-top: 4px;
}

.profile :deep(.el-descriptions__body) {
  border-radius: var(--mall-radius);
  overflow: hidden;
}

.profile :deep(.el-descriptions__label) {
  width: 110px;
  background: var(--mall-bg-subtle);
  color: var(--mall-text-3);
  font-weight: 500;
}

.profile :deep(.el-descriptions__cell) {
  padding: 12px 16px;
}

.profile__shortcuts {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.shortcut {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 22px 12px;
  border: 1px solid transparent;
  border-radius: 14px;
  background: var(--mall-bg-subtle);
  color: var(--mall-text-2);
  cursor: pointer;
  font-size: 14px;
  transition: transform var(--mall-dur) var(--mall-ease), box-shadow var(--mall-dur) var(--mall-ease),
    border-color var(--mall-dur-fast) ease-out, color var(--mall-dur-fast) ease-out,
    background-color var(--mall-dur-fast) ease-out;
}

.shortcut :deep(.el-icon) {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: #fff;
  color: var(--mall-primary);
  box-shadow: var(--mall-shadow-xs);
}

.shortcut:hover {
  transform: translateY(-2px);
  background: #fff;
  border-color: rgba(79, 70, 229, 0.2);
  box-shadow: var(--mall-shadow-card);
  color: var(--mall-primary);
}

@media (max-width: 1023px) {
  .profile__shortcuts {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .profile {
    gap: 14px;
  }

  .profile__head {
    flex-wrap: wrap;
    padding: 20px;
  }

  .profile__logout {
    width: 100%;
  }

  .profile__name {
    font-size: 20px;
  }
}
</style>
