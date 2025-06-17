<template>
  <div class="products-container">
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索商品名称"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-select v-model="searchForm.sort" placeholder="排序方式" class="sort-select">
            <el-option label="最新发布" value="createTime,desc" />
            <el-option label="价格从低到高" value="price,asc" />
            <el-option label="价格从高到低" value="price,desc" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div class="products-list" v-loading="loading">
      <el-empty v-if="products.length === 0" description="暂无商品" />
      
      <el-row :gutter="24">
        <el-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6" class="mb-24">
          <div class="product-wrapper" @click="viewProduct(product.id)">
            <el-card class="product-card" shadow="hover">
              <div class="product-status">
                <el-tag size="small" effect="dark" type="success">可购买</el-tag>
              </div>
              
              <div class="product-img">
                <el-image 
                  :src="product.imagePaths && product.imagePaths.length > 0 ? product.imagePaths[0] : '/placeholder.png'"
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
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[8, 12, 24, 36]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Picture } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const searchForm = reactive({
  keyword: '',
  sort: 'createTime,desc'
})

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await api.product.getList({
      keyword: searchForm.keyword,
      status: 'AVAILABLE',
      sort: searchForm.sort,
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
  searchForm.keyword = ''
  searchForm.sort = 'createTime,desc'
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

// 查看商品详情
const viewProduct = (id) => {
  router.push(`/product/${id}`)
}

// 截断文本
const truncateText = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

onMounted(() => {
  fetchProducts()
})

// 监听路由参数变化
watch(
  () => router.currentRoute.value.query,
  (query) => {
    if (query.keyword) {
      searchForm.keyword = query.keyword
    }
    fetchProducts()
  },
  { immediate: true }
)
</script>

<style scoped>
.products-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 30px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 10px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.search-input {
  width: 300px;
}

.sort-select {
  width: 180px;
}

.products-list {
  margin-top: 30px;
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

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
  margin-top: auto;
}

.product-date {
  display: flex;
  align-items: center;
}

.product-seller {
  display: flex;
  align-items: center;
  gap: 5px;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mb-24 {
  margin-bottom: 24px;
}

.pagination-container {
  margin-top: 50px;
  display: flex;
  justify-content: center;
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

@media (max-width: 768px) {
  .products-container {
    padding: 10px;
  }
  
  .search-input, .sort-select {
    width: 100%;
  }
}
</style> 