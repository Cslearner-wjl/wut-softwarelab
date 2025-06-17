<template>
  <div class="register-container">
    <h2>用户注册</h2>
    
    <el-form ref="formRef" :model="registerForm" :rules="rules" label-position="top" @submit.prevent="handleRegister">
      <el-form-item label="学号" prop="studentId">
        <el-input v-model="registerForm.studentId" placeholder="请输入学号" />
      </el-form-item>
      
      <el-form-item label="用户名" prop="username">
        <el-input v-model="registerForm.username" placeholder="请输入用户名" />
      </el-form-item>
      
      <el-form-item label="密码" prop="password">
        <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
      </el-form-item>
      
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
      </el-form-item>
      
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="registerForm.nickname" placeholder="请输入昵称（选填）" />
      </el-form-item>
      
      <el-form-item label="联系方式" prop="contactInfo">
        <el-input v-model="registerForm.contactInfo" placeholder="请输入联系方式（选填）" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleRegister" style="width: 100%">注册</el-button>
      </el-form-item>
    </el-form>
    
    <div class="register-links">
      <router-link to="/auth/login">已有账号？立即登录</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)

// 注册表单
const registerForm = reactive({
  studentId: '',
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  contactInfo: ''
})

// 密码验证函数
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6位'))
  } else {
    if (registerForm.confirmPassword !== '') {
      if (!formRef.value) return
      formRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

// 确认密码验证函数
const validateConfirmPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度必须在2-20之间', trigger: 'blur' }
  ],
  password: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPass, trigger: 'blur' }
  ]
}

// 处理注册
const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 移除确认密码字段，后端不需要
    const { confirmPassword, ...userData } = registerForm
    await userStore.register(userData)
    
    ElMessage.success('注册成功，请登录')
    router.push('/auth/login')
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请检查表单信息')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  width: 100%;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  font-weight: 500;
  color: #303133;
}

.register-links {
  margin-top: 20px;
  text-align: center;
}

.register-links a {
  color: #409EFF;
  text-decoration: none;
}

.register-links a:hover {
  text-decoration: underline;
}
</style> 