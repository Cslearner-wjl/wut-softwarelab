<template>
  <div class="user-products-container">
    <div class="header">
      <h2>我的商品</h2>
      <el-button type="primary" @click="$router.push('/product/publish')">发布新商品</el-button>
    </div>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane label="在售商品" name="AVAILABLE">
        <product-list
          :products="filteredProducts"
          :loading="loading"
          @edit="handleEdit"
          @remove="handleRemove"
          @view="handleView"
        />
      </el-tab-pane>
      
      <el-tab-pane label="已售商品" name="SOLD">
        <product-list
          :products="filteredProducts"
          :loading="loading"
          @view="handleView"
        />
      </el-tab-pane>
    </el-tabs>
    
    <div class="pagination-container" v-if="filteredProducts.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import ProductList from '../../components/ProductList.vue'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const activeTab = ref('AVAILABLE')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 根据状态过滤商品
const filteredProducts = computed(() => {
  return products.value
})

// 获取用户发布的商品
const fetchUserProducts = async () => {
  loading.value = true
  try {
    const response = await api.product.getUserProducts({
      status: activeTab.value,
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

// 查看商品详情
const handleView = (id) => {
  router.push(`/product/${id}`)
}

// 编辑商品
const handleEdit = (id) => {
  router.push(`/product/edit/${id}`)
}

// 删除商品
const handleRemove = (id) => {
  ElMessageBox.confirm(
    '确定要永久删除该商品吗？此操作无法恢复。',
    '删除商品',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await api.product.delete(id)
        ElMessage.success('商品已删除')
        fetchUserProducts()
      } catch (error) {
        ElMessage.error(error.message || '删除商品失败')
      }
    })
    .catch(() => {})
}

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page
  fetchUserProducts()
}

// 监听标签页变化
watch(activeTab, () => {
  currentPage.value = 1
  fetchUserProducts()
})

onMounted(() => {
  fetchUserProducts()
})
</script>

<style scoped>
.user-products-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 