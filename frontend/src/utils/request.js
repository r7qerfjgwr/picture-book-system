import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    // 更友好的错误消息处理
    let message = '网络错误'
    if (error.response) {
      // 服务器返回了错误响应
      const status = error.response.status
      const data = error.response.data

      if (data && data.message) {
        message = data.message
      } else if (status === 500) {
        message = '服务器内部错误'
      } else if (status === 401) {
        message = '登录已过期，请重新登录'
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (status === 403) {
        message = '没有权限访问'
      } else if (status === 404) {
        message = '请求的资源不存在'
      } else {
        message = `请求失败 (${status})`
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      message = '服务器无响应，请检查网络连接'
    } else if (error.message) {
      message = error.message
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
