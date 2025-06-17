<template>
  <div class="performance-container">
    <el-card class="control-card">
      <template #header>
        <div class="card-header">
          <h2>性能监控</h2>
          <div class="header-buttons">
            <el-button type="primary" @click="fetchPerformanceData">刷新数据</el-button>
            <el-button type="danger" @click="resetStats">重置统计</el-button>
          </div>
        </div>
      </template>
      
      <div class="test-controls">
        <h3>性能测试</h3>
        <el-form :inline="true" :model="testForm">
          <el-form-item label="并发数">
            <el-input-number v-model="testForm.concurrency" :min="1" :max="100" />
          </el-form-item>
          <el-form-item label="请求数">
            <el-input-number v-model="testForm.requests" :min="1" :max="1000" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="runTest" :loading="testing">执行测试</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <el-card v-if="testResult" class="result-card">
      <template #header>
        <div class="card-header">
          <h2>测试结果</h2>
          <span>执行时间: {{ new Date().toLocaleString() }}</span>
        </div>
      </template>
      
      <el-descriptions border>
        <el-descriptions-item label="并发数">{{ testResult.concurrency }}</el-descriptions-item>
        <el-descriptions-item label="总请求数">{{ testResult.requests }}</el-descriptions-item>
        <el-descriptions-item label="成功请求">{{ testResult.successCount }}</el-descriptions-item>
        <el-descriptions-item label="错误请求">{{ testResult.errorCount }}</el-descriptions-item>
        <el-descriptions-item label="总执行时间">{{ testResult.totalTimeMs }}ms</el-descriptions-item>
        <el-descriptions-item label="每秒请求数">{{ testResult.requestsPerSecond.toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <el-card class="stats-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>方法执行统计</h2>
          <el-input
            v-model="search"
            placeholder="搜索方法"
            style="width: 300px"
            clearable
          />
        </div>
      </template>
      
      <el-table
        :data="filteredStats"
        style="width: 100%"
        :default-sort="{ prop: 'averageExecutionTime', order: 'descending' }"
        border
        stripe
        height="500"
      >
        <el-table-column prop="method" label="方法名称" width="400" show-overflow-tooltip />
        <el-table-column prop="totalExecutions" label="调用次数" sortable width="120" />
        <el-table-column prop="averageExecutionTime" label="平均执行时间(ms)" sortable width="180">
          <template #default="scope">
            {{ scope.row.averageExecutionTime.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalExecutionTime" label="总执行时间(ms)" sortable width="150" />
        <el-table-column prop="maxExecutionTime" label="最长执行时间(ms)" sortable width="180" />
        <el-table-column prop="minExecutionTime" label="最短执行时间(ms)" sortable width="180" />
      </el-table>
    </el-card>
    
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="jvm-card" v-loading="jvmLoading">
          <template #header>
            <div class="card-header">
              <h2>JVM内存使用情况</h2>
              <el-button size="small" @click="fetchJvmMemory">刷新</el-button>
            </div>
          </template>
          
          <div v-if="jvmMemory">
            <h3>堆内存</h3>
            <el-progress 
              :percentage="parseFloat(jvmMemory.heap.usagePercentage)" 
              :format="format => jvmMemory.heap.usagePercentage"
              :stroke-width="15"
              status="warning"
            />
            <el-descriptions border size="small" column="2">
              <el-descriptions-item label="已使用">{{ jvmMemory.heap.used }}</el-descriptions-item>
              <el-descriptions-item label="已提交">{{ jvmMemory.heap.committed }}</el-descriptions-item>
              <el-descriptions-item label="初始值">{{ jvmMemory.heap.init }}</el-descriptions-item>
              <el-descriptions-item label="最大值">{{ jvmMemory.heap.max }}</el-descriptions-item>
            </el-descriptions>
            
            <h3 class="mt-20">非堆内存</h3>
            <el-progress 
              :percentage="parseFloat(jvmMemory.nonHeap.usagePercentage)" 
              :format="format => jvmMemory.nonHeap.usagePercentage"
              :stroke-width="15"
              status="success"
            />
            <el-descriptions border size="small" column="2">
              <el-descriptions-item label="已使用">{{ jvmMemory.nonHeap.used }}</el-descriptions-item>
              <el-descriptions-item label="已提交">{{ jvmMemory.nonHeap.committed }}</el-descriptions-item>
              <el-descriptions-item label="初始值">{{ jvmMemory.nonHeap.init }}</el-descriptions-item>
              <el-descriptions-item label="最大值">{{ jvmMemory.nonHeap.max }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty v-else description="暂无数据" />
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="jvm-card" v-loading="threadsLoading">
          <template #header>
            <div class="card-header">
              <h2>线程信息</h2>
              <el-button size="small" @click="fetchThreadsInfo">刷新</el-button>
            </div>
          </template>
          
          <div v-if="threadsInfo" class="threads-info">
            <el-statistic title="当前线程数" :value="threadsInfo.threadCount">
              <template #suffix>
                <span>个</span>
              </template>
            </el-statistic>
            
            <el-statistic title="峰值线程数" :value="threadsInfo.peakThreadCount">
              <template #suffix>
                <span>个</span>
              </template>
            </el-statistic>
            
            <el-statistic title="已创建线程总数" :value="threadsInfo.totalStartedThreadCount">
              <template #suffix>
                <span>个</span>
              </template>
            </el-statistic>
            
            <el-statistic title="守护线程数" :value="threadsInfo.daemonThreadCount">
              <template #suffix>
                <span>个</span>
              </template>
            </el-statistic>
          </div>
          <el-empty v-else description="暂无数据" />
        </el-card>
        
        <el-card class="jvm-card mt-20" v-loading="jvmInfoLoading">
          <template #header>
            <div class="card-header">
              <h2>JVM信息</h2>
              <el-button size="small" @click="fetchJvmInfo">刷新</el-button>
            </div>
          </template>
          
          <div v-if="jvmInfo">
            <el-descriptions border size="small">
              <el-descriptions-item label="可用处理器">{{ jvmInfo.runtime.availableProcessors }}</el-descriptions-item>
              <el-descriptions-item label="空闲内存">{{ jvmInfo.runtime.freeMemory }}</el-descriptions-item>
              <el-descriptions-item label="总内存">{{ jvmInfo.runtime.totalMemory }}</el-descriptions-item>
              <el-descriptions-item label="最大内存">{{ jvmInfo.runtime.maxMemory }}</el-descriptions-item>
              <el-descriptions-item label="运行时间" :span="2">{{ jvmInfo.uptime }}</el-descriptions-item>
            </el-descriptions>
            
            <h3 class="mt-20">JVM参数</h3>
            <div class="jvm-args">
              <el-tag v-for="(value, key) in jvmInfo.jvmArgs" :key="key" class="jvm-arg">
                {{ key }}{{ value ? '=' + value : '' }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无数据" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import config from '../../config'

// 状态变量
const loading = ref(false)
const testing = ref(false)
const stats = ref([])
const search = ref('')
const testResult = ref(null)
const testForm = ref({
  concurrency: 10,
  requests: 100
})

// JVM监控相关状态
const jvmLoading = ref(false)
const threadsLoading = ref(false)
const jvmInfoLoading = ref(false)
const jvmMemory = ref(null)
const threadsInfo = ref(null)
const jvmInfo = ref(null)

// 计算属性
const filteredStats = computed(() => {
  if (!search.value) {
    return stats.value
  }
  const searchTerm = search.value.toLowerCase()
  return stats.value.filter(item => item.method.toLowerCase().includes(searchTerm))
})

// 获取性能数据
const fetchPerformanceData = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`${config.apiBaseUrl}/api/admin/performance`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data && response.data.data) {
      const data = response.data.data
      stats.value = Object.keys(data).map(key => {
        return {
          method: key,
          ...data[key]
        }
      })
    }
  } catch (error) {
    console.error('获取性能数据失败', error)
    ElMessage.error('获取性能数据失败')
  } finally {
    loading.value = false
  }
}

// 重置统计数据
const resetStats = async () => {
  try {
    await ElMessageBox.confirm('确定要重置所有性能统计数据吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const token = localStorage.getItem('token')
    await axios.post(`${config.apiBaseUrl}/api/admin/performance/reset`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    ElMessage.success('统计数据已重置')
    fetchPerformanceData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置统计数据失败', error)
      ElMessage.error('重置统计数据失败')
    }
  }
}

// 执行性能测试
const runTest = async () => {
  testing.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post(
      `${config.apiBaseUrl}/api/admin/performance-test/products`,
      null,
      {
        params: {
          concurrency: testForm.value.concurrency,
          requests: testForm.value.requests
        },
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    )
    
    if (response.data && response.data.data) {
      testResult.value = response.data.data
      ElMessage.success('性能测试完成')
      fetchPerformanceData()
    }
  } catch (error) {
    console.error('执行性能测试失败', error)
    ElMessage.error('执行性能测试失败')
  } finally {
    testing.value = false
  }
}

// 获取JVM内存使用情况
const fetchJvmMemory = async () => {
  jvmLoading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`${config.apiBaseUrl}/api/admin/jprofiler-report/memory`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data && response.data.data) {
      jvmMemory.value = response.data.data
    }
  } catch (error) {
    console.error('获取JVM内存使用情况失败', error)
    ElMessage.error('获取JVM内存使用情况失败')
  } finally {
    jvmLoading.value = false
  }
}

// 获取线程信息
const fetchThreadsInfo = async () => {
  threadsLoading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`${config.apiBaseUrl}/api/admin/jprofiler-report/threads`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data && response.data.data) {
      threadsInfo.value = response.data.data
    }
  } catch (error) {
    console.error('获取线程信息失败', error)
    ElMessage.error('获取线程信息失败')
  } finally {
    threadsLoading.value = false
  }
}

// 获取JVM信息
const fetchJvmInfo = async () => {
  jvmInfoLoading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`${config.apiBaseUrl}/api/admin/jprofiler-report/jvm`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data && response.data.data) {
      jvmInfo.value = response.data.data
    }
  } catch (error) {
    console.error('获取JVM信息失败', error)
    ElMessage.error('获取JVM信息失败')
  } finally {
    jvmInfoLoading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchPerformanceData()
  fetchJvmMemory()
  fetchThreadsInfo()
  fetchJvmInfo()
})
</script>

<style scoped>
.performance-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.control-card,
.result-card,
.stats-card {
  margin-bottom: 20px;
}

.test-controls {
  margin-top: 20px;
}

h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

h3 {
  font-size: 16px;
  margin-top: 0;
}

.mt-20 {
  margin-top: 20px;
}

.jvm-card {
  margin-bottom: 20px;
}

.threads-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.jvm-args {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.jvm-arg {
  margin-bottom: 5px;
}
</style> 