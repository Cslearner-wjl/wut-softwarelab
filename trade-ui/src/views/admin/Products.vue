<template>
  <div class="products-container">
    <div class="header">
      <h2>商品管理</h2>
      <div class="actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品标题/描述"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态过滤" clearable>
          <el-option label="全部" value="" />
          <el-option label="在售" value="AVAILABLE" />
          <el-option label="已售" value="SOLD" />
          <el-option label="已下架" value="REMOVED" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>
    
    <el-table :data="products" style="width: 100%" v-loading="loading">
      <el-table-column label="商品信息" min-width="300">
        <template #default="scope">
          <div class="product-info">
            <el-image 
              :src="getProductImage(scope.row)" 
              fit="cover"
              class="product-image"
              @click="previewImage(scope.row)"
            />
            <div class="product-detail">
              <div class="product-title">{{ scope.row.title }}</div>
              <div class="product-price">¥{{ scope.row.price }}</div>
              <div class="product-description">{{ truncateText(scope.row.description, 50) }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="卖家" width="120">
        <template #default="scope">
          <div>{{ scope.row.seller.username }}</div>
          <div class="seller-id">{{ scope.row.seller.studentId }}</div>
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
      
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="scope">
          <el-button
            size="small"
            type="primary"
            @click="viewProduct(scope.row.id)"
          >
            查看
          </el-button>
          
          <el-button
            v-if="scope.row.status === 'AVAILABLE'"
            size="small"
            type="danger"
            @click="handleRemoveProduct(scope.row)"
          >
            下架
          </el-button>
          
          <el-button
            v-if="scope.row.status === 'REMOVED'"
            size="small"
            type="success"
            @click="handleRestoreProduct(scope.row)"
          >
            恢复
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="商品图片预览" width="80%">
      <div v-if="selectedProduct" class="image-preview">
        <el-carousel height="500px" v-if="selectedProduct.imagePaths && selectedProduct.imagePaths.length > 0">
          <el-carousel-item v-for="(image, index) in selectedProduct.imagePaths" :key="index">
            <el-image :src="image" fit="contain" style="width: 100%; height: 100%;" />
          </el-carousel-item>
        </el-carousel>
        <el-empty v-else description="暂无图片" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '../../api'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const statusFilter = ref('')

// 图片预览相关
const previewVisible = ref(false)
const selectedProduct = ref(null)

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await api.admin.getProducts({
      keyword: searchKeyword.value,
      status: statusFilter.value,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    products.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取商品列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索商品
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  currentPage.value = 1
  fetchProducts()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchProducts()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchProducts()
}

// 预览图片
const previewImage = (product) => {
  selectedProduct.value = product
  previewVisible.value = true
}

// 查看商品详情
const viewProduct = (id) => {
  router.push(`/product/${id}`)
}

// 下架商品
const handleRemoveProduct = (product) => {
  ElMessageBox.confirm(
    `确定要下架商品 "${product.title}" 吗？`,
    '下架商品',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await api.admin.removeProduct(product.id)
        ElMessage.success('商品已下架')
        
        // 更新本地商品状态
        const index = products.value.findIndex(p => p.id === product.id)
        if (index !== -1) {
          products.value[index].status = 'REMOVED'
        }
      } catch (error) {
        ElMessage.error('下架商品失败')
        console.error(error)
      }
    })
    .catch(() => {})
}

// 恢复商品
const handleRestoreProduct = (product) => {
  ElMessageBox.confirm(
    `确定要恢复商品 "${product.title}" 吗？`,
    '恢复商品',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    }
  )
    .then(async () => {
      try {
        await api.product.update(product.id, { status: 'AVAILABLE' })
        ElMessage.success('商品已恢复')
        
        // 更新本地商品状态
        const index = products.value.findIndex(p => p.id === product.id)
        if (index !== -1) {
          products.value[index].status = 'AVAILABLE'
        }
      } catch (error) {
        ElMessage.error('恢复商品失败')
        console.error(error)
      }
    })
    .catch(() => {})
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

// 截断文本
const truncateText = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 监听状态过滤器变化
watch(statusFilter, () => {
  currentPage.value = 1
  fetchProducts()
})

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.products-container {
  padding: 20px 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 10px;
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
  cursor: pointer;
}

.product-detail {
  display: flex;
  flex-direction: column;
}

.product-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.product-price {
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 5px;
}

.product-description {
  font-size: 12px;
  color: #909399;
}

.seller-id {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.image-preview {
  width: 100%;
  height: 100%;
}
</style> 