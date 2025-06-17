import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const unreadMessageCount = ref(0)
  const loading = ref(false)
  const error = ref(null)
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  
  // 方法
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const setUser = (newUser) => {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }
  
  const login = async (credentials) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await api.auth.login(credentials)
      if (response.data && response.data.token) {
        setToken(response.data.token)
        setUser(response.data.user)
        console.log('登录成功，用户信息:', response.data.user)
        ElMessage.success('登录成功')
        return response
      } else {
        throw new Error('登录响应格式不正确')
      }
    } catch (err) {
      console.error('登录失败:', err)
      error.value = err.message || '登录失败，请检查账号和密码'
      ElMessage.error(error.value)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  const register = async (userData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await api.auth.register(userData)
      ElMessage.success('注册成功，请登录')
      return response
    } catch (err) {
      console.error('注册失败:', err)
      error.value = err.message || '注册失败，请稍后重试'
      ElMessage.error(error.value)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  const logout = () => {
    token.value = ''
    user.value = null
    unreadMessageCount.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    console.log('已退出登录')
    ElMessage.success('已退出登录')
  }
  
  const updateProfile = async (userData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await api.user.updateProfile(userData)
      setUser(response.data)
      ElMessage.success('个人资料更新成功')
      return response
    } catch (err) {
      console.error('更新个人资料失败:', err)
      error.value = err.message || '更新个人资料失败，请稍后重试'
      ElMessage.error(error.value)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  const changePassword = async (passwordData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await api.user.changePassword(passwordData)
      ElMessage.success('密码修改成功')
      return response
    } catch (err) {
      console.error('修改密码失败:', err)
      error.value = err.message || '修改密码失败，请稍后重试'
      ElMessage.error(error.value)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  const fetchUnreadMessageCount = async () => {
    if (!isLoggedIn.value) return 0
    
    try {
      const response = await api.message.getUnreadCount()
      unreadMessageCount.value = response.data
      return response
    } catch (err) {
      console.error('获取未读消息数量失败:', err)
      return 0
    }
  }
  
  // 获取用户信息
  const fetchUserProfile = async () => {
    if (!isLoggedIn.value) return null
    
    loading.value = true
    error.value = null
    
    try {
      console.log('正在获取用户信息...')
      const response = await api.user.getProfile()
      if (response.data) {
        setUser(response.data)
        console.log('获取用户信息成功:', response.data)
        return response.data
      } else {
        throw new Error('获取用户信息响应格式不正确')
      }
    } catch (err) {
      console.error('获取用户信息失败:', err)
      error.value = err.message || '获取用户信息失败'
      // 如果是401错误，会在API拦截器中处理登出
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 检查登录状态是否有效
  const checkAuthStatus = async () => {
    if (!token.value) return false
    
    try {
      // 尝试获取用户信息，验证token是否有效
      const userProfile = await fetchUserProfile()
      return !!userProfile
    } catch (err) {
      console.error('登录状态校验失败:', err)
      // 清除无效的登录状态
      logout()
      return false
    }
  }
  
  return {
    token,
    user,
    unreadMessageCount,
    loading,
    error,
    isLoggedIn,
    isAdmin,
    setToken,
    setUser,
    login,
    register,
    logout,
    updateProfile,
    changePassword,
    fetchUnreadMessageCount,
    fetchUserProfile,
    checkAuthStatus
  }
}) 