<!-- src/components/AppSidebar.vue -->
<script setup lang="ts">
import { useSidebar } from '@/composables/useSidebar'
import { useTheme } from '@/composables/useTheme'
import router from '@/router'

const { isOpen, close } = useSidebar()
const { theme } = useTheme()

function onSearch() {
  // TODO: 搜索
}

function onNewChat() {
  // TODO: 开启新对话
  close()
  router.push('/')
}
</script>

<template>
  <Teleport to="body">
    <!-- 遮罩 -->
    <Transition name="mask">
      <div v-if="isOpen" class="sidebar-mask" @click="close" />
    </Transition>

    <!-- 抽屉 -->
    <Transition name="panel">
      <aside v-if="isOpen" class="sidebar" :data-theme="theme">
        <!-- 头部：品牌 + 搜索 -->
        <div class="sidebar__head">
          <div class="sidebar__brand">
            <img class="sidebar__logo" src="/flower.png" alt="" />
            <span class="sidebar__name">Emoji心理健康助手</span>
          </div>

          <button
            class="sidebar__search"
            type="button"
            aria-label="搜索"
            data-tip="搜索"
            @click="onSearch"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
              <circle cx="11" cy="11" r="6.5"
                      fill="none" stroke="currentColor" stroke-width="1.8" />
              <line x1="15.8" y1="15.8" x2="20.5" y2="20.5"
                    stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
            </svg>
          </button>
        </div>

        <!-- 开启新对话 -->
        <button class="sidebar__new" type="button" @click="onNewChat">
          <svg viewBox="0 0 24 24" width="16" height="16"
               fill="none" stroke="currentColor"
               stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 20.5a8.5 8.5 0 1 0-7.3-4.1L3.5 20.5l4.3-.9a8.5 8.5 0 0 0 4.2.9z" />
            <path d="M12 8.5v7M8.5 12h7" />
          </svg>
          <span>开启新对话</span>
        </button>

        <!-- 会话列表 -->
        <div class="sidebar__body">
          <section class="sidebar__section">
            <h3 class="sidebar__section-title">置顶</h3>
            <ul class="sidebar__list">
              <li class="sidebar__item">主题切换 logo 调整</li>
              <li class="sidebar__item">项目拷打信息补充</li>
              <li class="sidebar__item">逐句解读情感疑心</li>
            </ul>
          </section>

          <section class="sidebar__section">
            <h3 class="sidebar__section-title">今天</h3>
            <ul class="sidebar__list">
              <li class="sidebar__item is-active">合并视图布局调整</li>
            </ul>
          </section>
        </div>

        <!-- 底部用户 -->
        <div class="sidebar__foot">
          <span class="sidebar__avatar" />
          <span class="sidebar__user">用户</span>
        </div>
      </aside>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ===== 遮罩 ===== */
.sidebar-mask {
  position: fixed;
  inset: 0;
  z-index: 99;
  background: rgba(0, 0, 0, .28);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
}

.mask-enter-active,
.mask-leave-active {
  transition: opacity .3s ease;
}
.mask-enter-from,
.mask-leave-to {
  opacity: 0;
}

/* =========================================================
 * 抽屉
 * ========================================================= */
.sidebar {
  /* 默认：日间 —— 与 HomeView / ChatInput 的暖橙主色保持一致 */
  --sb-bg: rgba(255, 255, 255, .72);
  --sb-border: rgba(255, 255, 255, .6);
  --sb-fg: #4a3a30;
  --sb-fg-sub: rgba(74, 58, 48, .55);
  --sb-hover: rgba(0, 0, 0, .05);
  --sb-active-bg: rgba(255, 154, 118, .18);
  --sb-active-fg: #C25A2E;
  --sb-shadow: 8px 0 32px rgba(0, 0, 0, .08);
  --sb-new-bg: rgba(0, 0, 0, .04);
  --sb-new-hover: rgba(0, 0, 0, .08);

  /* tooltip —— 与 AppHeader 一致，bg 与 day 背景同色系 */
  --tip-bg: rgba(255, 248, 242, .96);
  --tip-fg: #6B4A3A;
  --tip-border: rgba(107, 74, 58, .12);

  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;

  width: 280px;
  display: flex;
  flex-direction: column;
  padding: 20px 14px 16px;

  background: var(--sb-bg);
  border-right: 1px solid var(--sb-border);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: var(--sb-shadow);
  color: var(--sb-fg);

  transition: background .6s ease, border-color .6s ease, color .6s ease;
}

/* ===== 主题覆盖 ===== */
.sidebar[data-theme='dusk'] {
  --sb-bg: rgba(40, 24, 56, .78);
  --sb-border: rgba(255, 255, 255, .14);
  --sb-fg: #ffffff;
  --sb-fg-sub: rgba(255, 255, 255, .55);
  --sb-hover: rgba(255, 255, 255, .08);
  --sb-active-bg: rgba(232, 139, 160, .22);
  --sb-active-fg: #ffffff;
  --sb-shadow: 8px 0 32px rgba(0, 0, 0, .3);
  --sb-new-bg: rgba(255, 255, 255, .08);
  --sb-new-hover: rgba(255, 255, 255, .14);

  --tip-bg: rgba(58, 34, 78, .94);
  --tip-fg: #FFFFFF;
  --tip-border: rgba(255, 255, 255, .14);
}

.sidebar[data-theme='night'] {
  --sb-bg: rgba(12, 16, 36, .8);
  --sb-border: rgba(200, 200, 255, .12);
  --sb-fg: #E8E8FF;
  --sb-fg-sub: rgba(200, 200, 255, .5);
  --sb-hover: rgba(255, 255, 255, .06);
  --sb-active-bg: rgba(140, 160, 255, .18);
  --sb-active-fg: #ffffff;
  --sb-shadow: 8px 0 32px rgba(0, 0, 0, .35);
  --sb-new-bg: rgba(255, 255, 255, .06);
  --sb-new-hover: rgba(255, 255, 255, .12);

  --tip-bg: rgba(18, 24, 54, .94);
  --tip-fg: #E8E8FF;
  --tip-border: rgba(200, 200, 255, .22);
}

.sidebar[data-theme='midnight'] {
  --sb-bg: rgba(6, 8, 20, .85);
  --sb-border: rgba(150, 160, 220, .12);
  --sb-fg: #C8C8E8;
  --sb-fg-sub: rgba(150, 160, 220, .5);
  --sb-hover: rgba(255, 255, 255, .05);
  --sb-active-bg: rgba(120, 140, 255, .16);
  --sb-active-fg: #E8E8FF;
  --sb-shadow: 8px 0 32px rgba(0, 0, 0, .4);
  --sb-new-bg: rgba(255, 255, 255, .05);
  --sb-new-hover: rgba(255, 255, 255, .1);

  --tip-bg: rgba(8, 12, 28, .96);
  --tip-fg: #C8C8E8;
  --tip-border: rgba(150, 160, 220, .18);
}

/* ===== 过渡 ===== */
.panel-enter-active,
.panel-leave-active {
  transition: transform .35s cubic-bezier(.22, 1, .36, 1);
}
.panel-enter-from,
.panel-leave-to {
  transform: translateX(-100%);
}

/* =========================================================
 * 头部
 * ========================================================= */
.sidebar__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 0 8px 16px;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.sidebar__logo {
  width: 26px;
  height: 26px;
  object-fit: contain;
  flex-shrink: 0;
}

.sidebar__name {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: .5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* =========================================================
 * 头部搜索按钮
 * ========================================================= */
.sidebar__search {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: inherit;
  cursor: pointer;
  transition: background .25s ease, color .25s ease, transform .2s ease;
}

.sidebar__search:hover {
  background: var(--sb-hover);
  color: var(--sb-active-fg);
}

.sidebar__search:active {
  transform: scale(.94);
}

/* =========================================================
 * tooltip 气泡：与 AppHeader 完全一致
 * ========================================================= */
.sidebar__search::after {
  content: attr(data-tip);
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translate(-50%, -4px);

  padding: 5px 10px;
  border-radius: 8px;
  background: var(--tip-bg);
  color: var(--tip-fg);
  border: 1px solid var(--tip-border);
  font-size: 12px;
  font-weight: 500;
  line-height: 1.3;
  letter-spacing: .5px;
  white-space: nowrap;

  box-shadow: 0 6px 20px rgba(0, 0, 0, .12);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);

  opacity: 0;
  pointer-events: none;
  transition: opacity .2s ease, transform .2s ease;
  z-index: 20;
}

/* 小三角 */
.sidebar__search::before {
  content: '';
  position: absolute;
  top: calc(100% + 3px);
  left: 50%;
  transform: translate(-50%, -4px);
  border: 5px solid transparent;
  border-bottom-color: var(--tip-bg);
  border-top-width: 0;

  opacity: 0;
  pointer-events: none;
  transition: opacity .2s ease, transform .2s ease;
  z-index: 20;
}

.sidebar__search:hover::after,
.sidebar__search:hover::before,
.sidebar__search:focus-visible::after,
.sidebar__search:focus-visible::before {
  opacity: 1;
  transform: translate(-50%, 0);
}

/* =========================================================
 * 开启新对话
 * ========================================================= */
.sidebar__new {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px 16px;
  border: none;
  border-radius: 999px;
  background: var(--sb-new-bg);
  color: inherit;
  font: inherit;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: .5px;
  cursor: pointer;
  transition: background .25s ease, transform .2s ease;
}

.sidebar__new:hover {
  background: var(--sb-new-hover);
}
.sidebar__new:active {
  transform: scale(.98);
}

/* =========================================================
 * 列表
 * ========================================================= */
.sidebar__body {
  flex: 1;
  overflow-y: auto;
  margin-top: 20px;
  padding: 0 2px;
  scrollbar-width: thin;
}

.sidebar__section + .sidebar__section {
  margin-top: 20px;
}

.sidebar__section-title {
  margin: 0 0 6px;
  padding: 0 10px;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 1px;
  color: var(--sb-fg-sub);
}

.sidebar__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.sidebar__item {
  padding: 9px 10px;
  border-radius: 8px;
  font-size: 13.5px;
  line-height: 1.4;
  cursor: pointer;
  color: inherit;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: background .2s ease, color .2s ease;
}

.sidebar__item:hover {
  background: var(--sb-hover);
}

.sidebar__item.is-active {
  background: var(--sb-active-bg);
  color: var(--sb-active-fg);
  font-weight: 500;
}

/* day 主题：主色渐变，呼应 CTA 按钮（去掉左侧竖线） */
.sidebar[data-theme='day'] .sidebar__item.is-active {
  background: linear-gradient(
    90deg,
    rgba(255, 184, 140, .28),
    rgba(255, 154, 118, .10)
  );
  color: #B04E22;
}

/* =========================================================
 * 底部
 * ========================================================= */
.sidebar__foot {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 8px 4px;
  margin-top: 12px;
  border-top: 1px solid var(--sb-border);
}

.sidebar__avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7aa2ff, #b48bff);
  flex-shrink: 0;
}

.sidebar__user {
  font-size: 13.5px;
  font-weight: 500;
  color: inherit;
}

/* =========================================================
 * 移动端
 * ========================================================= */
@media (max-width: 520px) {
  .sidebar {
    width: 84vw;
    max-width: 300px;
    padding: 16px 12px 12px;
  }
  .sidebar__search {
    width: 28px;
    height: 28px;
  }
  .sidebar__search::after,
  .sidebar__search::before {
    display: none;
  }
}
</style>