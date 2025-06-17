<template>
  <div class="layout-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="logo" @click="$router.push('/')">
            <h1>校园二手交易平台</h1>
          </div>
          <div class="menu">
            <el-menu
              :default-active="activeMenu"
              mode="horizontal"
              router
              :ellipsis="false"
              background-color="#409EFF"
              text-color="#fff"
              active-text-color="#ffd04b"
            >
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/products">商品列表</el-menu-item>
              <el-menu-item v-if="userStore.isLoggedIn" index="/product/publish">发布商品</el-menu-item>
              <el-sub-menu v-if="userStore.isLoggedIn" index="/user">
                <template #title>
                  <el-icon><User /></el-icon>
                  个人中心
                  <el-badge v-if="userStore.unreadMessageCount > 0" :value="userStore.unreadMessageCount" class="badge" />
                </template>
                <el-menu-item index="/user">个人信息</el-menu-item>
                <el-menu-item index="/user/products">我的商品</el-menu-item>
                <el-menu-item index="/user/orders">我的订单</el-menu-item>
                <el-menu-item index="/user/messages">
                  我的消息
                  <el-badge v-if="userStore.unreadMessageCount > 0" :value="userStore.unreadMessageCount" class="badge" />
                </el-menu-item>
                <el-menu-item v-if="userStore.isAdmin" index="/admin">管理面板</el-menu-item>
                <el-menu-item @click="handleLogout">退出登录</el-menu-item>
              </el-sub-menu>
              <template v-else>
                <el-menu-item index="/auth/login">登录</el-menu-item>
                <el-menu-item index="/auth/register">注册</el-menu-item>
              </template>
            </el-menu>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <div class="container">
          <router-view />
        </div>
      </el-main>
      
      <el-footer>
        <div class="footer-content">
          <p>© {{ new Date().getFullYear() }} 校园二手交易平台 - 版权所有</p>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 处理登出
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.el-container {
  min-height: 100vh;
}

.el-header {
  background-color: #409EFF;
  color: #fff;
  padding: 0;
  height: auto !important;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  cursor: pointer;
}

.logo h1 {
  margin: 0;
  padding: 10px 0;
  font-size: 20px;
}

.menu {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.el-menu--horizontal {
  border-bottom: none;
}

.el-main {
  padding: 20px;
  flex: 1;
}

.el-footer {
  background-color: #f5f5f5;
  color: #666;
  text-align: center;
  padding: 20px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.badge {
  margin-top: -10px;
  margin-left: 5px;
}
</style> 