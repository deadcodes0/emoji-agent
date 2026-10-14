<!-- src/views/HomeView.vue -->
<script setup lang="ts">
import { computed, ref } from 'vue'
import Emoji from '@/components/EmojiGif.vue'
import ChatInput from '@/components/ChatInput.vue'
import { useTheme } from '@/composables/useTheme'
import { THEMES, THEME_ORDER } from '@/config/themes'
import { getGif } from '@/composables/useGif'
import { useAuth } from '@/composables/useAuth'
import router from '@/router'

/* ---------------- 主题 ---------------- */
const { theme } = useTheme()
const meta = computed(() => THEMES[theme.value])

const THEME_ICONS = ['☀️', '👍', '🌙', '💤']
const currentIcon = computed(() => {
  const idx = THEME_ORDER.indexOf(theme.value)
  return THEME_ICONS[idx] ?? '⭐'
})

const currentGif = computed(() => getGif(meta.value.icon))

/* ---------------- 预加载 ---------------- */
Object.values(THEMES).forEach((m) => {
  const img = new Image()
  img.src = getGif(m.icon)
})
const huggingImg = new Image()
huggingImg.src = getGif('hugging')

/* ---------------- 登录状态 ---------------- */
const { isLoggedIn } = useAuth()

function goLogin() {
  router.push({ name: 'login' })
}

function goRegister() {
  router.push({ name: 'login', query: { mode: 'register' } })
}

/* ---------------- 输入框状态 ---------------- */
const isInputFocused = ref(false)
const message = ref('')

function onSend(text: string) {
  router.push({ name: 'diary', query: { q: text } })
}
</script>

<template>
  <div class="home" :data-theme="theme">
    <main class="content">
      <!-- 标题 / 副标题 -->
      <h1 class="title">{{ meta.title }}</h1>
      <p class="subtitle">{{ meta.sub }}</p>

      <!-- 中间：EmojiGif -->
      <div class="stage">
        <Emoji
          :src="isLoggedIn && isInputFocused ? getGif('hugging') : currentGif"
          clickable
          :icon="currentIcon"
        />
      </div>

      <!-- 底部：未登录 → 登录 / 注册；已登录 → ChatInput -->
      <div class="bottom-slot">
        <template v-if="isLoggedIn">
          <ChatInput
            v-model="message"
            placeholder="对小家伙说句话…"
            :maxlength="120"
            @focus="isInputFocused = true"
            @blur="isInputFocused = false"
            @send="onSend"
          />
        </template>

        <template v-else>
          <p class="auth-hint">登录后就能和小家伙聊天啦</p>
          <div class="auth-actions">
            <button type="button" class="auth-btn" @click="goLogin">登录</button>
            <button type="button" class="auth-btn" @click="goRegister">注册</button>
          </div>
        </template>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* ==================== 主题变量 ==================== */
.home {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;

  --text: #6B4A3A;
  --text-sub: #9C7A68;

  /* 透明玻璃按钮（白天） */
  --btn-fg: #5a3d2e;
  --btn-fg-hover: #3a2419;
  --btn-bg: rgba(255, 255, 255, 0.10);
  --btn-bg-hover: rgba(255, 255, 255, 0.28);
  --btn-border: rgba(255, 255, 255, 0.75);
  --btn-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  --btn-shadow-hover: 0 10px 26px rgba(0, 0, 0, 0.12);
}

.home[data-theme='dusk'] {
  --text: #FFFFFF;
  --text-sub: rgba(255, 255, 255, .88);

  --btn-fg: #ffffff;
  --btn-fg-hover: #ffffff;
  --btn-bg: rgba(255, 255, 255, 0.10);
  --btn-bg-hover: rgba(255, 255, 255, 0.24);
  --btn-border: rgba(255, 255, 255, 0.42);
  --btn-shadow: 0 6px 20px rgba(60, 30, 80, 0.16);
  --btn-shadow-hover: 0 10px 28px rgba(60, 30, 80, 0.24);
}

.home[data-theme='night'] {
  --text: #F0EFFF;
  --text-sub: #A8A8D0;

  --btn-fg: #e8e8ff;
  --btn-fg-hover: #ffffff;
  --btn-bg: rgba(255, 255, 255, 0.05);
  --btn-bg-hover: rgba(255, 255, 255, 0.13);
  --btn-border: rgba(200, 200, 255, 0.28);
  --btn-shadow: 0 0 22px rgba(120, 140, 255, 0.10);
  --btn-shadow-hover: 0 0 30px rgba(120, 140, 255, 0.20);
}

.home[data-theme='midnight'] {
  --text: #C8C8E8;
  --text-sub: #7A7A9C;

  --btn-fg: #c8c8e8;
  --btn-fg-hover: #e8e8ff;
  --btn-bg: rgba(255, 255, 255, 0.035);
  --btn-bg-hover: rgba(255, 255, 255, 0.10);
  --btn-border: rgba(150, 160, 220, 0.22);
  --btn-shadow: 0 0 20px rgba(100, 120, 220, 0.08);
  --btn-shadow-hover: 0 0 26px rgba(100, 120, 220, 0.18);
}

/* ==================== 内容层 ==================== */
.content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 24px;
  box-sizing: border-box;
}

/* ==================== 标题 / 副标题 ==================== */
.title {
  margin: 0 0 16px;
  font-size: clamp(28px, 5vw, 48px);
  color: var(--text);
  letter-spacing: 2px;
  font-weight: 600;
  transition: color .8s ease, text-shadow .8s ease;
}

.home[data-theme='night'] .title,
.home[data-theme='midnight'] .title {
  text-shadow: 0 0 26px rgba(150, 170, 255, .35);
}

.subtitle {
  margin: 0 0 24px;
  font-size: clamp(14px, 2vw, 18px);
  color: var(--text-sub);
  letter-spacing: 1px;
  transition: color .8s ease;
}

/* ==================== 中间 Emoji 舞台 ==================== */
.stage {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
}

/* ==================== 底部插槽 ==================== */
.bottom-slot {
  width: min(420px, 100%);
  pointer-events: auto;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
}

/* ==================== 登录 / 注册：透明玻璃框 ==================== */
.auth-hint {
  margin: 0;
  font-size: 13px;
  letter-spacing: .5px;
  color: var(--text-sub);
  transition: color .8s ease;
}

.auth-actions {
  display: flex;
  gap: 12px;
}

.auth-btn {
  flex: 1 1 0;
  min-width: 0;
  height: 48px;
  padding: 0 16px;
  border-radius: 999px;
  font-family: inherit;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1.5px;
  line-height: 1;
  cursor: pointer;

  border: 1px solid var(--btn-border);
  background: var(--btn-bg);
  color: var(--btn-fg);
  backdrop-filter: blur(14px) saturate(120%);
  -webkit-backdrop-filter: blur(14px) saturate(120%);
  box-shadow: var(--btn-shadow);

  transition: background-color .3s ease, border-color .3s ease,
  color .3s ease, transform .2s ease, box-shadow .3s ease;
}

.auth-btn:hover {
  background: var(--btn-bg-hover);
  color: var(--btn-fg-hover);
  transform: translateY(-1px);
  box-shadow: var(--btn-shadow-hover);
}

.auth-btn:active {
  transform: translateY(0) scale(.98);
}

.auth-btn:focus-visible {
  outline: 2px solid var(--btn-fg);
  outline-offset: 3px;
}

/* ==================== 移动端 ==================== */
@media (max-width: 520px) {
  .title {
    margin-bottom: 12px;
  }

  .subtitle {
    margin-bottom: 18px;
  }

  .stage {
    margin-bottom: 22px;
  }

  .auth-btn {
    height: 44px;
    font-size: 14px;
    letter-spacing: 1.2px;
  }
}
</style>
