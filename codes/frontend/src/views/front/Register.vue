<template>
  <div class="auth">
    <div class="auth__card">
      <h2 class="auth__title">注册账号</h2>
      <p class="auth__subtitle">已有账号？<router-link to="/login" class="auth__link">直接登录</router-link></p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="4–20 位字母、数字或下划线" clearable />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="展示在个人中心" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="6–20 位" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-form-item label="邮箱（选填）" prop="email">
          <el-input v-model="form.email" placeholder="用于接收通知" clearable />
        </el-form-item>

        <el-button type="primary" class="auth__submit" :loading="loading" @click="onSubmit">
          注册并登录
        </el-button>
      </el-form>

      <p class="auth__tips text-sub">注册即表示同意平台的用户协议与隐私政策（课程演示项目）。</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  checkNickname,
  checkOptionalEmail,
  checkPassword,
  checkUsername,
  makeConfirmPassword
} from '@/utils/validate'

const router = useRouter()
const user = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const rules = {
  username: [{ validator: checkUsername, trigger: 'blur' }],
  nickname: [{ validator: checkNickname, trigger: 'blur' }],
  password: [{ validator: checkPassword, trigger: 'blur' }],
  confirmPassword: [{ validator: makeConfirmPassword(() => form.password), trigger: 'blur' }],
  email: [{ validator: checkOptionalEmail, trigger: 'blur' }]
}

async function onSubmit() {
  if (loading.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await user.register({
      username: form.username.trim(),
      nickname: form.nickname.trim(),
      password: form.password,
      email: form.email.trim()
    })
    ElMessage.success('注册成功，已自动登录')
    router.replace('/')
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

.auth__card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  background: var(--mall-bg-white);
  border: 1px solid var(--mall-border-light);
  border-radius: var(--mall-radius-xl);
  box-shadow: var(--mall-shadow-pop);
  padding: 40px 36px;
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

.auth__card form {
  margin-top: 24px;
}

.auth__card :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth__card :deep(.el-form-item__label) {
  padding-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--mall-text-2);
}

.auth__card :deep(.el-input__wrapper) {
  height: 44px;
  padding: 0 14px;
  border-radius: 10px;
}

.auth__submit {
  width: 100%;
  height: 44px;
  margin-top: 6px;
  border-radius: 10px;
  font-size: 15px;
}

.auth__tips {
  margin-top: 18px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--mall-text-3);
}

@media (max-width: 767px) {
  .auth {
    padding: 16px;
  }

  .auth__card {
    padding: 28px 20px;
    border-radius: var(--mall-radius-lg);
  }

  .auth__title {
    font-size: 20px;
  }
}
</style>
