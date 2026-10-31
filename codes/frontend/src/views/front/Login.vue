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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--mall-space-3);
  background: linear-gradient(135deg, #e8f3ff 0%, #f5f7fa 60%, #eef7ee 100%);
}

.auth__panel {
  width: 100%;
  max-width: 860px;
  background: var(--mall-bg-white);
  border-radius: 12px;
  box-shadow: var(--mall-shadow-card);
  display: flex;
  overflow: hidden;
}

.auth__brand {
  flex: 0 0 320px;
  padding: 40px 28px;
  background: linear-gradient(160deg, #409eff, #2b7cd3);
  color: #fff;
}

.auth__logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
}

.auth__brand-title {
  margin-top: 18px;
  font-size: 22px;
  font-weight: 700;
}

.auth__brand-desc {
  margin-top: 8px;
  opacity: 0.9;
  line-height: 1.8;
}

.auth__points {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
  opacity: 0.92;
}

.auth__points li::before {
  content: '·';
  margin-right: 6px;
  font-weight: 700;
}

.auth__form {
  flex: 1;
  padding: 40px 36px;
}

.auth__title {
  font-size: 20px;
  font-weight: 600;
}

.auth__subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: var(--mall-text-3);
}

.auth__link {
  color: var(--mall-primary);
}

.auth__form form {
  margin-top: 22px;
}

.auth__submit {
  width: 100%;
  margin-top: 4px;
}

.auth__footer {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

@media (max-width: 767px) {
  .auth {
    padding: 12px;
  }

  .auth__brand {
    display: none;
  }

  .auth__form {
    padding: 28px 20px;
  }
}
</style>
