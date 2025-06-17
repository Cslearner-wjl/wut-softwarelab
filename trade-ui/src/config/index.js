/**
 * 全局配置文件
 */

const config = {
  // API基础URL
  apiBaseUrl: 'http://localhost:8080',
  
  // 图片服务器URL，用于拼接图片路径
  imageBaseUrl: 'http://localhost:8080',
  
  // 超时时间（毫秒）
  timeout: 30000,
  
  // 分页默认值
  pagination: {
    defaultPageSize: 12,
    pageSizes: [12, 24, 36, 48]
  },
  
  // 图片占位符
  placeholderImage: '/placeholder.png'
}

export default config 