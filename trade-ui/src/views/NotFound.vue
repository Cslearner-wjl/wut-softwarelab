<template>
  <div class="not-found-container">
    <el-result
      icon="error"
      title="404"
      sub-title="对不起，您访问的页面不存在"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
// 无需额外逻辑
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 70vh;
}
</style> 