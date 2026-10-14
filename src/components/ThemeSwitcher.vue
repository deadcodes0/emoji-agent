<!-- src/components/ThemeSwitcher.vue -->
<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
import { getGif } from '@/composables/useGif'
import { THEMES, THEME_ORDER, type Theme } from '@/config/themes'

const { theme, isManual, setThemeManual, setThemeAuto } = useTheme()

function onPick(t: Theme) {
  setThemeManual(t)
}
</script>

<template>
  <nav class="theme-switcher">
    <button class="pill" :class="{ on: !isManual }" @click="setThemeAuto">
      自动
    </button>

    <span class="divider" />

    <button
      v-for="t in THEME_ORDER"
      :key="t"
      class="pill"
      :class="{ on: isManual && theme === t }"
      @click="onPick(t)"
    >
      <img class="pill__logo" :src="getGif(THEMES[t].icon)" alt="" />
      <span>{{ THEMES[t].label }}</span>
    </button>
  </nav>
</template>

<style scoped>
/* ====== 胶囊容器（相对定位，由父级 topbar 居中） ====== */
.theme-switcher {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, .16);
  border: 1px solid rgba(255, 255, 255, .28);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: 0 8px 28px rgba(0, 0, 0, .12);
}

/* ====== 胶囊按钮 ====== */
.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 999px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, .82);
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 1px;
  cursor: pointer;
  transition:
    background .3s ease,
    color .3s ease,
    transform .2s ease,
    box-shadow .3s ease;
}

.pill:hover {
  background: rgba(255, 255, 255, .18);
  color: #fff;
  transform: translateY(-1px);
}

.pill:active {
  transform: translateY(0) scale(.97);
}

.pill.on {
  background: #fff;
  color: #2a2a3a;
  box-shadow: 0 4px 14px rgba(0, 0, 0, .18);
}

.pill.on:hover {
  background: #fff;
  color: #2a2a3a;
}

/* ====== gif logo ====== */
.pill__logo {
  width: 18px;
  height: 18px;
  object-fit: contain;
  display: block;
  pointer-events: none;
  user-select: none;
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, .08));
}

/* ====== 分隔线 ====== */
.divider {
  width: 1px;
  height: 18px;
  background: rgba(255, 255, 255, .25);
  margin: 0 4px;
}

/* ====== 夜间 / 深夜微调 ====== */
:global(html[data-theme='night']) .theme-switcher,
:global(html[data-theme='midnight']) .theme-switcher {
  background: rgba(255, 255, 255, .08);
  border-color: rgba(200, 200, 255, .18);
}

:global(html[data-theme='night']) .pill,
:global(html[data-theme='midnight']) .pill {
  color: rgba(220, 220, 255, .75);
}

/* ====== 移动端 ====== */
@media (max-width: 520px) {
  .theme-switcher {
    padding: 4px;
    gap: 2px;
  }

  .pill {
    padding: 7px 12px;
    font-size: 12px;
    letter-spacing: 0;
  }

  .pill__logo {
    width: 16px;
    height: 16px;
  }

  .divider {
    display: none;
  }
}
</style>
