<template>
  <div class="select-chat-container">
    <h2>Select a Conversation</h2>
    <p>Welcome, {{ email }}</p>
    <div class="conversation-list">
      <div v-for="conversation in conversations" :key="conversation.id">
        <button @click="startChat(conversation.receiverEmail)">
          連接: {{ conversation.receiverEmail }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { useUserStore } from '@/stores/userStore' // 引入 Pinia store
export default {
  data() {
    return {
      conversations: [], // 模擬的對話列表
    }
  },
  computed: {
    email() {
      const userStore = useUserStore()
      return userStore.email // 讀取 email
    },
  },

  methods: {
    startChat(receiverEmail) {
      // 在 Pinia 中設置 receiverEmail
      const userStore = useUserStore()
      userStore.setReceiverEmail(receiverEmail)
      console.log(userStore.receiverEmail)
      // 跳轉到聊天室頁面，網址中不包含 email
      this.$router.push({ name: 'Chat' })
    },
  },
  mounted() {
    // 在這裡可以呼叫 API 來獲取對話列表
    this.conversations = [
      { id: 1, receiverEmail: 'dimimg92306@gmail.com' },
      { id: 2, receiverEmail: 'test@gmail.com' },
    ]
  },
}
</script>

<style scoped>
.select-chat-container {
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

button {
  padding: 0.75rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 10px;
}

button:hover {
  background-color: #45a049;
}
</style>
