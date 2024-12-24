<template>
  <view class="register-container">
    <view class="register-form">
      <input type="text" v-model="form.username" placeholder="用户名" />
      <input type="password" v-model="form.password" placeholder="密码" />
      <input type="password" v-model="form.confirmPassword" placeholder="确认密码" />
      <button @click="handleRegister">注册</button>
      <text @click="goToLogin">已有账号？去登录</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: ''
      }
    }
  },
  methods: {
    handleRegister() {
      if (this.form.password !== this.form.confirmPassword) {
        uni.showToast({
          title: '两次密码不一致',
          icon: 'none'
        })
        return
      }
      // TODO: Implement register logic
      console.log('Register with:', this.form)
    },
    goToLogin() {
      uni.navigateTo({
        url: '/pages/user/login'
      })
    }
  }
}
</script>

<style>
.register-container {
  padding: 20px;
}
.register-form {
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
button {
  height: 40px;
  background-color: #007AFF;
  color: white;
  border-radius: 4px;
}
text {
  text-align: center;
  color: #007AFF;
  font-size: 14px;
  margin-top: 10px;
}
</style>
