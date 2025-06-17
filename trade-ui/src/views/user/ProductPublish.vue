<template>
  <div class="publish-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h2>发布商品</h2>
        </div>
      </template>
      
      <el-form 
        ref="formRef" 
        :model="productForm" 
        :rules="rules" 
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <!-- 基本信息 -->
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="productForm.title" placeholder="请输入商品标题" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="productForm.price" :min="0.01" :precision="2" :step="1" />
        </el-form-item>
        
        <el-form-item label="交易地点" prop="tradeLocation">
          <el-input v-model="productForm.tradeLocation" placeholder="请输入交易地点" />
        </el-form-item>
        
        <el-form-item label="商品描述" prop="description">
          <el-input 
            v-model="productForm.description" 
            type="textarea" 
            :rows="6" 
            placeholder="请详细描述商品的成色、购买时间、使用感受等信息" 
            maxlength="1000" 
            show-word-limit 
          />
        </el-form-item>
        
        <!-- 商品图片上传 -->
        <el-form-item label="商品图片" prop="images">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            list-type="picture-card"
            :http-request="uploadImage"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :limit="6"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                请上传商品图片，最多6张，每张不超过2MB，支持JPG/PNG格式
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">发布商品</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '../../api'

const router = useRouter()
const formRef = ref(null)
const fileList = ref([])
const submitting = ref(false)

// 表单数据
const productForm = reactive({
  title: '',
  price: 1.00,
  tradeLocation: '',
  description: '',
  imagePaths: []
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在2-50个字符之间', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }
  ],
  tradeLocation: [
    { required: true, message: '请输入交易地点', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '描述长度应在10-1000个字符之间', trigger: 'blur' }
  ]
}

// 图片上传前检查
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传JPG或PNG格式的图片!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  
  return true
}

// 上传图片
const uploadImage = async (options) => {
  const { file } = options
  
  const formData = new FormData()
  formData.append('files', file)
  
  try {
    const response = await api.product.uploadImage(formData)
    
    if (response.success && response.data && response.data.length > 0) {
      productForm.imagePaths.push(response.data[0])
      options.onSuccess()
    } else {
      options.onError('上传失败')
      ElMessage.error('图片上传失败')
    }
  } catch (error) {
    options.onError('上传失败')
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
  }
}

// 删除图片
const handleRemove = (file, fileList) => {
  const index = productForm.imagePaths.findIndex(path => path.includes(file.name))
  if (index !== -1) {
    productForm.imagePaths.splice(index, 1)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (productForm.imagePaths.length === 0) {
      ElMessage.warning('请至少上传一张商品图片')
      return
    }
    
    submitting.value = true
    
    try {
      const response = await api.product.create(productForm)
      
      ElMessage.success('商品发布成功')
      
      // 跳转到商品详情页
      router.push(`/product/${response.data.id}`)
    } catch (error) {
      ElMessage.error('商品发布失败: ' + (error.message || '未知错误'))
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 20px auto;
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

.el-upload__tip {
  line-height: 1.2;
  padding: 8px 0;
}
</style> 