import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('blog_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (!body || typeof body !== 'object' || !('code' in body)) return body
    if (body.code === 0) return body.data
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message
    if (status === 401) {
      localStorage.removeItem('blog_token')
      localStorage.removeItem('blog_user')
      ElMessage.error('登录已过期，请重新登录')
      if (!location.pathname.startsWith('/login')) {
        location.replace(`/login?redirect=${encodeURIComponent(location.pathname + location.search)}`)
      }
    } else if (status === 403) {
      ElMessage.error(message || '没有权限执行该操作')
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error(message || '网络异常，请检查后端是否启动')
    }
    return Promise.reject(error)
  }
)

export default request