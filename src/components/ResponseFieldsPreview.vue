<!-- src/components/ResponseFieldsPreview.vue -->
<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'

const { theme } = useTheme()
const router = useRouter()

function goDiary() {
  router.push({ name: 'diary' })
}
</script>

<template>
  <button
    class="diary-entry"
    type="button"
    :data-theme="theme"
    @click="goDiary"
  >
    情绪日志
  </button>
</template>

<style scoped>
/* =========================================================
 * 情绪日志入口：纯文字，位于 brand 右侧
 * 文字风格参考 DiaryToolbar 的 h1
 * ========================================================= */
.diary-entry {
  /* 主题变量（默认：日间）—— 与 AppHeader 同色系 */
  --p-text: #4a3a30;

  padding: 0;
  border: none;
  background: transparent;

  color: var(--p-text);
  font-family: inherit;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1.2px;
  line-height: 1;

  cursor: pointer;

  transition: color .6s ease, opacity .2s ease;
}

/* =========================================================
 * 主题覆盖
 * ========================================================= */
.diary-entry[data-theme='dusk'] {
  --p-text: #ffffff;
}

.diary-entry[data-theme='night'] {
  --p-text: #E8E8FF;
}

.diary-entry[data-theme='midnight'] {
  --p-text: #C8C8E8;
}

/* =========================================================
 * 交互
 * ========================================================= */
.diary-entry:hover {
  opacity: .75;
}

.diary-entry:active {
  opacity: .55;
}

.diary-entry:focus-visible {
  outline: 2px solid currentColor;
  outline-offset: 4px;
  border-radius: 4px;
}

/* =========================================================
 * 窄屏
 * ========================================================= */
@media (max-width: 520px) {
  .diary-entry {
    font-size: 13px;
    letter-spacing: 1px;
  }
}
</style>
