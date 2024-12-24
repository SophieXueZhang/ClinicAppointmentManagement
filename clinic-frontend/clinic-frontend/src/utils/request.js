import axios from 'axios'
import { getToken } from '@/utils/auth'

const service = axios.create({
  baseURL: 'https://manage-admin-website-tunnel-luh5by19.devinapps.com/api',
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 0) {
      uni.showToast({
        title: res.msg || '请求失败',
        icon: 'none',
        duration: 2000
      })
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  error => {
    console.log('err' + error)
    uni.showToast({
      title: error.message || '请求失败',
      icon: 'none',
      duration: 2000
    })
    return Promise.reject(error)
  }
)

export default service
