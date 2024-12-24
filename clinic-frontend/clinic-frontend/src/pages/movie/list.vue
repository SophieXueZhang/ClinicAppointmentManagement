<template>
  <view class="movie-list">
    <view class="movie-item" v-for="movie in movies" :key="movie.id" @click="goToDetail(movie.id)">
      <image :src="movie.poster" mode="aspectFill"></image>
      <view class="movie-info">
        <text class="title">{{movie.title}}</text>
        <text class="price">¥{{movie.price}}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      movies: []
    }
  },
  onLoad() {
    // TODO: Fetch movie list from API
    this.movies = [
      { id: 1, title: '测试电影1', price: 35, poster: '/static/movie1.jpg' },
      { id: 2, title: '测试电影2', price: 45, poster: '/static/movie2.jpg' }
    ]
  },
  methods: {
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/movie/detail?id=${id}`
      })
    }
  }
}
</script>

<style>
.movie-list {
  padding: 10px;
}
.movie-item {
  margin-bottom: 15px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.movie-item image {
  width: 100%;
  height: 200px;
}
.movie-info {
  padding: 10px;
}
.title {
  font-size: 16px;
  font-weight: bold;
}
.price {
  color: #f00;
  margin-top: 5px;
  font-size: 14px;
}
</style>
