<template>
  <view class="movie-detail">
    <image :src="movie.poster" mode="aspectFill" class="movie-poster"></image>
    <view class="movie-info">
      <text class="title">{{movie.title}}</text>
      <text class="price">¥{{movie.price}}</text>
      <text class="description">{{movie.description}}</text>
    </view>
    <button class="buy-btn" @click="createOrder">立即购票</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      movie: {}
    }
  },
  onLoad(options) {
    // TODO: Fetch movie detail from API
    this.movie = {
      id: options.id,
      title: '测试电影',
      price: 35,
      description: '电影详细描述...',
      poster: '/static/movie1.jpg'
    }
  },
  methods: {
    createOrder() {
      uni.navigateTo({
        url: `/pages/order/create?movieId=${this.movie.id}`
      })
    }
  }
}
</script>

<style>
.movie-detail {
  padding-bottom: 50px;
}
.movie-poster {
  width: 100%;
  height: 300px;
}
.movie-info {
  padding: 15px;
}
.title {
  font-size: 20px;
  font-weight: bold;
}
.price {
  color: #f00;
  font-size: 18px;
  margin: 10px 0;
}
.description {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}
.buy-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  line-height: 50px;
  background: #007AFF;
  color: white;
  text-align: center;
  font-size: 16px;
}
</style>
