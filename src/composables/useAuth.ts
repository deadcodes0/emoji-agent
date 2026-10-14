// src/composables/useAuth.ts
import { computed, ref } from 'vue'
import { getToken, getUser, clearAuth, type AuthUser } from '@/utils/auth'

/** 模块级单例：整个应用共享同一份登录态 */
const token = ref<string | null>(getToken())
const user = ref<AuthUser | null>(getUser())

/** 从 storage 重新同步（路由切换后、其他标签页登录/登出时调用） */
export function syncAuth() {
  token.value = getToken()
  user.value = getUser()
}

export function useAuth() {
  const isLoggedIn = computed(() => !!token.value)
  const currentUser = computed(() => user.value)

  /**
   * 退出登录：只清 storage 与响应式状态，
   * 不在这里做 location.href 跳转（交给调用方的 router 处理）。
   */
  function logout() {
    clearAuth()
    token.value = null
    user.value = null
  }

  /** 登录成功后手动同步（LoginView 调用 login()/register() 后调一次） */
  function refresh() {
    syncAuth()
  }

  return { isLoggedIn, currentUser, logout, refresh, syncAuth }
}
