<template>
  <div class="user-page">
    <h2>Welcome, {{ email }}!</h2>
    <h2>-----------------------------------------------------------</h2>
    <h2>與 {{ receiverEmail }} 連接</h2>

    <div class="chat-window">
      <div class="messages" ref="messagesContainer">
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', message.sender === email ? 'sent' : 'received']"
        >
          <div class="message-text">
            <span>{{ message.text }}</span>
          </div>
        </div>
      </div>
      <div class="input-container">
        <input v-model="messageText" @keyup.enter="sendMessage" placeholder="Type a message..." />
        <button @click="sendMessage">Send</button>
      </div>
    </div>
  </div>
</template>
<script>
import { useUserStore } from '@/stores/userStore' // 引入 Pinia store

export default {
  data() {
    return {
      messageText: '', // 訊息輸入框的值
      messages: [], // 存放訊息的數組
      socket: null, // WebSocket 連接
    }
  },
  computed: {
    email() {
      const userStore = useUserStore()
      return userStore.email // 讀取 email
    },
    token() {
      const userStore = useUserStore()
      return userStore.token // 讀取 token
    },
    receiverEmail() {
      const userStore = useUserStore()
      return userStore.receiverEmail // 讀取 email
    },
  },
  mounted() {
    const userStore = useUserStore()
    console.log('Token:', userStore.token) // 在 console 中印出 token
    this.connectWebSocket() // 初始化 WebSocket 連接
  },
  beforeUnmount() {
    if (this.socket) {
      this.socket.close() // 關閉 WebSocket 連接
    }
  },
  methods: {
    connectToUser() {
      if (this.receiverId.trim() === '') {
        console.error('Receiver Email cannot be empty')
        return
      }
      // 如果 WebSocket 已經連接，則不再重新連接
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        console.log('Already connected to WebSocket')
        return
      }
      // 更新與接收者的連接
      console.log(`Connecting to user with Email: ${this.receiverId}`)
      this.connectWebSocket() // 重新建立 WebSocket 連接
    },

    sendMessage() {
      if (this.messageText.trim() === '') return

      // 發送訊息到 WebSocket 伺服器
      const message = {
        senderEmail: `${this.email}`,
        text: this.messageText,
        receiveEmail: `${this.receiverEmail}`, // 接收者的 Email
      }
      this.socket.send(JSON.stringify(message))

      // 立即將發送的訊息顯示在對話框中
      this.messages.push({
        sender: this.email, // 設置發送者為當前用戶
        text: this.messageText,
      })

      // 清空訊息輸入框
      this.messageText = ''
    },
    receiveMessage(event) {
      const message = JSON.parse(event.data) // 解析接收到的訊息
      // 修改訊息格式，使其包含 senderEmail 和 text
      this.messages.push({
        sender: message.senderEmail,
        text: message.text,
      })
      this.scrollToBottom() // 滾動到訊息列表的底部
      console.log(message) // 查看訊息格式
    },
    scrollToBottom() {
      // 滾動訊息容器到底部
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        container.scrollTop = container.scrollHeight
      })
    },
    connectWebSocket() {
      const socketURL = `ws://192.168.2.182:8080/chat/d2c5e1fc-7599-490c-930b-58255de2b88b/${this.email}`
      this.socket = new WebSocket(socketURL) // 這是 WebSocket 伺服器的地址

      // 當 WebSocket 連接成功時
      this.socket.onopen = () => {
        console.log('Connected to WebSocket server')
      }

      // 當接收到訊息時
      this.socket.onmessage = this.receiveMessage

      // 當 WebSocket 連接關閉時
      this.socket.onclose = () => {
        console.log('Disconnected from WebSocket server')
      }

      // 當 WebSocket 出現錯誤時
      this.socket.onerror = (error) => {
        console.error('WebSocket error:', error)
      }
    },
  },
}
</script>

<style scoped>
.user-page {
  padding: 20px;
  text-align: center;
}

.chat-window {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  border: 1px solid #ccc;
  border-radius: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  height: 500px;
  display: flex;
  flex-direction: column;
}

.messages {
  flex-grow: 1;
  overflow-y: auto;
  margin-bottom: 10px;
}

.message {
  max-width: 60%;
  margin: 10px;
  padding: 10px;
  border-radius: 10px;
  display: inline-block;
  clear: both;
}

/* 發送者的訊息 */
.sent {
  background-color: #e0e0e0; /* 灰色背景 */
  color: black;
  text-align: right;
  float: right;
}

/* 接收者的訊息 */
.received {
  background-color: #e0e0e0; /* 灰色背景 */
  color: black;
  text-align: left;
  float: left;
}

.input-container {
  display: flex;
  align-items: center;
}

input {
  width: 80%;
  padding: 10px;
  margin-right: 10px;
  border-radius: 20px;
  border: 1px solid #ccc;
}

button {
  padding: 10px 20px;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}

button:hover {
  background-color: #45a049;
}
</style>
