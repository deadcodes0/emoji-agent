<!-- src/App.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import DayBg from '@/components/backgrounds/DayBg.vue'
import DuskBg from '@/components/backgrounds/DuskBg.vue'
import NightBg from '@/components/backgrounds/NightBg.vue'
import MidnightBg from '@/components/backgrounds/MidnightBg.vue'
import AppHeader from '@/components/AppHeader.vue'
import BackButton from '@/components/BackButton.vue'
import ResponseFieldsPreview from '@/components/ResponseFieldsPreview.vue'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import UserMenu from '@/components/UserMenu.vue'
import { useTheme } from '@/composables/useTheme'
import { useAuth } from '@/composables/useAuth'

const { theme } = useTheme()
const route = useRoute()
const { isLoggedIn } = useAuth()

/** 首页：不显示返回按钮 */
const isHome = computed(() => route.name === 'home' || route.name === 'warm-home')
/** 情绪日记页 */
const isDiary = computed(() => route.name === 'diary')
/** 登录页 */
const isLogin = computed(() => route.name === 'login')

/** 情绪日志入口：已登录 + 非日记页 + 非登录页 才显示 */
const showDiaryEntry = computed(
  () => isLoggedIn.value && !isDiary.value && !isLogin.value,
)
</script>

<template>
  <div class="app-root">
    <div class="bg-layer">
      <Transition name="fade-bg">
        <DayBg v-if="theme === 'day'" />
        <DuskBg v-else-if="theme === 'dusk'" />
        <NightBg v-else-if="theme === 'night'" />
        <MidnightBg v-else />
      </Transition>
    </div>

    <!-- 全局顶栏：左 | 中（主题切换）| 右 -->
    <div class="topbar" :data-theme="theme">
      <!-- 左：返回 | 品牌图标 | 标语 -->
      <div class="topbar__left">
        <BackButton v-if="!isHome" />
        <AppHeader />
        <span class="brand-tagline">emoji心理健康助手</span>
      </div>

      <!-- 中：主题切换 -->
      <div class="topbar__center">
        <ThemeSwitcher />
      </div>

      <!-- 右：情绪日志入口 + 用户菜单 -->
      <div class="topbar__right">
        <ResponseFieldsPreview v-if="showDiaryEntry" />
        <UserMenu v-if="isLoggedIn" />
      </div>
    </div>

    <main class="content-layer">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-root {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
}

/* ============================================================
 * 顶栏：三栏 grid —— 左 / 中 / 右 同一行，垂直居中自动对齐
 * ============================================================ */
.topbar {
  --topbar-inset-y: 24px;
  --topbar-inset-x: 24px;

  /* 顶栏统一字号 */
  --topbar-font-size: 15px;

  position: fixed;
  top: var(--topbar-inset-y);
  left: var(--topbar-inset-x);
  right: var(--topbar-inset-x);
  z-index: 10;

  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  column-gap: 16px;

  pointer-events: none;

  animation: dropInLeft 0.6s cubic-bezier(0.22, 1, 0.36, 1);

  --tagline-sub: rgba(107, 74, 58, 0.55);
  --tagline-line: rgba(107, 74, 58, 0.22);
}

.topbar__left,
.topbar__center,
.topbar__right {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  pointer-events: auto;
}

.topbar__left {
  justify-self: start;
}

.topbar__center {
  justify-self: center;
  gap: 0;
}

.topbar__right {
  justify-self: end;
  gap: 16px;
}

/* ============================================================
 * 主题
 * ============================================================ */
.topbar[data-theme='dusk'] {
  --tagline-sub: rgba(255, 255, 255, 0.7);
  --tagline-line: rgba(255, 255, 255, 0.28);
}

.topbar[data-theme='night'] {
  --tagline-sub: rgba(240, 239, 255, 0.62);
  --tagline-line: rgba(200, 200, 255, 0.24);
}

.topbar[data-theme='midnight'] {
  --tagline-sub: rgba(200, 200, 232, 0.58);
  --tagline-line: rgba(150, 160, 220, 0.2);
}

/* ============================================================
 * 品牌标语
 * ============================================================ */
.brand-tagline {
  position: relative;
  padding-left: 15px;
  font-size: var(--topbar-font-size);
  font-weight: 500;
  letter-spacing: 1px;
  line-height: 1;
  color: var(--tagline-sub);
  white-space: nowrap;
  user-select: none;
  transition: color 0.6s ease;
}

.brand-tagline::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 1px;
  height: 1em;
  transform: translateY(-50%);
  background: var(--tagline-line);
  transition: background 0.6s ease;
}

@keyframes dropInLeft {
  from {
    opacity: 0;
    transform: translateY(-12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.content-layer {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
}

/* ============================================================
 * 响应式
 * ============================================================ */
@media (max-width: 520px) {
  .topbar {
    --topbar-inset-y: 16px;
    --topbar-inset-x: 16px;
    --topbar-font-size: 14px;
    column-gap: 10px;
  }

  .topbar__left,
  .topbar__right {
    gap: 10px;
  }

  .brand-tagline {
    padding-left: 11px;
    letter-spacing: 0.5px;
  }
}

@media (max-width: 380px) {
  .brand-tagline {
    display: none;
  }
}
</style>
