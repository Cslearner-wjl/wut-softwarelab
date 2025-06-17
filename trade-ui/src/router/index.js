import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('../views/Products.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../views/ProductDetail.vue'),
        meta: { title: '商品详情' },
        props: true
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('../views/user/UserCenter.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'user/products',
        name: 'UserProducts',
        component: () => import('../views/user/UserProducts.vue'),
        meta: { title: '我的商品', requiresAuth: true }
      },
      {
        path: 'user/orders',
        name: 'UserOrders',
        component: () => import('../views/user/UserOrders.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'user/messages',
        name: 'UserMessages',
        component: () => import('../views/user/UserMessages.vue'),
        meta: { title: '我的消息', requiresAuth: true }
      },
      {
        path: 'product/publish',
        name: 'ProductPublish',
        component: () => import('../views/user/ProductPublish.vue'),
        meta: { title: '发布商品', requiresAuth: true }
      }
    ]
  },
  {
    path: '/auth',
    component: () => import('../layouts/AuthLayout.vue'),
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('../views/auth/Login.vue'),
        meta: { title: '登录' }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('../views/auth/Register.vue'),
        meta: { title: '注册' }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '管理面板' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/admin/Products.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'performance',
        name: 'AdminPerformance',
        component: () => import('../views/admin/PerformanceMonitor.vue'),
        meta: { title: '性能监控' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  // 添加滚动行为
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 修复导航重复错误
const originalPush = router.push
router.push = function push(location) {
  return originalPush.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') {
      throw err
    }
  })
}

// 导航守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园二手交易平台` : '校园二手交易平台'
  
  // 权限检查
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 需要登录但未登录，重定向到登录页面
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    // 需要管理员权限但不是管理员，重定向到首页
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router 