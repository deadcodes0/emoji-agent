// src/composables/useTheme.ts
import { ref, watch } from 'vue'
import {
  THEME_ORDER,
  STORAGE_KEY,
  BOUNDARIES,
  DAY_START,
  type Theme,
} from '@/config/themes'

export function pickThemeByTime(date = new Date()): Theme {
  const h = date.getHours() + date.getMinutes() / 60
  if (h >= 6 && h < 17) return 'day'
  if (h >= 17 && h < 19.5) return 'dusk'
  if (h >= 19.5 && h < 23) return 'night'
  return 'midnight'
}

/** 距离下一个时段边界还有多少毫秒 */
function msUntilNextBoundary(now = new Date()): number {
  const nowTs = now.getTime()
  let next: number | null = null

  for (const { hour, minute } of BOUNDARIES) {
    const t = new Date(now)
    t.setHours(hour, minute, 0, 0)
    if (t.getTime() > nowTs) {
      next = t.getTime()
      break
    }
  }

  // 今天已无边界 → 取明天 6:00
  if (next === null) {
    const t = new Date(now)
    t.setDate(t.getDate() + 1)
    t.setHours(DAY_START.hour, DAY_START.minute, 0, 0)
    next = t.getTime()
  }

  return next - nowTs + 500
}

// —— 模块级单例状态 ——
const theme = ref<Theme>(pickThemeByTime())
const isManual = ref(false)
let timer: number | undefined
let initialized = false

function applyAutoTheme() {
  if (isManual.value) return
  const auto = pickThemeByTime()
  if (theme.value !== auto) theme.value = auto
}

function scheduleNextCheck() {
  if (timer) clearTimeout(timer)
  timer = window.setTimeout(() => {
    applyAutoTheme()
    scheduleNextCheck()
  }, msUntilNextBoundary())
}

function onVisibilityChange() {
  if (document.visibilityState === 'visible') {
    applyAutoTheme()
    scheduleNextCheck()
  }
}

function initOnce() {
  if (initialized) return
  initialized = true

  const saved = localStorage.getItem(STORAGE_KEY) as Theme | null
  if (saved && (THEME_ORDER as readonly string[]).includes(saved)) {
    theme.value = saved
    isManual.value = true
  }

  scheduleNextCheck()
  document.addEventListener('visibilitychange', onVisibilityChange)
}

function setThemeManual(t: Theme) {
  theme.value = t
  isManual.value = true
  localStorage.setItem(STORAGE_KEY, t)
}

function setThemeAuto() {
  isManual.value = false
  localStorage.removeItem(STORAGE_KEY)
  theme.value = pickThemeByTime()
}

// 同步到 <html data-theme>
watch(theme, (t) => {
  document.documentElement.dataset.theme = t
}, { immediate: true })

export function useTheme() {
  initOnce()
  return { theme, isManual, setThemeManual, setThemeAuto }
}