<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="auth-header">
        <h2 @click="$router.push('/')">校园二手交易平台</h2>
      </div>
      <div class="auth-content">
        <router-view />
      </div>
      <div class="auth-footer">
        <p>© {{ new Date().getFullYear() }} 校园二手交易平台 - 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// 认证布局不需要额外的逻辑
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.auth-box {
  width: 400px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.auth-header {
  text-align: center;
  padding: 20px 0;
  background-color: #409EFF;
  color: #fff;
}

.auth-header h2 {
  margin: 0;
  cursor: pointer;
}

.auth-content {
  padding: 30px;
}

.auth-footer {
  text-align: center;
  padding: 15px 0;
  font-size: 12px;
  color: #999;
  border-top: 1px solid #eee;
}
</style> 