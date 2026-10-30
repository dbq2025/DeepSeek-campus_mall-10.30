<template>
  <router-view />
</template>

<script setup>
// 根组件只承载路由出口，布局由 layout/ 下的组件负责
</script>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
  background: #f5f7fa;
  color: #303133;
}
</style>
