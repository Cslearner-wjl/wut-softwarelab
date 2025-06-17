<template>
  <div class="login-container">
    <h2>用户登录</h2>
    
    <el-form ref="formRef" :model="loginForm" :rules="rules" label-position="top" @submit.prevent="handleLogin">
      <el-form-item label="学号" prop="studentId">
        <el-input v-model="loginForm.studentId" placeholder="请输入学号" />
      </el-form-item>
      
      <el-form-item label="密码" prop="password">
        <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">登录</el-button>
      </el-form-item>
    </el-form>
    
    <div class="login-links">
      <router-link to="/auth/register">没有账号？立即注册</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)

// 登录表单
const loginForm = reactive({
  studentId: '',
  password: ''
})

// 表单验证规则
const rules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    await userStore.login(loginForm)
    
    ElMessage.success('登录成功')
    
    // 如果有重定向地址，则跳转到重定向地址，否则跳转到首页
    const redirectPath = route.query.redirect || '/'
    router.push(redirectPath)
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请检查学号和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  font-weight: 500;
  color: #303133;
}

.login-links {
  margin-top: 20px;
  text-align: center;
}

.login-links a {
  color: #409EFF;
  text-decoration: none;
}

.login-links a:hover {
  text-decoration: underline;
}
</style> 