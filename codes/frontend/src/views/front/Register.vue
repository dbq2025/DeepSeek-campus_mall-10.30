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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--mall-space-3);
  background: linear-gradient(135deg, #e8f3ff 0%, #f5f7fa 60%, #eef7ee 100%);
}

.auth__card {
  width: 100%;
  max-width: 440px;
  background: var(--mall-bg-white);
  border-radius: 12px;
  box-shadow: var(--mall-shadow-card);
  padding: 32px 28px;
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

.auth__card form {
  margin-top: 20px;
}

.auth__submit {
  width: 100%;
}

.auth__tips {
  margin-top: 16px;
  font-size: 12px;
  line-height: 1.7;
}

@media (max-width: 767px) {
  .auth {
    padding: 12px;
  }

  .auth__card {
    padding: 24px 18px;
  }
}
</style>
