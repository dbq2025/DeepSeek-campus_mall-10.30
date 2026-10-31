<template>
  <div class="auth">
    <div class="auth__panel">
      <aside class="auth__brand">
        <div class="auth__logo">校</div>
        <h1 class="auth__brand-title">校园商城</h1>
        <p class="auth__brand-desc">数码 · 文具 · 生活 · 运动<br />校内仓储，当日出库</p>
        <ul class="auth__points">
          <li>正品保障，7 天无理由退换</li>
          <li>下单即锁定库存，不怕超卖</li>
          <li>订单状态全程可追踪</li>
        </ul>
      </aside>

      <section class="auth__form">
        <h2 class="auth__title">账号登录</h2>
        <p class="auth__subtitle">还没账号？<router-link to="/register" class="auth__link">立即注册</router-link></p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @submit.prevent="onSubmit"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="onSubmit"
            />
          </el-form-item>
          <el-button type="primary" class="auth__submit" :loading="loading" @click="onSubmit">
            登录
          </el-button>
        </el-form>

        <div class="auth__footer">
          <span class="text-sub">演示账号：student / 123456</span>
          <router-link to="/admin/login" class="auth__link">管理员登录</router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { checkPassword, checkUsername } from '@/utils/validate'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const cart = useCartStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ validator: checkUsername, trigger: 'blur' }],
  password: [{ validator: checkPassword, trigger: 'blur' }]
}

async function onSubmit() {
  if (loading.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await user.login({ username: form.username.trim(), password: form.password })
    ElMessage.success('登录成功')
    await cart.refresh()
    const redirect = String(route.query.redirect || '/')
    router.replace(redirect.startsWith('/') ? redirect : '/')
  } catch (e) {
    // 请求层已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  background: radial-gradient(900px 480px at 12% -8%, rgba(79, 70, 229, 0.16), transparent 70%),
    radial-gradient(700px 420px at 92% 108%, rgba(34, 184, 207, 0.12), transparent 70%),
    var(--mall-bg);
}

.auth::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: linear-gradient(rgba(15, 23, 42, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(15, 23, 42, 0.03) 1px, transparent 1px);
  background-size: 56px 56px;
  -webkit-mask-image: radial-gradient(ellipse at 50% 40%, #000 20%, transparent 72%);
  mask-image: radial-gradient(ellipse at 50% 40%, #000 20%, transparent 72%);
}

.auth__panel {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 900px;
  background: var(--mall-bg-white);
  border: 1px solid var(--mall-border-light);
  border-radius: var(--mall-radius-xl);
  box-shadow: var(--mall-shadow-pop);
  display: flex;
  overflow: hidden;
}

.auth__brand {
  position: relative;
  flex: 0 0 340px;
  padding: 44px 32px;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(160deg, #4f46e5 0%, #6d5be8 55%, #7c3aed 100%);
}

.auth__brand::after {
  content: '';
  position: absolute;
  right: -120px;
  bottom: -140px;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.24), transparent 70%);
}

.auth__logo {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
}

.auth__brand-title {
  margin-top: 22px;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: #fff;
}

.auth__brand-desc {
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.86);
}

.auth__points {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.92);
}

.auth__points li {
  position: relative;
  padding-left: 18px;
}

.auth__points li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 6px;
  height: 6px;
  margin-top: -3px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.72);
}

.auth__form {
  flex: 1;
  min-width: 0;
  padding: 48px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth__title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.auth__subtitle {
  margin-top: 8px;
  font-size: 13px;
  color: var(--mall-text-3);
}

.auth__link {
  color: var(--mall-primary);
  font-weight: 500;
}

.auth__link:hover {
  color: var(--mall-primary-hover);
}

.auth__form form {
  margin-top: 26px;
}

.auth__form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.auth__form :deep(.el-form-item__label) {
  padding-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--mall-text-2);
}

.auth__form :deep(.el-input__wrapper) {
  height: 44px;
  padding: 0 14px;
  border-radius: 10px;
}

.auth__submit {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-size: 15px;
}

.auth__footer {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid var(--mall-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

@media (max-width: 900px) {
  .auth__brand {
    display: none;
  }

  .auth__form {
    padding: 40px 32px;
  }
}

@media (max-width: 767px) {
  .auth {
    padding: 16px;
  }

  .auth__panel {
    border-radius: var(--mall-radius-lg);
  }

  .auth__form {
    padding: 30px 20px;
  }

  .auth__title {
    font-size: 20px;
  }
}
</style>
