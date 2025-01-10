<template>
  <div>
    <h1>WebSocket Chat</h1>
    <!-- 用户名输入框 -->
    <div v-if="!connected">
      <input v-model="username" placeholder="Enter your username" />
      <button @click="connectWebSocket" :disabled="!username">Connect</button>
    </div>

    <!-- 连接成功后的聊天界面 -->
    <div v-if="connected">
      <h2>Welcome, {{ username }}</h2>
      <!-- 目标用户输入框 -->
      <input v-model="targetUser" placeholder="Enter target username" />
      <input v-model="message" placeholder="Enter message" />
      <button @click="sendMessage" :disabled="!targetUser || !message">Send Message</button>

      <!-- 聊天记录 -->
      <div v-if="chatHistory.length">
        <h3>Chat History</h3>
        <ul>
          <li v-for="(msg, index) in chatHistory" :key="index">
            {{ msg.sender }}: {{ msg.message }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      socket: null, // WebSocket 连接对象
      username: '', // 用户名
      targetUser: '', // 目标用户
      message: '', // 发送的消息
      connected: false, // 用户是否已连接
      chatHistory: [], // 聊天历史记录
    }
  },
  methods: {
    // 连接 WebSocket
    connectWebSocket() {
      if (this.username.trim()) {
        // 创建 WebSocket 连接
        this.socket = new WebSocket(`ws://192.168.2.182:8080/websocket/${this.username}`)

        this.socket.onopen = () => {
          this.connected = true // 连接成功
          this.addMessage('System', 'Connected to the server', true) // 显示连接成功消息
        }

        this.socket.onmessage = (event) => {
          // 处理来自服务器的消息
          const receivedMessage = event.data
          console.log(receivedMessage)
          if (receivedMessage === '來自後台：連線成功') {
            this.addMessage('System', receivedMessage, true) // 系统消息
          } else {
            // 正常消息格式: "sender: message"
            const parts = receivedMessage.split(': ', 2)
            if (parts.length === 2) {
              this.addMessage(parts[0], parts[1])
            }
          }
        }

        this.socket.onerror = (error) => {
          console.error('WebSocket error:', error)
        }

        this.socket.onclose = () => {
          this.connected = false
        }
      } else {
        alert('Username is required to connect.')
      }
    },

    // 发送消息
    sendMessage() {
      if (this.socket && this.targetUser && this.message) {
        const messageData = {
          sender: this.username,
          target: this.targetUser,
          message: this.message,
        }
        this.socket.send(JSON.stringify(messageData)) // 发送 JSON 格式的消息
        this.addMessage(this.username, this.message) // 添加消息到聊天记录
        this.message = '' // 清空消息框
      }
    },

    // 添加消息到聊天记录
    addMessage(sender, message, isSystemMessage = false) {
      const formattedMessage = { sender, message }
      if (isSystemMessage) {
        formattedMessage.message = `System: ${message}` // 系统消息格式
      }
      this.chatHistory.push(formattedMessage)
    },
  },
}
</script>

<style scoped>
/* 样式可以根据需求调整 */
input {
  margin: 5px;
  padding: 8px;
}

button {
  padding: 8px;
  margin-top: 10px;
}

h1 {
  color: #4caf50;
}

h2 {
  color: #007bff;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  padding: 5px;
  border-bottom: 1px solid #ddd;
}
</style>
