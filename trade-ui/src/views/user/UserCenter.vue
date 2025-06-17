<template>
  <div class="user-center-container">
    <el-card shadow="never" class="user-info-card">
      <template #header>
        <div class="card-header">
          <h2>个人资料</h2>
          <div class="user-status">
            <el-tag type="success">{{ roleMap[userStore.user?.role] || '用户' }}</el-tag>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="user-info-content">
        <el-form 
          ref="formRef" 
          :model="userForm" 
          :rules="rules" 
          label-position="top"
        >
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="学号" prop="studentId">
                <el-input v-model="userForm.studentId" disabled />
              </el-form-item>
            </el-col>
            
            <el-col :xs="24" :sm="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="userForm.username" disabled />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="userForm.nickname" />
              </el-form-item>
            </el-col>
            
            <el-col :xs="24" :sm="12">
              <el-form-item label="联系方式" prop="contactInfo">
                <el-input v-model="userForm.contactInfo" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item>
            <el-button type="primary" @click="handleUpdateProfile" :loading="updating">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <el-card shadow="never" class="password-card">
      <template #header>
        <div class="card-header">
          <h2>修改密码</h2>
        </div>
      </template>
      
      <div class="password-content">
        <el-form 
          ref="passwordFormRef" 
          :model="passwordForm" 
          :rules="passwordRules" 
          label-position="top"
        >
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input 
              v-model="passwordForm.currentPassword" 
              type="password" 
              show-password 
            />
          </el-form-item>
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input 
              v-model="passwordForm.newPassword" 
              type="password" 
              show-password 
            />
          </el-form-item>
          
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              show-password 
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <el-card shadow="never" class="stats-card">
      <template #header>
        <div class="card-header">
          <h2>交易数据</h2>
        </div>
      </template>
      
      <div class="stats-content">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="8">
            <div class="stat-item">
              <div class="stat-title">已发布商品</div>
              <div class="stat-value">{{ stats.publishedCount }}</div>
              <el-button @click="$router.push('/user/products')" text>查看详情</el-button>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="8">
            <div class="stat-item">
              <div class="stat-title">已购买商品</div>
              <div class="stat-value">{{ stats.boughtCount }}</div>
              <el-button @click="$router.push('/user/orders')" text>查看详情</el-button>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="8">
            <div class="stat-item">
              <div class="stat-title">已出售商品</div>
              <div class="stat-value">{{ stats.soldCount }}</div>
              <el-button @click="$router.push('/user/orders')" text>查看详情</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import api from '../../api'

const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const loading = ref(false)
const updating = ref(false)
const changingPassword = ref(false)

// 角色映射
const roleMap = {
  'ADMIN': '管理员',
  'USER': '普通用户'
}

// 用户基本信息表单
const userForm = reactive({
  studentId: '',
  username: '',
  nickname: '',
  contactInfo: ''
})

// 修改密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 用户交易数据
const stats = reactive({
  publishedCount: 0,
  boughtCount: 0,
  soldCount: 0
})

// 表单验证规则
const rules = {
  nickname: [
    { max: 20, message: '昵称长度不能超过20个字符', trigger: 'blur' }
  ],
  contactInfo: [
    { max: 50, message: '联系方式长度不能超过50个字符', trigger: 'blur' }
  ]
}

// 密码表单验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 获取用户资料
const fetchUserProfile = async () => {
  loading.value = true
  try {
    const response = await api.user.getProfile()
    const userData = response.data
    
    // 更新表单
    userForm.studentId = userData.studentId
    userForm.username = userData.username
    userForm.nickname = userData.nickname || ''
    userForm.contactInfo = userData.contactInfo || ''
    
    // 更新用户状态
    userStore.setUser(userData)
  } catch (error) {
    ElMessage.error('获取用户资料失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取用户交易数据
const fetchUserStats = async () => {
  try {
    // 获取已发布商品数量
    const publishedResponse = await api.product.getList({
      seller: 'self',
      page: 0,
      size: 1
    })
    stats.publishedCount = publishedResponse.data.totalElements
    
    // 获取已购买商品数量
    const boughtResponse = await api.order.getList({
      type: 'buy',
      page: 0,
      size: 1
    })
    stats.boughtCount = boughtResponse.data.totalElements
    
    // 获取已出售商品数量
    const soldResponse = await api.order.getList({
      type: 'sell',
      page: 0,
      size: 1
    })
    stats.soldCount = soldResponse.data.totalElements
  } catch (error) {
    console.error(error)
  }
}

// 更新用户资料
const handleUpdateProfile = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    updating.value = true
    try {
      await api.user.updateProfile({
        nickname: userForm.nickname,
        contactInfo: userForm.contactInfo
      })
      
      ElMessage.success('个人资料更新成功')
      
      // 更新状态
      userStore.user.nickname = userForm.nickname
      userStore.user.contactInfo = userForm.contactInfo
    } catch (error) {
      ElMessage.error('更新个人资料失败')
    } finally {
      updating.value = false
    }
  })
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    changingPassword.value = true
    try {
      await api.user.changePassword({
        oldPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword
      })
      
      ElMessage.success('密码修改成功')
      
      // 清空表单
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } catch (error) {
      ElMessage.error(error.message || '密码修改失败')
    } finally {
      changingPassword.value = false
    }
  })
}

onMounted(() => {
  fetchUserProfile()
  fetchUserStats()
})
</script>

<style scoped>
.user-center-container {
  padding: 20px 0;
}

.user-info-card,
.password-card,
.stats-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
}

.user-info-content,
.password-content,
.stats-content {
  padding: 10px 0;
}

.stat-item {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s;
  height: 100%;
  margin-bottom: 20px;
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}
</style> 