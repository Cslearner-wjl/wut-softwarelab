<template>
  <div class="product-list">
    <el-empty v-if="products.length === 0" description="暂无商品" />
    
    <el-table v-else :data="products" style="width: 100%" v-loading="loading">
      <el-table-column label="商品信息" min-width="300">
        <template #default="scope">
          <div class="product-info">
            <el-image 
              :src="getProductImage(scope.row)" 
              fit="cover"
              class="product-image"
            />
            <div class="product-detail">
              <div class="product-title">{{ scope.row.title }}</div>
              <div class="product-price">¥{{ scope.row.price }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="createTime" label="发布时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <div class="table-actions">
            <el-button 
              size="small" 
              @click="handleView(scope.row.id)"
            >
              查看
            </el-button>
            
            <el-button 
              v-if="scope.row.status === 'AVAILABLE'" 
              size="small" 
              type="primary" 
              @click="handleEdit(scope.row.id)"
            >
              编辑
            </el-button>
            
            <el-button 
              v-if="scope.row.status === 'AVAILABLE'" 
              size="small" 
              type="danger" 
              @click="handleRemove(scope.row.id)"
            >
              下架
            </el-button>
            
            <el-button 
              v-if="scope.row.status === 'REMOVED'" 
              size="small" 
              type="success" 
              @click="handleRestore(scope.row.id)"
            >
              上架
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  products: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['edit', 'remove', 'restore', 'view'])

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

// 获取商品图片
const getProductImage = (product) => {
  if (product && product.imagePaths && product.imagePaths.length > 0) {
    return product.imagePaths[0]
  }
  return '/placeholder.png'
}

// 获取状态标签类型
const getStatusType = (status) => {
  const map = {
    'AVAILABLE': 'success',
    'SOLD': 'info',
    'REMOVED': 'danger'
  }
  return map[status] || ''
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  const map = {
    'AVAILABLE': '在售',
    'SOLD': '已售',
    'REMOVED': '已下架'
  }
  return map[status] || status
}

// 事件处理
const handleEdit = (id) => emit('edit', id)
const handleRemove = (id) => emit('remove', id)
const handleRestore = (id) => emit('restore', id)
const handleView = (id) => emit('view', id)
</script>

<style scoped>
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

.table-actions {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}
</style> 