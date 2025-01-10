import './assets/main.css'
import axios from 'axios'
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

const app = createApp(App)
// 全局註冊 axios，使它在每個組件中都可用
app.config.globalProperties.$axios = axios

app.use(createPinia())
app.use(router)

app.mount('#app')
