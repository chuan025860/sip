<template>
  <div class="login-container">
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="phone">Phone:</label>
        <input type="text" v-model="phone" id="phone" required />
      </div>
      <div class="form-group">
        <label for="password">Password:</label>
        <input type="password" v-model="password" id="password" required />
      </div>
      <button type="submit">Login</button>
    </form>
  </div>
</template>

<script>
import { useUserStore } from '@/stores/userStore' // 引入 Pinia store
export default {
  data() {
    return {
      phone: '',
      password: '',
    }
  },
  methods: {
    async handleLogin() {
      const loginData = {
        phone: this.phone,
        password: this.password,
      }

      try {
        // 使用 this.$axios 來發送請求
        const response = await this.$axios.post(
          'http://192.168.2.182:8080/customer/login',
          loginData,
        )

        if (response.data.code === 200) {
          // 檢查後端回應中的 code 屬性
          console.log('Login success')
          const customer = response.data.data.customer
          const token = response.data.data.token

          // 使用 Pinia store 保存 customerName 和 token
          const userStore = useUserStore()
          userStore.setUserInfo(customer.email, token)
          // 登入成功後跳轉到使用者頁面
          this.$router.push('/selectEmail')
          // 登入成功後進行導航或其他操作
          // this.$router.push('/')  // 例如：重定向到主頁
        } else {
          console.log('Login failed')
          console.log(response.data.message) // 打印出錯誤消息
          // 顯示錯誤消息
          // this.errorMessage = response.data.message || 'Login failed';
        }
      } catch (error) {
        console.error('An error occurred during login:', error)
        // 顯示錯誤消息
        // this.errorMessage = 'An error occurred. Please try again later.';
      }
    },
  },
}
</script>

<style scoped>
.login-container {
  width: 300px;
  margin: 50px auto;
  padding: 2rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  background-color: #f9f9f9;
  text-align: center;
}

h2 {
  margin-bottom: 1.5rem;
  font-size: 24px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

label {
  font-size: 14px;
  margin-bottom: 0.25rem;
}

input {
  width: 100%;
  padding: 0.75rem;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  padding: 0.75rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #45a049;
}

.error {
  color: red;
  font-size: 14px;
  margin-top: 1rem;
}
</style>
