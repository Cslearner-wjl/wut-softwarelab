<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>用户数据</h3>
            </div>
          </template>
          <div class="statistic-value">{{ stats.userCount }}</div>
          <div class="statistic-label">注册用户总数</div>
          <div class="card-actions">
            <el-button type="primary" @click="$router.push('/admin/users')">
              查看用户
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>商品数据</h3>
            </div>
          </template>
          <div class="statistic-value">{{ stats.productCount }}</div>
          <div class="statistic-label">商品总数</div>
          <div class="card-actions">
            <el-button type="primary" @click="$router.push('/admin/products')">
              管理商品
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>交易数据</h3>
            </div>
          </template>
          <div class="statistic-value">{{ stats.orderCount }}</div>
          <div class="statistic-label">订单总数</div>
          <div class="card-actions">
            <el-button type="primary" disabled>
              查看订单
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>最近活动</h3>
            </div>
          </template>
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="6" animated />
          </div>
          <div v-else>
            <el-empty v-if="recentActivities.length === 0" description="暂无活动记录" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="activity in recentActivities"
                :key="activity.id"
                :timestamp="formatDate(activity.time)"
                :type="getActivityType(activity.type)"
              >
                {{ activity.content }}
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const stats = reactive({
  userCount: 0,
  productCount: 0,
  orderCount: 0
})
const recentActivities = ref([])

// 加载统计数据
const loadStats = async () => {
  try {
    // 分别加载各项统计数据，避免一个失败导致全部失败
    try {
      console.log('开始加载用户统计数据...')
      const userResponse = await api.admin.getUsers({ page: 0, size: 1 })
      console.log('用户统计数据响应:', userResponse)
      stats.userCount = userResponse.data.totalElements || 0
      console.log('用户总数:', stats.userCount)
    } catch (error) {
      console.error('加载用户统计失败', error)
    }
    
    try {
      console.log('开始加载商品统计数据...')
      const productResponse = await api.admin.getProducts({ page: 0, size: 1 })
      console.log('商品统计数据响应:', productResponse)
      stats.productCount = productResponse.data.totalElements || 0
      console.log('商品总数:', stats.productCount)
    } catch (error) {
      console.error('加载商品统计失败', error)
    }
    
    try {
      console.log('开始加载订单统计数据...')
      const orderResponse = await api.order.getList({ page: 0, size: 1 })
      console.log('订单统计数据响应:', orderResponse)
      stats.orderCount = orderResponse.data.totalElements || 0
      console.log('订单总数:', stats.orderCount)
    } catch (error) {
      console.error('加载订单统计失败', error)
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error(error)
  }
}

// 加载最近活动
const loadRecentActivities = async () => {
  loading.value = true
  try {
    // 这里假设有一个获取最近活动的API
    // 由于目前可能没有这个API，我们模拟一些数据
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟延迟
    
    recentActivities.value = [
      {
        id: 1,
        type: 'user',
        content: '新用户 student2023 注册',
        time: new Date(Date.now() - 1000 * 60 * 30) // 30分钟前
      },
      {
        id: 2,
        type: 'product',
        content: '用户 tech_lover 发布新商品 "全新iPad Pro 2024"',
        time: new Date(Date.now() - 1000 * 60 * 120) // 2小时前
      },
      {
        id: 3,
        type: 'order',
        content: '订单 #2024060501 已完成交易',
        time: new Date(Date.now() - 1000 * 60 * 180) // 3小时前
      },
      {
        id: 4,
        type: 'product',
        content: '管理员删除违规商品 "考试作弊神器"',
        time: new Date(Date.now() - 1000 * 60 * 240) // 4小时前
      },
      {
        id: 5,
        type: 'user',
        content: '管理员禁用账号 black_market',
        time: new Date(Date.now() - 1000 * 60 * 60 * 24) // 1天前
      }
    ]
  } catch (error) {
    ElMessage.error('加载最近活动失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  try {
    const dateObj = new Date(date)
    if (isNaN(dateObj.getTime())) {
      return '' // 日期无效时返回空字符串
    }
    return dateObj.toLocaleString()
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '' // 出错时返回空字符串
  }
}

// 获取活动类型对应的样式
const getActivityType = (type) => {
  const typeMap = {
    user: 'primary',
    product: 'success',
    order: 'warning',
    system: 'info'
  }
  return typeMap[type] || 'info'
}

onMounted(() => {
  loadStats()
  loadRecentActivities()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px 0;
}

.dashboard-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.statistic-value {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
  margin: 10px 0;
}

.statistic-label {
  text-align: center;
  color: #606266;
  font-size: 14px;
  margin-bottom: 20px;
}

.card-actions {
  margin-top: auto;
  text-align: center;
}

.loading-container {
  padding: 20px 0;
}

.mt-20 {
  margin-top: 20px;
}
</style> 