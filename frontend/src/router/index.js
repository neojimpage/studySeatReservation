import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import SeatSelection from '../views/SeatSelection.vue'
import MyReservations from '../views/MyReservations.vue'
import ChatPage from '../views/ChatPage.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import Dashboard from '../views/admin/Dashboard.vue'
import AreaManage from '../views/admin/AreaManage.vue'
import SeatManage from '../views/admin/SeatManage.vue'
import UserManage from '../views/admin/UserManage.vue'

const routes = [
  { path: '/', name: 'Login', component: Login },
  { path: '/home', name: 'Home', component: Home },
  { path: '/seat-selection', name: 'SeatSelection', component: SeatSelection },
  { path: '/my-reservations', name: 'MyReservations', component: MyReservations },
  { path: '/ai-assistant', name: 'ChatPage', component: ChatPage },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: Dashboard },
      { path: 'areas', name: 'AreaManage', component: AreaManage },
      { path: 'seats', name: 'SeatManage', component: SeatManage },
      { path: 'users', name: 'UserManage', component: UserManage },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
