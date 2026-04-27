import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import SeatSelection from '../views/SeatSelection.vue'
import MyReservations from '../views/MyReservations.vue'

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/home',
    name: 'Home',
    component: Home
  },
  {
    path: '/seat-selection',
    name: 'SeatSelection',
    component: SeatSelection
  },
  {
    path: '/my-reservations',
    name: 'MyReservations',
    component: MyReservations
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router