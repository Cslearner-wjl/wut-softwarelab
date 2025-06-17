<template>
  <div class="messages-container">
    <div class="messages-header">
      <h2>我的消息</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleMarkAllRead" :disabled="loading || messages.length === 0">
          全部标为已读
        </el-button>
      </div>
    </div>
    
    <div class="messages-content" v-loading="loading">
      <el-empty v-if="messages.length === 0" description="暂无消息" />
      
      <el-timeline v-else>
        <el-timeline-item
          v-for="message in messages"
          :key="message.id"
          :type="message.read ? 'info' : 'primary'"
          :hollow="message.read"
          :timestamp="formatDate(message.createTime)"
        >
          <el-card 
            class="message-card" 
            :class="{ 'message-unread': !message.read }"
            shadow="hover"
            @click="handleReadMessage(message)"
          >
            <div class="message-header">
              <h3 class="message-title">{{ message.title }}</h3>
              <el-tag size="small" :type="getMessageTypeTag(message.type)">
                {{ getMessageTypeLabel(message.type) }}
              </el-tag>
            </div>
            <div class="message-content">{{ message.content }}</div>
            <div class="message-actions">
              <el-button 
                v-if="message.productId" 
                size="small" 
                type="primary" 
                @click.stop="viewProduct(message.productId)"
              >
                查看相关商品
              </el-button>
              <el-button 
                v-if="message.orderId" 
                size="small" 
                type="success" 
                @click.stop="viewOrder(message.orderId)"
              >
                查看相关订单
              </el-button>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      
      <div class="pagination-container" v-if="messages.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api'

const router = useRouter()
const loading = ref(false)
const messages = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取消息列表
const fetchMessages = async () => {
  loading.value = true
  try {
    const response = await api.message.getList({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    messages.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取消息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 标记消息为已读
const handleReadMessage = async (message) => {
  if (message.read) return
  
  try {
    await api.message.markAsRead(message.id)
    
    // 更新本地消息状态
    const index = messages.value.findIndex(m => m.id === message.id)
    if (index !== -1) {
      messages.value[index].read = true
    }
  } catch (error) {
    ElMessage.error('标记已读失败')
    console.error(error)
  }
}

// 全部标为已读
const handleMarkAllRead = async () => {
  try {
    await api.message.markAllAsRead()
    ElMessage.success('已将所有消息标记为已读')
    
    // 更新本地消息状态
    messages.value.forEach(message => {
      message.read = true
    })
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

// 查看相关商品
const viewProduct = (productId) => {
  router.push(`/product/${productId}`)
}

// 查看相关订单
const viewOrder = () => {
  router.push('/user/orders')
}

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page
  fetchMessages()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) {
      return '' // 日期无效时返回空字符串
    }
    return date.toLocaleString()
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '' // 出错时返回空字符串
  }
}

// 获取消息类型标签
const getMessageTypeLabel = (type) => {
  const map = {
    'PRODUCT_INTEREST': '商品兴趣',
    'ORDER_STATUS_CHANGE': '订单状态'
  }
  return map[type] || type
}

// 获取消息类型标签颜色
const getMessageTypeTag = (type) => {
  const map = {
    'PRODUCT_INTEREST': 'success',
    'ORDER_STATUS_CHANGE': 'warning'
  }
  return map[type] || ''
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>
.messages-container {
  padding: 20px 0;
}

.messages-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.messages-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
}

.message-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.message-unread {
  border-left: 3px solid #409EFF;
  background-color: #f0f9ff;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.message-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.message-content {
  color: #606266;
  margin-bottom: 15px;
  white-space: pre-wrap;
  line-height: 1.5;
}

.message-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 