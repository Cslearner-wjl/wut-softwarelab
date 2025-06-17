<template>
  <div class="orders-container">
    <el-tabs v-model="activeTab" class="demo-tabs">
      <el-tab-pane label="我购买的" name="buy">
        <template v-if="buyOrders.length === 0">
          <el-empty description="暂无购买记录" />
        </template>
        
        <el-table v-else :data="buyOrders" style="width: 100%" v-loading="loadingBuy">
          <el-table-column label="商品信息" min-width="300">
            <template #default="scope">
              <div class="product-info">
                <el-image 
                  :src="getProductImage(scope.row.product)" 
                  fit="cover"
                  class="product-image"
                />
                <div class="product-detail">
                  <div class="product-title">{{ scope.row.product.title }}</div>
                  <div class="product-price">¥{{ scope.row.product.price }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="seller.username" label="卖家" width="120" />
          
          <el-table-column label="订单状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button 
                size="small" 
                @click="viewProduct(scope.row.product.id)"
              >
                查看商品
              </el-button>
              
              <el-button 
                v-if="scope.row.status === 'PENDING'" 
                size="small" 
                type="danger" 
                @click="handleCancelOrder(scope.row.id)"
              >
                取消订单
              </el-button>
              
              <el-button 
                v-if="scope.row.status === 'PENDING'" 
                size="small" 
                type="success" 
                @click="handleCompleteOrder(scope.row.id)"
              >
                完成交易
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="我出售的" name="sell">
        <template v-if="sellOrders.length === 0">
          <el-empty description="暂无出售记录" />
        </template>
        
        <el-table v-else :data="sellOrders" style="width: 100%" v-loading="loadingSell">
          <el-table-column label="商品信息" min-width="300">
            <template #default="scope">
              <div class="product-info">
                <el-image 
                  :src="getProductImage(scope.row.product)" 
                  fit="cover"
                  class="product-image"
                />
                <div class="product-detail">
                  <div class="product-title">{{ scope.row.product.title }}</div>
                  <div class="product-price">¥{{ scope.row.product.price }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="buyer.username" label="买家" width="120" />
          
          <el-table-column label="订单状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button 
                size="small" 
                @click="viewProduct(scope.row.product.id)"
              >
                查看商品
              </el-button>
              
              <el-button 
                v-if="scope.row.status === 'PENDING'" 
                size="small" 
                type="danger" 
                @click="handleCancelOrder(scope.row.id)"
              >
                取消订单
              </el-button>
              
              <el-button 
                v-if="scope.row.status === 'PENDING'" 
                size="small" 
                type="success" 
                @click="handleCompleteOrder(scope.row.id)"
              >
                完成交易
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    
    <div class="pagination-container" v-if="activeTab === 'buy' && buyOrders.length > 0">
      <el-pagination
        :current-page="buyPagination.currentPage"
        :page-size="buyPagination.pageSize"
        :total="buyPagination.total"
        layout="total, prev, pager, next, jumper"
        @current-change="handleBuyPageChange"
      />
    </div>
    
    <div class="pagination-container" v-if="activeTab === 'sell' && sellOrders.length > 0">
      <el-pagination
        :current-page="sellPagination.currentPage"
        :page-size="sellPagination.pageSize"
        :total="sellPagination.total"
        layout="total, prev, pager, next, jumper"
        @current-change="handleSellPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const router = useRouter()
const activeTab = ref('buy')
const buyOrders = ref([])
const sellOrders = ref([])
const loadingBuy = ref(false)
const loadingSell = ref(false)

const buyPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const sellPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 获取购买订单
const fetchBuyOrders = async () => {
  loadingBuy.value = true
  try {
    const response = await api.order.getList({
      type: 'buy',
      page: buyPagination.currentPage - 1,
      size: buyPagination.pageSize
    })
    buyOrders.value = response.data.content
    buyPagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取购买订单失败')
    console.error(error)
  } finally {
    loadingBuy.value = false
  }
}

// 获取出售订单
const fetchSellOrders = async () => {
  loadingSell.value = true
  try {
    const response = await api.order.getList({
      type: 'sell',
      page: sellPagination.currentPage - 1,
      size: sellPagination.pageSize
    })
    sellOrders.value = response.data.content
    sellPagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取出售订单失败')
    console.error(error)
  } finally {
    loadingSell.value = false
  }
}

// 处理取消订单
const handleCancelOrder = (orderId) => {
  ElMessageBox.confirm(
    '确定要取消这个订单吗？',
    '取消订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        console.log('开始取消订单:', orderId)
        const response = await api.order.updateStatus(orderId, 'CANCELLED')
        console.log('取消订单成功:', response)
        ElMessage.success('订单已取消')
        
        // 刷新订单列表
        if (activeTab.value === 'buy') {
          fetchBuyOrders()
        } else {
          fetchSellOrders()
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        ElMessage.error('取消订单失败: ' + (error.message || '未知错误'))
      }
    })
    .catch(() => {
      console.log('用户取消了操作')
    })
}

// 处理完成交易
const handleCompleteOrder = (orderId) => {
  ElMessageBox.confirm(
    '确认已完成当面交易？',
    '完成交易',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    }
  )
    .then(async () => {
      try {
        console.log('开始完成交易:', orderId)
        const response = await api.order.updateStatus(orderId, 'COMPLETED')
        console.log('完成交易成功:', response)
        ElMessage.success('交易已完成')
        
        // 刷新订单列表
        if (activeTab.value === 'buy') {
          fetchBuyOrders()
        } else {
          fetchSellOrders()
        }
      } catch (error) {
        console.error('完成交易失败:', error)
        ElMessage.error('完成交易失败: ' + (error.message || '未知错误'))
      }
    })
    .catch(() => {
      console.log('用户取消了操作')
    })
}

// 查看商品详情
const viewProduct = (productId) => {
  router.push(`/product/${productId}`)
}

// 获取商品图片
const getProductImage = (product) => {
  if (product && product.imagePaths && product.imagePaths.length > 0) {
    return product.imagePaths[0]
  }
  return '/placeholder.png'
}

// 获取订单状态标签
const getStatusLabel = (status) => {
  const map = {
    'PENDING': '待交易',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

// 获取订单状态标签类型
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return map[status] || ''
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    // 处理后端返回的ISO格式日期字符串
    const date = new Date(dateString)
    if (isNaN(date.getTime())) {
      return '' // 如果解析失败，返回空字符串
    }
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit', 
      minute: '2-digit'
    })
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '' // 出错时返回空字符串
  }
}

// 处理购买订单分页
const handleBuyPageChange = (page) => {
  buyPagination.currentPage = page
  fetchBuyOrders()
}

// 处理出售订单分页
const handleSellPageChange = (page) => {
  sellPagination.currentPage = page
  fetchSellOrders()
}

// 监听标签页变化
watch(activeTab, (newValue) => {
  if (newValue === 'buy' && buyOrders.value.length === 0) {
    fetchBuyOrders()
  } else if (newValue === 'sell' && sellOrders.value.length === 0) {
    fetchSellOrders()
  }
})

onMounted(() => {
  fetchBuyOrders()
})
</script>

<style scoped>
.orders-container {
  padding: 20px 0;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 15px;
  border-radius: 4px;
}

.product-detail {
  display: flex;
  flex-direction: column;
}

.product-title {
  font-weight: 500;
  margin-bottom: 8px;
}

.product-price {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 