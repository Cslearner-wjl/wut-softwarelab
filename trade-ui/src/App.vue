<script setup>
import { onMounted, watch, ref } from 'vue'
import { useUserStore } from './stores/user'
import { useRouter } from 'vue-router'
import api from './api'
import { ElMessage, ElLoading } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const initializationComplete = ref(false)

// 初始化应用
const initializeApp = async () => {
  console.log('初始化应用...')
  console.log('当前登录状态:', userStore.isLoggedIn)
  
  let loadingInstance = ElLoading.service({
    lock: true,
    text: '正在加载应用...',
    background: 'rgba(255, 255, 255, 0.7)'
  })
  
  try {
    // 如果localStorage中有token但pinia存储没有用户信息，尝试获取用户信息
    if (localStorage.getItem('token') && !userStore.user) {
      try {
        // 测试公开API
        await api.auth.test()
        console.log('公开API测试成功')
        
        // 尝试验证token并获取用户信息
        const validAuth = await userStore.checkAuthStatus()
        if (validAuth) {
          console.log('用户认证有效')
          userStore.fetchUnreadMessageCount()
          ElMessage.success('欢迎回来，' + userStore.user.nickname)
        } else {
          console.log('用户认证已过期')
          ElMessage.warning('登录已过期，请重新登录')
          if (router.currentRoute.value.meta.requiresAuth) {
            router.push('/auth/login')
          }
        }
      } catch (error) {
        console.error('初始化失败:', error)
        ElMessage.error('系统初始化失败，请刷新页面重试')
      }
    } else {
      console.log('无需初始化用户状态')
    }
  } finally {
    loadingInstance.close()
    initializationComplete.value = true
    console.log('初始化完成')
  }
}

onMounted(() => {
  initializeApp()
})

// 监听路由变化
watch(
  () => router.currentRoute.value.path,
  (newPath) => {
    console.log('路由变化:', newPath)
    
    // 如果用户已登录但没有用户信息，可能是页面刷新导致的，尝试重新获取用户信息
    if (localStorage.getItem('token') && !userStore.user && initializationComplete.value) {
      userStore.fetchUserProfile()
    }
    
    // 如果访问需要认证的页面但未登录，重定向到登录页
    const requiresAuth = router.currentRoute.value.meta.requiresAuth
    if (requiresAuth && !userStore.isLoggedIn) {
      console.log('访问需要认证的页面但未登录，重定向到登录页')
      ElMessage.warning('请先登录')
      router.push('/auth/login')
    }
  }
)
</script>

<template>
  <router-view />
</template>

<style>
/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

#app {
  height: 100%;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 通用样式 */
.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: bold;
}

.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.mt-20 {
  margin-top: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
