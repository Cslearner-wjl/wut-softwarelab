<template>
  <div class="product-detail-container" v-loading="loading">
    <el-empty v-if="!product" description="商品不存在或已下架" />
    
    <div v-else class="product-detail">
      <el-row :gutter="30">
        <!-- 商品图片 -->
        <el-col :xs="24" :sm="24" :md="12">
          <div class="product-gallery">
            <el-carousel v-if="product.imagePaths && product.imagePaths.length > 0" height="400px">
              <el-carousel-item v-for="(image, index) in product.imagePaths" :key="index">
                <el-image :src="image" fit="contain">
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      <div>图片加载失败</div>
                    </div>
                  </template>
                </el-image>
              </el-carousel-item>
            </el-carousel>
            
            <el-image 
              v-else 
              src="/placeholder.png" 
              fit="contain"
              style="width: 100%; height: 400px;"
            />
          </div>
        </el-col>
        
        <!-- 商品信息 -->
        <el-col :xs="24" :sm="24" :md="12">
          <div class="product-info">
            <h1 class="product-title">{{ product.title }}</h1>
            
            <div class="product-price">
              <span class="price-label">价格：</span>
              <span class="price-value">¥{{ product.price }}</span>
            </div>
            
            <div class="product-meta">
              <div class="meta-item">
                <el-icon><User /></el-icon>
                <span>卖家：{{ product.seller?.nickname || product.seller?.username || '未知用户' }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>发布时间：{{ formatDate(product.createTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>交易地点：{{ product.tradeLocation || '待协商' }}</span>
              </div>
            </div>
            
            <div class="product-actions">
              <el-button 
                type="primary" 
                size="large" 
                @click="handleBuy"
                :disabled="!userStore.isLoggedIn || isSeller || buyLoading"
                :loading="buyLoading"
              >
                立即购买
              </el-button>
              
              <el-button 
                type="success" 
                size="large" 
                @click="handleInterest"
                :disabled="!userStore.isLoggedIn || isSeller || interestLoading"
                :loading="interestLoading"
              >
                <el-icon><Star /></el-icon> 感兴趣
              </el-button>
            </div>
            
            <el-alert
              v-if="!userStore.isLoggedIn"
              title="请先登录后再进行交易"
              type="warning"
              :closable="false"
              show-icon
            />
            
            <el-alert
              v-else-if="isSeller"
              title="无法购买自己发布的商品"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
        </el-col>
      </el-row>
      
      <!-- 商品描述 -->
      <div class="product-description">
        <el-divider>
          <el-icon><InfoFilled /></el-icon>
          <span>商品描述</span>
        </el-divider>
        
        <div class="description-content">
          {{ product.description || '暂无描述' }}
        </div>
      </div>
    </div>
    
    <!-- 确认购买对话框 -->
    <el-dialog
      v-model="buyDialogVisible"
      title="确认购买"
      width="30%"
      :close-on-click-modal="false"
      :close-on-press-escape="!buyLoading"
    >
      <div class="dialog-content">
        <p>您确定要购买 <strong>{{ product?.title }}</strong> 吗？</p>
        <p>价格：¥{{ product?.price }}</p>
        <p>卖家：{{ product?.seller?.nickname || product?.seller?.username || '未知用户' }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="buyDialogVisible = false" :disabled="buyLoading">取消</el-button>
          <el-button type="primary" @click="confirmBuy" :loading="buyLoading">
            确认购买
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Clock, Location, Star, InfoFilled, Picture } from '@element-plus/icons-vue'
import api from '../api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const product = ref(null)
const buyDialogVisible = ref(false)
const buyLoading = ref(false)
const interestLoading = ref(false)
const productId = computed(() => route.params.id)

// 是否是卖家
const isSeller = computed(() => {
  if (!product.value || !userStore.isLoggedIn || !userStore.user || !product.value.seller) return false
  return product.value.seller.studentId === userStore.user.studentId
})

// 获取商品详情
const fetchProductDetail = async () => {
  if (!productId.value) return
  
  loading.value = true
  try {
    const response = await api.product.getById(productId.value)
    if (response && response.data) {
      product.value = response.data
    } else {
      ElMessage.error('获取商品详情失败：数据格式不正确')
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败：' + (error.message || '未知错误'))
    console.error('获取商品详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    // 将输入的ISO日期字符串解析为Date对象
    const date = new Date(dateString)
    if (isNaN(date.getTime())) {
      return '' // 日期无效时返回空字符串
    }
    
    // 只返回年月日，不包含时间部分
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    
    return `${year}-${month}-${day}`
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '' // 出错时返回空字符串
  }
}

// 处理购买
const handleBuy = () => {
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm(
      '您需要登录后才能购买商品',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
      .then(() => {
        router.push({
          path: '/auth/login',
          query: { redirect: route.fullPath }
        })
      })
      .catch(() => {})
    return
  }
  
  if (isSeller.value) {
    ElMessage.warning('您不能购买自己发布的商品')
    return
  }
  
  buyDialogVisible.value = true
}

// 确认购买
const confirmBuy = async () => {
  if (!product.value || !product.value.id) {
    ElMessage.error('商品信息不完整，无法购买')
    return
  }
  
  buyLoading.value = true
  try {
    const response = await api.order.create({
      productId: product.value.id
    })
    
    buyDialogVisible.value = false
    ElMessage.success('购买成功，请前往订单页面查看')
    
    // 延迟一下再跳转，让用户看到成功消息
    setTimeout(() => {
      // 跳转到订单页面
      router.push('/user/orders')
    }, 1500)
  } catch (error) {
    console.error('购买失败:', error)
    ElMessage.error(error.message || '购买失败，请稍后重试')
  } finally {
    buyLoading.value = false
  }
}

// 标记感兴趣
const handleInterest = async () => {
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm(
      '您需要登录后才能标记感兴趣',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
      .then(() => {
        router.push({
          path: '/auth/login',
          query: { redirect: route.fullPath }
        })
      })
      .catch(() => {})
    return
  }
  
  if (isSeller.value) {
    ElMessage.warning('您不能对自己发布的商品标记感兴趣')
    return
  }
  
  if (!product.value || !product.value.id) {
    ElMessage.error('商品信息不完整，无法标记感兴趣')
    return
  }
  
  interestLoading.value = true
  try {
    await api.product.markInterest(product.value.id)
    ElMessage.success('已成功标记感兴趣，卖家会收到通知')
  } catch (error) {
    console.error('标记感兴趣失败:', error)
    ElMessage.error(error.message || '标记感兴趣失败，请稍后重试')
  } finally {
    interestLoading.value = false
  }
}

// 监听路由参数变化重新加载数据
const reloadOnRouteChange = async () => {
  // 重置状态
  product.value = null
  
  // 延迟一下再获取数据，确保DOM已更新
  await nextTick()
  await fetchProductDetail()
}

onMounted(async () => {
  await fetchProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  padding: 20px 0;
}

.product-detail {
  margin-top: 20px;
}

.product-gallery {
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.image-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 400px;
  color: #909399;
  background-color: #f5f7fa;
}

.image-error .el-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.product-info {
  padding: 0 20px;
}

.product-title {
  font-size: 24px;
  margin: 0 0 20px;
  color: #303133;
  word-break: break-all;
}

.product-price {
  margin-bottom: 20px;
}

.price-label {
  font-size: 16px;
  color: #606266;
}

.price-value {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
}

.product-meta {
  margin-bottom: 30px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  color: #606266;
}

.meta-item .el-icon {
  margin-right: 10px;
}

.product-actions {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.product-description {
  margin-top: 40px;
}

.description-content {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 4px;
  color: #606266;
  line-height: 1.6;
}

.dialog-content {
  font-size: 16px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .product-info {
    padding: 20px 0 0;
  }
  
  .product-title {
    font-size: 20px;
  }
  
  .price-value {
    font-size: 24px;
  }
  
  .product-actions {
    flex-direction: column;
  }
}
</style> 