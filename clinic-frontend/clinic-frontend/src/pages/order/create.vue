<template>
  <view class="order-create">
    <view class="movie-info">
      <text class="title">{{movie.title}}</text>
      <text class="price">¥{{movie.price}}</text>
    </view>
    <view class="form">
      <input type="text" v-model="form.name" placeholder="观影人姓名" />
      <input type="text" v-model="form.phone" placeholder="手机号码" />
    </view>
    <button class="submit-btn" @click="submitOrder">提交订单</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      movie: {},
      form: {
        name: '',
        phone: ''
      }
    }
  },
  onLoad(options) {
    // TODO: Fetch movie info from API
    this.movie = {
      id: options.movieId,
      title: '测试电影',
      price: 35
    }
  },
  methods: {
    submitOrder() {
      // TODO: Submit order to API
      console.log('Submit order:', {
        movieId: this.movie.id,
        ...this.form
      })
      uni.showToast({
        title: '订单提交成功',
        icon: 'success'
      })
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/order/list'
        })
      }, 1500)
    }
  }
}
</script>

<style>
.order-create {
  padding: 20px;
}
.movie-info {
  background: #fff;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
.price {
  color: #f00;
  font-size: 16px;
  margin-top: 10px;
}
.form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
input {
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 10px;
}
.submit-btn {
  margin-top: 20px;
  height: 40px;
  background: #007AFF;
  color: white;
  border-radius: 4px;
}
</style>
