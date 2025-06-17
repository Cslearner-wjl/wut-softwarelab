<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="banner">
          <h1>校园二手交易平台</h1>
          <p>让闲置物品流动起来，让校园生活更美好</p>
          <el-button type="primary" size="large" @click="redirectTo('/products')">浏览商品</el-button>
          <el-button v-if="userStore.isLoggedIn" type="success" size="large" @click="redirectTo('/product/publish')">发布商品</el-button>
          <el-button v-else type="success" size="large" @click="redirectTo('/auth/login')">立即登录</el-button>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <h2 class="section-title">最新商品</h2>
      </el-col>
    </el-row>
    
    <el-row :gutter="24" v-loading="loading">
      <el-col v-if="loading || latestProducts.length === 0" :span="24" class="text-center">
        <el-empty v-if="!loading && latestProducts.length === 0" description="暂无商品" />
      </el-col>
      <el-col v-else v-for="product in latestProducts" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6" class="mb-24">
        <div class="product-wrapper" @click="viewProduct(product.id)">
          <el-card class="product-card" shadow="hover">
            <div class="product-status">
              <el-tag size="small" effect="dark" type="success">可购买</el-tag>
            </div>
            <div class="product-img">
              <el-image 
                :src="getImageUrl(product)"
                fit="cover"
                :lazy="true"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <div>加载失败</div>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <div class="product-price">¥{{ product.price }}</div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="24" class="text-center">
        <el-button type="primary" @click="redirectTo('/products')">查看更多商品</el-button>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <h2 class="section-title">平台特色</h2>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <el-col :xs="24" :sm="8" class="mb-20">
        <el-card class="feature-card" shadow="hover">
          <template #header>
            <div class="feature-header">
              <el-icon :size="32"><Goods /></el-icon>
              <h3>便捷交易</h3>
            </div>
          </template>
          <div class="feature-content">
            校内交易，方便快捷，无需担心物流问题，当面交易更放心。
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="8" class="mb-20">
        <el-card class="feature-card" shadow="hover">
          <template #header>
            <div class="feature-header">
              <el-icon :size="32"><User /></el-icon>
              <h3>实名认证</h3>
            </div>
          </template>
          <div class="feature-content">
            学号实名认证，确保交易双方身份真实可靠，提高交易安全性。
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="8" class="mb-20">
        <el-card class="feature-card" shadow="hover">
          <template #header>
            <div class="feature-header">
              <el-icon :size="32"><ChatDotRound /></el-icon>
              <h3>消息通知</h3>
            </div>
          </template>
          <div class="feature-content">
            实时消息通知，及时了解商品动态和交易状态，不错过任何重要信息。
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import api from '../api'
import { processImageUrl } from '../api'
import { ElMessage } from 'element-plus'
import { Goods, ChatDotRound, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const latestProducts = ref([])
const fetchRetries = ref(0)
const maxRetries = 3

// 获取最新商品
const fetchLatestProducts = async () => {
  if (fetchRetries.value >= maxRetries) {
    ElMessage.error('加载商品失败，请刷新页面重试')
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    const response = await api.product.getList({ 
      page: 0, 
      size: 8, 
      sort: 'createTime,desc',
      status: 'AVAILABLE',
      _t: new Date().getTime() // 添加时间戳防止缓存
    })
    
    if (response && response.data && response.data.content) {
      latestProducts.value = response.data.content
      console.log('成功加载最新商品:', latestProducts.value)
    } else {
      console.error('商品数据格式不正确:', response)
      fetchRetries.value++
      setTimeout(fetchLatestProducts, 1000) // 1秒后重试
    }
  } catch (error) {
    console.error('获取最新商品失败:', error)
    fetchRetries.value++
    setTimeout(fetchLatestProducts, 1000) // 1秒后重试
  } finally {
    loading.value = false
  }
}

// 获取图片URL
const getImageUrl = (product) => {
  if (!product || !product.imagePaths || product.imagePaths.length === 0) {
    return '/placeholder.png'
  }
  
  const url = product.imagePaths[0]
  return processImageUrl(url)
}

// 安全跳转
const redirectTo = (path) => {
  router.push(path).catch(err => {
    console.error('路由跳转失败:', err)
    // 如果是重复导航错误，忽略它
    if (err.name !== 'NavigationDuplicated') {
      // 重新加载当前页面
      window.location.href = path
    }
  })
}

// 查看商品详情
const viewProduct = (id) => {
  redirectTo(`/product/${id}`)
}

onMounted(() => {
  fetchLatestProducts()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.banner {
  background-color: #f0f9ff;
  padding: 60px 20px;
  text-align: center;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
}

.banner h1 {
  font-size: 36px;
  margin-bottom: 20px;
  color: #303133;
}

.banner p {
  font-size: 18px;
  margin-bottom: 30px;
  color: #606266;
}

.section-title {
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: 500;
  color: #303133;
  position: relative;
  padding-left: 15px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 5px;
  height: 20px;
  background-color: #409EFF;
  border-radius: 2px;
}

.product-wrapper {
  height: 100%;
  transition: transform 0.3s;
  cursor: pointer;
  margin-bottom: 12px;
}

.product-wrapper:hover {
  transform: translateY(-8px);
}

.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: none;
  aspect-ratio: 0.75;
}

.product-status {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
}

.product-img {
  height: 65%;
  overflow: hidden;
  position: relative;
}

.product-img .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
  object-fit: cover;
}

.product-card:hover .product-img .el-image {
  transform: scale(1.08);
}

.image-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
  background-color: #f5f7fa;
}

.image-error .el-icon {
  font-size: 30px;
  margin-bottom: 10px;
}

.product-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-title {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
  margin-top: auto;
}

.feature-card {
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background-color: #f5f7fa;
}

.feature-header h3 {
  margin: 0;
  font-size: 18px;
}

.feature-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  padding: 15px;
}

.mt-20 {
  margin-top: 20px;
}

.mb-24 {
  margin-bottom: 24px;
}

.text-center {
  text-align: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .home-container {
    padding: 10px;
  }
  
  .banner {
    padding: 30px 15px;
  }
  
  .banner h1 {
    font-size: 28px;
  }
  
  .banner p {
    font-size: 16px;
  }
}
</style> 