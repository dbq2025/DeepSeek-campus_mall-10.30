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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--mall-space-3);
  background: #1f2d3d;
  background-image: radial-gradient(circle at 20% 20%, rgba(64, 158, 255, 0.25), transparent 45%),
    radial-gradient(circle at 80% 80%, rgba(103, 194, 58, 0.18), transparent 45%);
}

.admin-login__card {
  width: 100%;
  max-width: 400px;
  background: var(--mall-bg-white);
  border-radius: 12px;
  padding: 32px 28px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.28);
}

.admin-login__head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.admin-login__mark {
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  border-radius: 10px;
  background: var(--mall-primary);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.admin-login__title {
  font-size: 17px;
  font-weight: 600;
}

.admin-login__subtitle {
  font-size: 12px;
  color: var(--mall-text-3);
}

.admin-login__submit {
  width: 100%;
  margin-top: 4px;
}

.admin-login__footer {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}

.admin-login__link {
  color: var(--mall-primary);
}
</style>
