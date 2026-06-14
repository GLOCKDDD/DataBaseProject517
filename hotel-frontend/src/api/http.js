import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：自动附加 token（如果后续需要鉴权头）
http.interceptors.request.use(
  config => config,
  error => Promise.reject(error)
)

// 响应拦截器：统一处理 Result<T> 包装格式
// 请求配置中传 { silent: true } 可跳过自动弹错误 toast
http.interceptors.response.use(
  response => {
    const result = response.data
    // 后端 Result<T>: { code, msg, data }
    if (result.code === 200) {
      return result.data
    }
    if (!response.config?.silent) {
      ElMessage.error(result.msg || '操作失败')
    }
    return Promise.reject(new Error(result.msg || '操作失败'))
  },
  error => {
    const msg = error.response?.data?.msg || error.message || '网络错误'
    if (!error.config?.silent) {
      ElMessage.error(msg)
    }
    return Promise.reject(new Error(msg))
  }
)

export default http
