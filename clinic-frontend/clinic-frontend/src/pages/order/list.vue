<template>
  <view class="order-list">
    <view class="order-item" v-for="order in orders" :key="order.id">
      <view class="order-header">
        <text class="order-no">订单号：{{order.orderNo}}</text>
        <text class="order-status">{{order.status}}</text>
      </view>
      <view class="order-content">
        <text class="movie-title">{{order.movieTitle}}</text>
        <text class="order-price">¥{{order.price}}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      orders: []
    }
  },
  onLoad() {
    // TODO: Fetch order list from API
    this.orders = [
      {
        id: 1,
        orderNo: 'ORD20231224001',
        status: '已支付',
        movieTitle: '测试电影1',
        price: 35
      }
    ]
  }
}
</script>

<style>
.order-list {
  padding: 10px;
}
.order-item {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 10px;
  padding: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}
.order-no {
  font-size: 14px;
  color: #666;
}
.order-status {
  color: #007AFF;
}
.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.movie-title {
  font-size: 16px;
  font-weight: bold;
}
.order-price {
  color: #f00;
  font-size: 16px;
}
</style>
