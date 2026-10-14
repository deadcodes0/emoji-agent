<!-- src/components/AppHeader.vue -->
<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

const { theme } = useTheme()
</script>

<template>
  <header class="app-header" :data-theme="theme">
    <div class="brand">
      <img class="brand__icon" src="/flower.png" alt="" />
    </div>
  </header>
</template>

<style scoped>
/* =========================================================
 * 主题变量（默认：日间）
 * ========================================================= */
.app-header {
  --hd-fg: #4a3a30;

  display: flex;
  align-items: center;
  gap: 12px;
}

.app-header[data-theme='dusk'] {
  --hd-fg: #ffffff;
}

.app-header[data-theme='night'] {
  --hd-fg: #e8e8ff;
}

.app-header[data-theme='midnight'] {
  --hd-fg: #c8c8e8;
}

/* =========================================================
 * 品牌
 * ========================================================= */
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--hd-fg);
}

.brand__icon {
  width: 28px;
  height: 28px;
  object-fit: contain;
  display: block;
  pointer-events: none;
  user-select: none;
}

@media (max-width: 520px) {
  .app-header {
    gap: 8px;
  }

  .brand__icon {
    width: 24px;
    height: 24px;
  }
}
</style>
