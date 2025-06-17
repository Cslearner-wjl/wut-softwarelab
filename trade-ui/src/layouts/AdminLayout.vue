<template>
  <div class="admin-layout">
    <el-container class="layout-container">
      <!-- 侧边栏 -->
      <el-aside width="220px">
        <div class="logo-container">
          <h2>校园二手交易平台</h2>
          <div class="subtitle">管理后台</div>
        </div>
        <el-menu
          router
          :default-active="$route.path"
          class="el-menu-vertical"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/admin">
            <el-icon><Odometer /></el-icon>
            <span>管理面板</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/performance">
            <el-icon><Histogram /></el-icon>
            <span>性能监控</span>
          </el-menu-item>
          
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>返回前台</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主要内容区域 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header height="60px">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin' }">管理后台</el-breadcrumb-item>
              <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="el-dropdown-link">
                <el-avatar :size="32" icon="User" />
                <span class="username">{{ userStore.user?.username }}</span>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 内容区域 -->
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
        
        <!-- 页脚 -->
        <el-footer height="50px">
          <div class="footer-content">
            <span>校园二手交易平台 © {{ new Date().getFullYear() }} 管理员系统</span>
          </div>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Odometer,
  User,
  Goods,
  House,
  ArrowDown,
  Histogram
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '退出登录',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      userStore.logout()
      ElMessage.success('已成功退出登录')
      router.push('/auth/login')
    })
    .catch(() => {})
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.layout-container {
  height: 100vh;
}

/* 侧边栏样式 */
.el-aside {
  background-color: #304156;
  color: #bfcbd9;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 60px;
  padding: 10px 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background-color: #263445;
}

.logo-container h2 {
  margin: 0;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}

.subtitle {
  font-size: 12px;
  color: #97a8be;
  margin-top: 4px;
}

.el-menu {
  border-right: none;
  flex-grow: 1;
}

.el-menu-item {
  display: flex;
  align-items: center;
}

.el-menu-item .el-icon {
  margin-right: 10px;
}

/* 顶部导航栏样式 */
.el-header {
  background-color: #fff;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.username {
  margin: 0 8px;
  color: #606266;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

/* 内容区域样式 */
.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* 页脚样式 */
.el-footer {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
  border-top: 1px solid #e6e6e6;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 