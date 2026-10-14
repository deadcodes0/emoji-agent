// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import EmotionDiaryView from '@/views/EmotionDiaryView.vue'
import LoginView from '@/views/LoginView.vue'
import { getToken } from '@/utils/auth'
import { syncAuth } from '@/composables/useAuth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/warm-home', name: 'warm-home', component: HomeView },
    { path: '/login', name: 'login', component: LoginView },
    {
      path: '/chat',
      redirect: (to) => ({ name: 'diary', query: to.query }),
    },
    {
      path: '/diary',
      name: 'diary',
      component: EmotionDiaryView,
      meta: { back: true, requiresAuth: true },
    },
  ],
})

/** 未登录也能访问的页面 */
const PUBLIC_ROUTES = new Set(['home', 'warm-home', 'login'])

router.beforeEach((to) => {
  syncAuth()
  const hasToken = !!getToken()
  const name = typeof to.name === 'string' ? to.name : ''

  // 未登录：只放行公开页；受保护页跳登录，并带回跳地址
  if (!hasToken && !PUBLIC_ROUTES.has(name)) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // 已登录访问登录页：回首页
  if (hasToken && to.name === 'login') return { name: 'home' }

  return true
})

router.afterEach(() => {
  syncAuth()
})

export default router
