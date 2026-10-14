<!-- src/components/BackButton.vue -->
<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'

withDefaults(
  defineProps<{
    /** plain：无背景无边框（默认，用于顶栏）；glass：玻璃胶囊（保留备用） */
    variant?: 'plain' | 'glass'
  }>(),
  {
    variant: 'plain',
  },
)

const router = useRouter()
const { theme } = useTheme()

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push({ name: 'home' })
}
</script>

<template>
  <button
    class="back-btn"
    :class="`back-btn--${variant}`"
    type="button"
    :data-theme="theme"
    @click="goBack"
  >
    <span class="back-btn__arrow">←</span>
    <span class="back-btn__text">返回</span>
  </button>
</template>

<style scoped>
/* ============================================================
 * 公共
 * ============================================================ */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.6px;
  line-height: 1;
  cursor: pointer;
  transition: color 0.4s ease, opacity 0.25s ease, transform 0.2s ease;
}

.back-btn:active {
  transform: scale(0.97);
}

.back-btn__arrow {
  font-size: 14px;
  line-height: 1;
  transition: transform 0.2s ease;
}

.back-btn:hover .back-btn__arrow {
  transform: translateX(-2px);
}

.back-btn__text {
  line-height: 1;
}

/* ============================================================
 * plain：纯文字 + 箭头，无背景无边框
 * ============================================================ */
.back-btn--plain {
  color: var(--tagline-sub, #6b4a3a);
  height: 24px;
}

.back-btn--plain:hover {
  color: var(--tagline-hover, #3a2419);
  opacity: 1;
}

/* 四套主题兜底（脱离 topbar 单独用时也能看清） */
.back-btn--plain[data-theme='day'] {
  --tagline-sub: rgba(107, 74, 58, 0.85);
  --tagline-hover: #3a2419;
}

.back-btn--plain[data-theme='dusk'] {
  --tagline-sub: rgba(255, 255, 255, 0.85);
  --tagline-hover: #ffffff;
}

.back-btn--plain[data-theme='night'] {
  --tagline-sub: rgba(232, 232, 255, 0.78);
  --tagline-hover: #ffffff;
}

.back-btn--plain[data-theme='midnight'] {
  --tagline-sub: rgba(200, 200, 232, 0.72);
  --tagline-hover: #e8e8ff;
}

/* ============================================================
 * glass：玻璃胶囊（保留，显式指定 variant="glass" 时才用）
 * ============================================================ */
.back-btn--glass {
  height: 32px;
  padding: 0 14px 0 11px;
  border-radius: 999px;

  --bb-fg: #4a3a30;
  --bb-fg-hover: #1a1a2a;
  --bb-glass-bg: rgba(255, 255, 255, 0.55);
  --bb-glass-border: rgba(255, 255, 255, 0.7);
  --bb-shadow: 0 8px 28px rgba(0, 0, 0, 0.08);

  border: 1px solid var(--bb-glass-border);
  background: var(--bb-glass-bg);
  color: var(--bb-fg);
  backdrop-filter: blur(10px) saturate(120%);
  -webkit-backdrop-filter: blur(10px) saturate(120%);
  box-shadow: var(--bb-shadow);
}

.back-btn--glass:hover {
  color: var(--bb-fg-hover);
  transform: translateY(-1px);
}

.back-btn--glass[data-theme='dusk'] {
  --bb-fg: #ffffff;
  --bb-fg-hover: #ffffff;
  --bb-glass-bg: rgba(255, 255, 255, 0.16);
  --bb-glass-border: rgba(255, 255, 255, 0.28);
  --bb-shadow: 0 8px 28px rgba(60, 30, 80, 0.18);
}

.back-btn--glass[data-theme='night'] {
  --bb-fg: #e8e8ff;
  --bb-fg-hover: #ffffff;
  --bb-glass-bg: rgba(255, 255, 255, 0.06);
  --bb-glass-border: rgba(200, 200, 255, 0.22);
  --bb-shadow: 0 0 24px rgba(120, 140, 255, 0.12);
}

.back-btn--glass[data-theme='midnight'] {
  --bb-fg: #c8c8e8;
  --bb-fg-hover: #e8e8ff;
  --bb-glass-bg: rgba(255, 255, 255, 0.04);
  --bb-glass-border: rgba(150, 160, 220, 0.18);
  --bb-shadow: 0 0 20px rgba(100, 120, 220, 0.1);
}

@media (max-width: 520px) {
  .back-btn--glass {
    height: 28px;
    padding: 0 11px 0 9px;
    font-size: 12px;
  }

  .back-btn--plain {
    font-size: 12px;
  }
}
</style>
