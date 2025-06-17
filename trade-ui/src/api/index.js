import axios from 'axios'
import { ElMessage } from 'element-plus'
import config from '../config'

// 创建axios实例
const http = axios.create({
  baseURL: config.apiBaseUrl,
  timeout: config.timeout,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: false // 不需要跨域请求携带cookie，使用JWT认证
})

// 请求拦截器
http.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
      console.log('请求附带token:', token.substring(0, 15) + '...')
    }
    console.log('请求URL:', config.url, '方法:', config.method, '数据:', config.data || config.params)
    
    // 增加随机参数避免缓存问题
    if (config.url.includes('/products/upload') || config.url.includes('/images')) {
      const timestamp = new Date().getTime()
      config.params = { ...config.params, _t: timestamp }
    }
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    ElMessage.error('发送请求失败，请检查网络连接')
    return Promise.reject(error)
  }
)

// 响应拦截器
http.interceptors.response.use(
  response => {
    console.log('响应成功:', response.config.url, response.data)
    
    // 处理成功但数据为空的情况
    if (response.data && response.data.data === null && response.config.url.includes('/products')) {
      console.warn('返回数据为空，可能需要处理:', response.config.url)
    }
    
    return response.data
  },
  error => {
    console.error('响应错误:', error.config?.url, error)
    
    // 处理错误消息
    let errorMessage = '未知错误，请稍后重试'
    
    if (error.response) {
      // 有响应但状态码错误
      const status = error.response.status
      const data = error.response.data
      
      console.log('错误状态码:', status, '错误数据:', data)
      
      // 处理401错误（未授权）
      if (status === 401) {
        console.warn('未授权请求，清除登录状态')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        errorMessage = '登录已过期，请重新登录'
        
        if (window.location.pathname !== '/auth/login') {
          setTimeout(() => {
            window.location.href = '/auth/login'
          }, 1500)
        }
      } 
      // 处理其他常见错误
      else if (status === 403) {
        errorMessage = '您没有权限执行此操作'
      } else if (status === 404) {
        errorMessage = '请求的资源不存在'
      } else if (status === 400) {
        errorMessage = data.message || '请求参数错误'
      } else if (status === 500) {
        errorMessage = '服务器内部错误，请稍后重试'
      }
      
      // 显示错误信息
      ElMessage.error(errorMessage)
      
      return Promise.reject(data || { message: errorMessage })
    } else if (error.request) {
      // 发送了请求但没有收到响应
      console.error('没有收到响应:', error.request)
      ElMessage.error('服务器无响应，请检查网络连接')
      return Promise.reject({ message: '服务器无响应，请检查网络连接' })
    } else {
      // 请求设置有问题
      console.error('请求配置错误:', error.message)
      ElMessage.error('请求配置错误: ' + error.message)
      return Promise.reject(error)
    }
  }
)

// 处理图片URL
const processImageUrl = (url) => {
  if (!url) return config.placeholderImage
  if (url.startsWith('http')) return url
  // 确保图片URL包含完整域名
  return `${config.imageBaseUrl}${url.startsWith('/') ? '' : '/'}${url}?_t=${new Date().getTime()}`
}

// 通用处理商品数据方法
const processProductData = (product) => {
  if (!product) return product
  
  // 处理图片路径
  if (product.imagePaths) {
    product.imagePaths = product.imagePaths.map(processImageUrl)
  }
  
  return product
}

// 处理商品列表数据
const processProductsResponse = (response) => {
  console.log('处理商品响应:', response);
  if (!response) {
    console.error('响应为空');
    return response;
  }
  
  if (!response.data) {
    console.error('响应数据为空');
    return response;
  }
  
  try {
    if (Array.isArray(response.data)) {
      console.log('处理数组类型的商品数据');
      response.data = response.data.map(processProductData);
    } else if (response.data.content && Array.isArray(response.data.content)) {
      console.log('处理分页类型的商品数据');
      response.data.content = response.data.content.map(processProductData);
    } else {
      console.log('处理单个商品数据');
      response.data = processProductData(response.data);
    }
  } catch (error) {
    console.error('处理商品数据时出错:', error);
  }
  
  return response;
}

// API模块
const api = {
  // 认证相关
  auth: {
    login: (data) => http.post('/api/auth/login', data),
    register: (data) => http.post('/api/auth/register', data),
    test: () => http.get('/api/auth/test')
  },
  
  // 用户相关
  user: {
    getProfile: () => http.get('/api/user/profile'),
    updateProfile: (data) => http.put('/api/user/profile', data),
    changePassword: (data) => {
      console.log('修改密码请求数据:', data)
      return http.put('/api/user/password', data)
    }
  },
  
  // 商品相关
  product: {
    getList: (params) => http.get('/api/products', { params }).then(processProductsResponse),
    getUserProducts: (params) => http.get('/api/products/user', { params }).then(processProductsResponse),
    getById: (id) => http.get(`/api/products/${id}`).then(processProductsResponse),
    create: (data) => http.post('/api/products', data),
    update: (id, data) => http.put(`/api/products/${id}`, data),
    delete: (id) => http.delete(`/api/products/${id}`),
    restore: (id) => http.post(`/api/products/${id}/restore`),
    uploadImage: (formData) => http.post('/api/products/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }),
    markInterest: (id) => http.post(`/api/products/${id}/interest`)
  },
  
  // 订单相关
  order: {
    getList: (params) => http.get('/api/orders', { params }),
    getById: (id) => http.get(`/api/orders/${id}`),
    create: (data) => http.post('/api/orders', data),
    updateStatus: (id, status) => http.put(`/api/orders/${id}/status?status=${status}`)
  },
  
  // 消息相关
  message: {
    getList: (params) => http.get('/api/messages', { params }),
    getById: (id) => http.get(`/api/messages/${id}`),
    markAsRead: (id) => http.put(`/api/messages/${id}/read`),
    markAllAsRead: () => http.put('/api/messages/read-all'),
    getUnreadCount: () => http.get('/api/messages/unread-count')
  },
  
  // 管理员相关
  admin: {
    getUsers: (params) => http.get('/api/admin/users', { params }),
    getUserById: (id) => http.get(`/api/admin/users/${id}`),
    updateUserStatus: (id, enabled) => http.put(`/api/admin/users/${id}/status`, { enabled }),
    resetPassword: (id) => http.put(`/api/admin/users/${id}/reset-password`),
    getProducts: (params) => http.get('/api/admin/products', { params }).then(processProductsResponse),
    removeProduct: (id) => http.put(`/api/admin/products/${id}/remove`)
  }
}

export { processImageUrl }
export default api 