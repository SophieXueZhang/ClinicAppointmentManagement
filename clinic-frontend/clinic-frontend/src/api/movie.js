import request from '@/utils/request'
import { md5 } from '@/utils/crypto'

const ACCESS_KEY = 'your_access_key' // 从配置获取
const APP_KEY = 'your_app_key' // 从配置获取

// 生成签名
function generateSign(params) {
  const timestamp = Math.floor(Date.now() / 1000).toString()
  const randstr = Math.random().toString(36).substr(2, 10)
  
  const signParams = {
    ...params,
    access: ACCESS_KEY,
    timestamp: timestamp,
    randstr: randstr
  }
  
  const keys = Object.keys(signParams).sort()
  let signStr = keys.map(key => `${key}=${signParams[key]}`).join('&')
  signStr += `&key=${APP_KEY}`
  
  return {
    sign: md5(signStr).toUpperCase(),
    timestamp,
    randstr
  }
}

// API请求函数
export const movieApi = {
  // 创建订单
  createOrder(data) {
    const signData = generateSign(data)
    return request({
      url: '/movie/createOrder',
      method: 'post',
      headers: {
        'X-Access': ACCESS_KEY,
        'X-Timestamp': signData.timestamp,
        'X-Randstr': signData.randstr,
        'X-Sign': signData.sign
      },
      data
    })
  },

  // 获取订单列表
  getOrderList(params) {
    const signData = generateSign(params)
    return request({
      url: '/movie/getOrderList',
      method: 'post',
      headers: {
        'X-Access': ACCESS_KEY,
        'X-Timestamp': signData.timestamp,
        'X-Randstr': signData.randstr,
        'X-Sign': signData.sign
      },
      data: params
    })
  },

  // 获取财务信息
  getFinanceInfo() {
    const signData = generateSign({})
    return request({
      url: '/movie/getFinanceInfo',
      method: 'get',
      headers: {
        'X-Access': ACCESS_KEY,
        'X-Timestamp': signData.timestamp,
        'X-Randstr': signData.randstr,
        'X-Sign': signData.sign
      }
    })
  }
}
