import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import SelectEmail from '../views/SelectEmail.vue'
import Login from '../views/Login.vue'
import UserPage from '../views/User.vue' // 使用者頁面
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
    },
    { path: '/selectEmail', name: 'SelectEmail', component: SelectEmail },
    { path: '/user', name: 'Chat', component: UserPage }, // 使用者頁面
    // { path: '/chat/:receiverEmail', name: 'Chat', component: Chat },
  ],
})

export default router
