<template>
  <div class="users-container">
    <div class="header">
      <h2>用户管理</h2>
      <div class="actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名/学号"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>
    
    <el-table :data="users" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="用户ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="studentId" label="学号" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="contactInfo" label="联系方式" width="150" />
      <el-table-column label="角色" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'info'">
            {{ scope.row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
            {{ scope.row.enabled ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="scope">
          <el-button
            size="small"
            type="primary"
            @click="showUserDetails(scope.row)"
          >
            详情
          </el-button>
          <el-button
            size="small"
            :type="scope.row.enabled ? 'danger' : 'success'"
            @click="toggleUserStatus(scope.row)"
            :disabled="scope.row.role === 'ADMIN'"
          >
            {{ scope.row.enabled ? '禁用' : '启用' }}
          </el-button>
          <el-button
            size="small"
            type="warning"
            @click="resetUserPassword(scope.row)"
            :disabled="scope.row.role === 'ADMIN'"
          >
            重置密码
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
    
    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="userDetailVisible"
      title="用户详情"
      width="50%"
    >
      <div v-if="selectedUser" class="user-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ selectedUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ selectedUser.studentId }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ selectedUser.nickname || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ selectedUser.contactInfo || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="selectedUser.role === 'ADMIN' ? 'danger' : 'info'">
              {{ selectedUser.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedUser.enabled ? 'success' : 'danger'">
              {{ selectedUser.enabled ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDate(selectedUser.createTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="user-stats">
          <h3>用户数据统计</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value">{{ userStats.publishedCount }}</div>
                <div class="stat-label">发布商品数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value">{{ userStats.soldCount }}</div>
                <div class="stat-label">售出商品数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value">{{ userStats.boughtCount }}</div>
                <div class="stat-label">购买商品数</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '../../api'

const loading = ref(false)
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

// 用户详情相关
const userDetailVisible = ref(false)
const selectedUser = ref(null)
const userStats = reactive({
  publishedCount: 0,
  soldCount: 0,
  boughtCount: 0
})

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await api.admin.getUsers({
      keyword: searchKeyword.value,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    users.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索用户
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  fetchUsers()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUsers()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchUsers()
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

// 查看用户详情
const showUserDetails = async (user) => {
  selectedUser.value = user
  userDetailVisible.value = true
  
  // 获取用户统计数据
  try {
    // 假设管理员可以查看特定用户的统计数据
    // 这里可能需要后端提供专门的API
    const [publishedResponse, soldResponse, boughtResponse] = await Promise.all([
      api.product.getList({ seller: user.studentId, page: 0, size: 1 }),
      api.order.getList({ seller: user.studentId, page: 0, size: 1 }),
      api.order.getList({ buyer: user.studentId, page: 0, size: 1 })
    ])
    
    userStats.publishedCount = publishedResponse.data.totalElements || 0
    userStats.soldCount = soldResponse.data.totalElements || 0
    userStats.boughtCount = boughtResponse.data.totalElements || 0
  } catch (error) {
    console.error('获取用户统计数据失败', error)
    userStats.publishedCount = 0
    userStats.soldCount = 0
    userStats.boughtCount = 0
  }
}

// 切换用户状态（启用/禁用）
const toggleUserStatus = (user) => {
  if (user.role === 'ADMIN') {
    ElMessage.warning('不能操作管理员账号')
    return
  }
  
  const action = user.enabled ? '禁用' : '启用'
  
  ElMessageBox.confirm(
    `确定要${action}用户 ${user.username} 吗？`,
    `${action}用户`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: user.enabled ? 'warning' : 'info'
    }
  )
    .then(async () => {
      try {
        await api.admin.updateUserStatus(user.id, !user.enabled)
        ElMessage.success(`${action}用户成功`)
        
        // 更新本地状态
        const index = users.value.findIndex(u => u.id === user.id)
        if (index !== -1) {
          users.value[index].enabled = !user.enabled
        }
      } catch (error) {
        ElMessage.error(`${action}用户失败`)
        console.error(error)
      }
    })
    .catch(() => {})
}

// 重置用户密码
const resetUserPassword = (user) => {
  if (user.role === 'ADMIN') {
    ElMessage.warning('不能操作管理员账号')
    return
  }
  
  ElMessageBox.confirm(
    `确定要重置用户 ${user.username} 的密码吗？`,
    '重置密码',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const response = await api.admin.resetPassword(user.id)
        ElMessage.success('密码重置成功：' + response.data.password)
      } catch (error) {
        ElMessage.error('密码重置失败')
        console.error(error)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.user-detail {
  margin-bottom: 20px;
}

.user-stats {
  margin-top: 30px;
}

.user-stats h3 {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
}

.stat-card {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  height: 100%;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}
</style> 