import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    email: localStorage.getItem('email') || '', // 初始化時從 localStorage 讀取
    token: localStorage.getItem('token') || '', // 初始化時從 localStorage 讀取
    receiverEmail: localStorage.getItem('receiverEmail') || '', // 初始化時從 localStorage 讀取
  }),
  actions: {
    // 設置用戶信息並儲存到 localStorage
    setUserInfo(email, token) {
      this.email = email
      this.token = token
      localStorage.setItem('email', email) // 儲存 email 到 localStorage
      localStorage.setItem('token', token) // 儲存 token 到 localStorage
    },
    setReceiverEmail(receiverEmail) {
      this.receiverEmail = receiverEmail
      localStorage.setItem('receiverEmail', receiverEmail) // 儲存 receiverEmail 到 localStorage
    },
    // 清除用戶信息並刪除 localStorage 中的資料
    clearUserInfo() {
      this.email = ''
      this.token = ''
      localStorage.removeItem('email') // 刪除 email
      localStorage.removeItem('token') // 刪除 token
    },
  },
})
