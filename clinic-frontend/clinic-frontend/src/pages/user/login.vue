<template>
  <view class="login-container">
    <view class="login-form">
      <input type="text" v-model="form.username" placeholder="用户名" />
      <input type="password" v-model="form.password" placeholder="密码" />
      <button @click="handleLogin">登录</button>
      <text @click="goToRegister">还没有账号？去注册</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      form: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    handleLogin() {
      // TODO: Implement login logic
      console.log('Login with:', this.form)
    },
    goToRegister() {
      uni.navigateTo({
        url: '/pages/user/register'
      })
    }
  }
}
</script>

<style>
.login-container {
  padding: 20px;
}
.login-form {
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
