<!-- src/views/EmotionDiaryView.vue -->
<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DiaryToolbar from '@/components/diary/DiaryToolbar.vue'
import DiaryCalendar from '@/components/diary/DiaryCalendar.vue'
import DiaryStream from '@/components/diary/DiaryStream.vue'
import DiaryDetailPanel from '@/components/diary/DiaryDetailPanel.vue'
import ChatInput from '@/components/ChatInput.vue'
import SentenceStage from '@/components/SentenceStage.vue'
import { useDiaryStore } from '@/stores/diary'
import { useTheme } from '@/composables/useTheme'
import { useChatStream } from '@/composables/useChatStream'
import { formatDate, pad } from '@/utils/diary'
import { isMobile } from '@/utils/device'
import { saveTurn } from '@/utils/diaryApi'
import type { Turn, RiskLevel } from '@/types/diary'
import type { ChatHistoryItem } from '@/types/chat'

const store = useDiaryStore()
const { theme } = useTheme()
const route = useRoute()
const router = useRouter()

const {
  streamingMode,
  sentences,
  meta,
  errorOccurred,
  busy,
  isStreaming,
  askBot,
  resumeIfAny,
  onStageComplete,
  stopStreaming,
  onFinish,
} = useChatStream()

const draft = ref('')

/** 最近 3 轮历史，供 LLM 理解上下文 */
function buildHistory(): ChatHistoryItem[] {
  const day = store.activeDay.value
  if (!day) return []
  return day.turns.slice(-3).flatMap((t) => {
    const items: ChatHistoryItem[] = []
    if (t.userMsg) items.push({ role: 'user', content: t.userMsg })
    if (t.reply) items.push({ role: 'assistant', content: t.reply })
    return items
  })
}

function onSend(text: string) {
  if (busy.value) return
  draft.value = ''
  void askBot(text, buildHistory())
}

/** 异常兜底：done 缺失时的保守值 */
const FALLBACK_EMOTION = '未知'
const FALLBACK_RISK: RiskLevel = 'MEDIUM'

onFinish(async ({ userMsg, reply, meta: done }) => {
  const now = new Date()
  const dateKey = formatDate(now)
  const time = `${pad(now.getHours())}:${pad(now.getMinutes())}`

  const turn: Turn = {
    time,
    userMsg: userMsg || '（继续上次对话）',
    reply,
    emotion: done?.userEmotionLabel ?? FALLBACK_EMOTION,
    riskLevel: done?.riskLevel ?? FALLBACK_RISK,
    suggestions: done?.suggestions ?? [],
  }

  // 1) 本地先显示（乐观更新，UI 不卡；appendTurn 会自动选中新轮次 → 三卡片刷新）
  store.appendTurn(dateKey, turn)

  // 2) 异步落库；失败只 log，不影响本地展示
  try {
    await saveTurn({
      date: dateKey,
      time: turn.time,
      userMsg: turn.userMsg,
      reply: turn.reply,
      emotion: turn.emotion,
      riskLevel: turn.riskLevel,
      suggestions: turn.suggestions,
    })
  } catch (err) {
    console.error('[diary] 保存失败:', err)
  }
})

// 切到某天时按需拉取（当天已拉过则跳过）
watch(
  () => store.activeDate.value,
  (dateKey) => {
    void store.loadDay(dateKey)
  },
)

// 切换年份/月份时拉取整月，为日历填真实色点
watch(
  () => [store.currentYear.value, store.currentMonth.value] as const,
  ([y, m]) => {
    void store.loadMonth(y, m)
  },
)

function onResize() {
  if (!isMobile()) store.closePanel()
}

onMounted(async () => {
  window.addEventListener('resize', onResize)
  store.calendarCollapsed.value = true

  // 先整月预取：activeDate 一定落在当前月，所以不用再单独 loadDay
  await store.loadMonth(store.currentYear.value, store.currentMonth.value)

  const q = route.query.q

  if (typeof q === 'string' && q.trim()) {
    const text = q.trim()
    const rest = { ...route.query }
    delete rest.q
    router.replace({ query: rest })
    await askBot(text, buildHistory())
  } else {
    void resumeIfAny()
  }
})

onUnmounted(() => window.removeEventListener('resize', onResize))
</script>

<template>
  <div class="emotion-diary-page" :data-theme="theme">
    <div class="bg-layer" />

    <div class="diary-layout">
      <div class="diary-layout__body">
        <div class="diary-layout__content">
          <header class="diary-layout__header">
            <div class="diary-layout__header-left">
              <!-- 品牌 / 返回按钮由 App.vue 顶栏统一提供，此处只保留工具栏 -->
              <DiaryToolbar />
            </div>
            <DiaryCalendar />
          </header>

          <main class="diary-layout__main">
            <DiaryStream />

            <Transition name="stage">
              <div v-if="streamingMode" class="diary-stage">
                <SentenceStage :sentences="sentences" :streaming="isStreaming" :error="errorOccurred"
                               @complete="onStageComplete" />
              </div>
            </Transition>
          </main>

          <footer class="diary-layout__foot">
            <div class="diary-layout__foot-inner">
              <ChatInput v-model="draft" placeholder="对小家伙说句话…" :maxlength="120" :disabled="busy" :streaming="busy"
                         @send="onSend" @stop="stopStreaming" />
            </div>
          </footer>
        </div>

        <aside class="diary-layout__aside">
          <DiaryDetailPanel :analyzing="streamingMode" :live-meta="meta" />
        </aside>
      </div>
    </div>

    <div class="panel-mask" :class="{ 'is-open': store.panelOpen.value }" @click="store.closePanel" />
  </div>
</template>

<style scoped>
/* ============================================================
 * 基础
 * ============================================================ */
.emotion-diary-page {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC',
  'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
}

.emotion-diary-page *,
.emotion-diary-page *::before,
.emotion-diary-page *::after {
  box-sizing: border-box;
}

/* ============================================================
 * 主题变量
 * ============================================================ */
.emotion-diary-page {
  --text: #5a3d2e;
  --text-sub: #8a6650;
  --text-dim: rgba(90, 61, 46, 0.5);
  --panel-bg: rgba(255, 255, 255, 0.62);
  --panel-border: rgba(255, 255, 255, 0.78);
  --panel-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  --card-bg: rgba(255, 255, 255, 0.52);
  --card-border: rgba(255, 255, 255, 0.7);
  --card-hover: rgba(255, 255, 255, 0.78);
  --card-active: rgba(255, 255, 255, 0.9);
  --user-bg: linear-gradient(135deg, #ffb88c, #ff9a76);
  --user-text: #ffffff;
  --idx-bg: rgba(255, 255, 255, 0.7);
  --scroll: rgba(107, 74, 58, 0.16);

  --risk-bg-alpha: 0.14;
  --risk-border-alpha: 0.42;
  --risk-text-glow: none;

  --text-shadow: none;
  --panel-text-shadow: none;
  --tag-base: rgba(255, 255, 255, 0.6);
  --tag-tint: 16%;
  --tag-border-strength: 55%;
  --tag-text-shadow: none;

  --c-bot-bg: rgba(255, 255, 255, 0.72);
  --c-bot-border: rgba(255, 255, 255, 0.85);
  --c-bot-text: #4a3a30;
  --c-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  --bubble-text-shadow: none;

  --stage-blur: blur(18px) saturate(115%) brightness(1.02);
  --stage-veil: rgba(255, 255, 255, 0.06);
}

.emotion-diary-page[data-theme='dusk'] {
  --text: #ffffff;
  --text-sub: rgba(255, 255, 255, 0.92);
  --text-dim: rgba(255, 255, 255, 0.66);
  --panel-bg: rgba(255, 255, 255, 0.16);
  --panel-border: rgba(255, 255, 255, 0.32);
  --panel-shadow: 0 8px 24px rgba(60, 30, 80, 0.22);
  --card-bg: rgba(255, 255, 255, 0.14);
  --card-border: rgba(255, 255, 255, 0.32);
  --card-hover: rgba(255, 255, 255, 0.22);
  --card-active: rgba(255, 255, 255, 0.28);
  --user-bg: linear-gradient(135deg, #e88ba0, #a87ad8);
  --user-text: #ffffff;
  --idx-bg: rgba(255, 255, 255, 0.24);
  --scroll: rgba(255, 255, 255, 0.22);

  --text-shadow: 0 1px 2px rgba(40, 0, 60, 0.5);
  --panel-text-shadow: 0 1px 3px rgba(40, 0, 60, 0.55);
  --tag-base: rgba(35, 12, 45, 0.55);
  --tag-tint: 30%;
  --tag-border-strength: 75%;
  --tag-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.55);

  --c-bot-bg: rgba(30, 12, 50, 0.55);
  --c-bot-border: rgba(255, 255, 255, 0.42);
  --c-bot-text: #ffffff;
  --c-shadow: 0 8px 28px rgba(40, 0, 60, 0.35);
  --bubble-text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);

  --stage-blur: blur(20px) saturate(120%) brightness(0.98);
  --stage-veil: rgba(40, 10, 55, 0.06);
}

.emotion-diary-page[data-theme='night'] {
  --text: #f4f4ff;
  --text-sub: #c0c0e0;
  --text-dim: rgba(210, 210, 255, 0.52);
  --panel-bg: rgba(255, 255, 255, 0.07);
  --panel-border: rgba(200, 200, 255, 0.18);
  --panel-shadow: 0 0 24px rgba(120, 140, 255, 0.14);
  --card-bg: rgba(255, 255, 255, 0.06);
  --card-border: rgba(200, 200, 255, 0.18);
  --card-hover: rgba(255, 255, 255, 0.11);
  --card-active: rgba(255, 255, 255, 0.15);
  --user-bg: linear-gradient(135deg, rgba(140, 160, 255, 0.34), rgba(180, 140, 255, 0.28));
  --user-text: #f0efff;
  --idx-bg: rgba(200, 200, 255, 0.14);
  --scroll: rgba(200, 200, 255, 0.18);

  --risk-bg-alpha: 0.18;
  --risk-border-alpha: 0.52;

  --text-shadow: none;
  --panel-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.45);
  --tag-base: rgba(0, 0, 20, 0.48);
  --tag-tint: 26%;
  --tag-border-strength: 66%;
  --tag-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);

  --c-bot-bg: rgba(20, 22, 55, 0.55);
  --c-bot-border: rgba(200, 200, 255, 0.3);
  --c-bot-text: #f0efff;
  --c-shadow: 0 8px 28px rgba(0, 0, 30, 0.4);
  --bubble-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);

  --stage-blur: blur(22px) saturate(115%) brightness(0.9);
  --stage-veil: rgba(10, 10, 40, 0.1);
}

.emotion-diary-page[data-theme='midnight'] {
  --text: #d4d4ee;
  --text-sub: #9090b8;
  --text-dim: rgba(160, 170, 230, 0.45);
  --panel-bg: rgba(255, 255, 255, 0.045);
  --panel-border: rgba(150, 160, 220, 0.16);
  --panel-shadow: 0 0 20px rgba(100, 120, 220, 0.1);
  --card-bg: rgba(255, 255, 255, 0.04);
  --card-border: rgba(150, 160, 220, 0.16);
  --card-hover: rgba(255, 255, 255, 0.08);
  --card-active: rgba(255, 255, 255, 0.11);
  --user-bg: linear-gradient(135deg, rgba(120, 140, 255, 0.26), rgba(150, 120, 220, 0.22));
  --user-text: #e0e0ff;
  --idx-bg: rgba(150, 160, 220, 0.14);
  --scroll: rgba(150, 160, 220, 0.16);

  --risk-bg-alpha: 0.18;
  --risk-border-alpha: 0.52;

  --text-shadow: none;
  --panel-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  --tag-base: rgba(0, 0, 15, 0.55);
  --tag-tint: 28%;
  --tag-border-strength: 70%;
  --tag-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.55);

  --c-bot-bg: rgba(10, 10, 26, 0.62);
  --c-bot-border: rgba(150, 160, 220, 0.28);
  --c-bot-text: #d4d4ee;
  --c-shadow: 0 8px 24px rgba(0, 0, 20, 0.55);
  --bubble-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.55);

  --stage-blur: blur(24px) saturate(110%) brightness(0.82);
  --stage-veil: rgba(0, 0, 15, 0.14);
}

/* ============================================================
 * 背景
 * ============================================================ */
.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  transition: background 1.2s ease;
}

.emotion-diary-page[data-theme='day'] .bg-layer {
  background: linear-gradient(135deg, #fff5e6 0%, #ffe8d6 50%, #ffd9c0 100%);
}

.emotion-diary-page[data-theme='dusk'] .bg-layer {
  background: linear-gradient(180deg,
  #ffc48c 0%,
  #ff9a8b 22%,
  #e88ba0 45%,
  #b87ab0 68%,
  #7a6ba8 88%,
  #5a5a8c 100%);
}

.emotion-diary-page[data-theme='night'] .bg-layer {
  background: radial-gradient(ellipse at 50% 0%, #2b2d5e 0%, #1a1a3e 40%, #0d0d24 100%);
}

.emotion-diary-page[data-theme='midnight'] .bg-layer {
  background: radial-gradient(ellipse at 50% 100%, #1a1a3e 0%, #0d0d24 45%, #050510 100%);
}

/* ============================================================
 * 布局
 * ============================================================ */
.diary-layout {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
}

.diary-layout__body {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 100%;
}

.diary-layout__content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  min-height: 0;
}

/* ★ 顶部 padding 让位于 App.vue 的固定顶栏（品牌图标 + 标语） */
.diary-layout__header {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  flex: 0 0 auto;
  padding: 72px 22px 12px;
  background: transparent;
  border-bottom: none;
  transition: background 0.6s ease, border-color 0.6s ease;
  position: relative;
  z-index: 10;
}

.diary-layout__header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
  padding-top: 4px;
}

.diary-layout__main {
  position: relative;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.diary-layout__foot {
  flex: 0 0 auto;
  display: flex;
  justify-content: center;
  padding: 8px 22px 20px;
}

.diary-layout__foot-inner {
  width: min(640px, 100%);
}

/* ============================================================
 * 流式舞台浮层
 * ============================================================ */
.diary-stage {
  position: absolute;
  inset: 0;
  z-index: 5;
  display: flex;
  flex-direction: column;
  background: var(--stage-veil, transparent);
  backdrop-filter: var(--stage-blur, blur(18px) saturate(115%));
  -webkit-backdrop-filter: var(--stage-blur, blur(18px) saturate(115%));
  transition: backdrop-filter 0.5s ease, background 0.5s ease;
}

.diary-layout__aside {
  flex: 0 0 auto;
  width: 380px;
  height: 100%;
  min-height: 0;
}

@media (min-width: 961px) {
  .diary-layout__aside :deep(.panel) {
    background: transparent;
    border-left: none;
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
  }
}

/* ============================================================
 * 移动端遮罩
 * ============================================================ */
.panel-mask {
  position: fixed;
  inset: 0;
  z-index: 20;
  background: rgba(0, 0, 0, 0.32);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.panel-mask.is-open {
  opacity: 1;
  pointer-events: auto;
}

@media (min-width: 961px) {
  .panel-mask {
    display: none;
  }
}

@media (max-width: 960px) {
  .diary-layout__body {
    flex-direction: column;
  }

  .diary-layout__header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .diary-layout__header-left {
    flex: 0 0 auto;
    padding-top: 0;
  }

  .diary-layout__aside {
    width: 100%;
  }
}

@media (max-width: 520px) {
  /* 移动端 App.vue 顶栏更小（top:16px），相应减小让位空间 */
  .diary-layout__header {
    padding: 60px 16px 10px;
  }

  .diary-layout__foot {
    padding: 6px 16px 14px;
  }
}

@media (prefers-reduced-motion: reduce) {

  .emotion-diary-page *,
  .emotion-diary-page *::before,
  .emotion-diary-page *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }

}
</style>
