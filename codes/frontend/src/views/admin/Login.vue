<template>
  <div class="admin-login">
    <div class="admin-login__card">
      <div class="admin-login__head">
        <span class="admin-login__mark">校</span>
        <div>
          <h1 class="admin-login__title">校园商城 · 管理后台</h1>
          <p class="admin-login__subtitle">仅限管理员账号登录</p>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="管理员账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" clearable />
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

        <el-button type="primary" class="admin-login__submit" :loading="loading" @click="onSubmit">
          登录后台
        </el-button>
      </el-form>

      <div class="admin-login__footer">
        <span class="text-sub">演示账号：admin / 123456</span>
        <router-link to="/login" class="admin-login__link">返回用户登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { checkPassword, checkUsername } from '@/utils/validate'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

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
    await user.adminLogin({ username: form.username.trim(), password: form.password })
    ElMessage.success('登录成功')
    const redirect = String(route.query.redirect || '/admin')
    router.replace(redirect.startsWith('/admin') ? redirect : '/admin')
  } catch (e) {
    // 请求层已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  background: radial-gradient(800px 500px at 18% 12%, rgba(79, 70, 229, 0.38), transparent 62%),
    radial-gradient(700px 460px at 82% 88%, rgba(34, 184, 207, 0.18), transparent 62%),
    linear-gradient(160deg, #0f172a 0%, #111827 55%, #0b1220 100%);
}

.admin-login::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 64px 64px;
  -webkit-mask-image: radial-gradient(ellipse at 50% 45%, #000 20%, transparent 74%);
  mask-image: radial-gradient(ellipse at 50% 45%, #000 20%, transparent 74%);
}

.admin-login__card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  background: var(--mall-bg-white);
  border-radius: var(--mall-radius-xl);
  padding: 36px 32px;
  box-shadow: 0 30px 60px -24px rgba(0, 0, 0, 0.55);
}

.admin-login__head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

.admin-login__mark {
  width: 44px;
  height: 44px;
  flex: 0 0 44px;
  border-radius: 12px;
  background: var(--mall-primary-gradient);
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--mall-shadow-brand);
}

.admin-login__title {
  font-size: 19px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.admin-login__subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: var(--mall-text-3);
}

.admin-login__card :deep(.el-form-item) {
  margin-bottom: 20px;
}

.admin-login__card :deep(.el-form-item__label) {
  padding-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--mall-text-2);
}

.admin-login__card :deep(.el-input__wrapper) {
  height: 44px;
  padding: 0 14px;
  border-radius: 10px;
}

.admin-login__submit {
  width: 100%;
  height: 44px;
  margin-top: 6px;
  border-radius: 10px;
  font-size: 15px;
}

.admin-login__footer {
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid var(--mall-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}

.admin-login__link {
  color: var(--mall-primary);
  font-weight: 500;
}

.admin-login__link:hover {
  color: var(--mall-primary-hover);
}

@media (max-width: 767px) {
  .admin-login {
    padding: 16px;
  }

  .admin-login__card {
    padding: 28px 20px;
    border-radius: var(--mall-radius-lg);
  }
}
</style>
