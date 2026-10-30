<template>
  <div class="wrap">
    <el-card class="login-card" shadow="never">
      <h2>后台管理登录</h2>
      <p class="tip">占位页面：登录接口尚未实现，明日按契约 <code>AUTH-02</code> 接入。</p>
      <el-form :model="form" label-width="64px" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="管理员账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-button type="primary" class="submit" disabled>登录（待接入）</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({ username: '', password: '' })
</script>

<style scoped>
.wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f5f7fa;
}

.login-card {
  width: 380px;
  border-radius: 12px;
}

h2 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 500;
}

.tip {
  margin: 0 0 18px;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

.submit {
  width: 100%;
}

code {
  background: #f2f3f5;
  border-radius: 3px;
  padding: 1px 4px;
}
</style>
