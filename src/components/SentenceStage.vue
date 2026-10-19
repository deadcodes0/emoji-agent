<!-- src/components/SentenceStage.vue -->
<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import EmojiGif from '@/components/EmojiGif.vue'
import { getGif } from '@/composables/useGif'
import { aiEmotionEmoji } from '@/config/aiEmotion'
import type { SentencePayload } from '@/types/chat'

/**
 * 逐句流式舞台：
 * - 收到一句播一句（打字机 + 停顿 + 句间淡出），像真人说话
 * - 每句切换舞台表情（按 aiEmotion 映射 gif），气泡左侧带情绪符号
 * - 无句子时 thinking，全程无回应且出错时 injured + 占位气泡
 * - 队列播完且流已关闭 -> emit('complete')
 */
const props = withDefaults(
  defineProps<{
    sentences: SentencePayload[]
    /** SSE 流是否仍在进行（false 时队列耗尽即结束） */
    streaming: boolean
    error?: boolean
    charDelayMs?: number
    pauseMs?: number
  }>(),
  {
    error: false,
    charDelayMs: 80,
    pauseMs: 620,
  },
)

const emit = defineEmits<{ complete: [] }>()

const displayed = ref('')
const bubbleVisible = ref(false)
const typing = ref(false)
/** 当前正在播报句子的 aiEmotion；null = thinking / injured */
const currentEmojiKey = ref<string | null>(null)

/** 每次挂载 / 卸载时自增，用于作废上一次 run 循环 */
let currentRun = 0

const sleep = (ms: number) => new Promise<void>((r) => setTimeout(r, ms))

/* ---------------- 表情舞台素材 ---------------- */
const failed = computed(() => props.error && props.sentences.length === 0)

const mascotSrc = computed(() => {
  if (failed.value) return getGif('injured')
  const e = currentEmojiKey.value
  if (!e) return getGif('thinking')
  return getGif(e)
})

const overlays = computed(() => {
  if (failed.value) {
    return { tl: getGif('x_mark'), tr: getGif('pill') }
  }
  if (!currentEmojiKey.value) {
    return { tl: getGif('stacked_book'), tr: getGif('bubble') }
  }
  return {}
})

const emotionEmoji = computed(() => {
  const e = currentEmojiKey.value
  return e ? aiEmotionEmoji(e) : null
})

/* ---------------- 播报循环 ---------------- */
async function typeText(text: string, id: number) {
  displayed.value = ''
  typing.value = true
  bubbleVisible.value = true
  for (let i = 0; i < text.length; i++) {
    if (currentRun !== id) return
    displayed.value = text.slice(0, i + 1)
    await sleep(props.charDelayMs)
  }
  typing.value = false
}

async function run(id: number) {
  let idx = 0

  while (currentRun === id) {
    // 1) 等待下一句（流仍在进行时轮询；关闭后立即结束等待）
    while (currentRun === id && props.sentences.length <= idx && props.streaming) {
      await sleep(30)
    }
    if (currentRun !== id) return

    // 2) 有下一句 -> 打字 + 停顿 + 淡出
    if (props.sentences.length > idx) {
      const s = props.sentences[idx]!
      currentEmojiKey.value = s.emoji
      await typeText(s.reply, id)
      if (currentRun !== id) return
      await sleep(props.pauseMs)
      if (currentRun !== id) return
      bubbleVisible.value = false
      await sleep(320) // 与 CSS transition 时长一致
      if (currentRun !== id) return
      displayed.value = ''
      idx++
      continue
    }

    // 3) 队列已空且流已关闭
    if (idx === 0 && props.error) {
      // 全程无回应且出错：播一条占位气泡
      currentEmojiKey.value = null
      await typeText('（我好像断线了…）', id)
      if (currentRun !== id) return
      await sleep(1200)
      if (currentRun !== id) return
      bubbleVisible.value = false
      await sleep(320)
    }
    break
  }

  if (currentRun === id) emit('complete')
}

onMounted(() => {
  const id = ++currentRun
  void run(id)
})

onUnmounted(() => {
  currentRun++
})
</script>

<template>
  <div class="stage">
    <!-- 头顶气泡：左侧情绪符号 + 打字机文本 -->
    <div class="stage__bubble" :class="{ 'stage__bubble--hidden': !bubbleVisible }">
      <span v-if="emotionEmoji" class="stage__emotion">{{ emotionEmoji }}</span>
      <span class="stage__text">{{ displayed }}<span v-if="typing" class="stage__cursor" /></span>
    </div>

    <!-- 表情舞台 -->
    <div class="stage__emoji">
      <EmojiGif :src="mascotSrc" :images="overlays" :inset="20" />
    </div>
  </div>
</template>

<style scoped>
.stage {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 22px;
  padding: 24px;
  box-sizing: border-box;
}

/* ---- 气泡 ---- */
.stage__bubble {
  position: relative;
  max-width: min(420px, 100%);
  min-height: 58px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  border-radius: 20px;
  background: var(--c-bot-bg, rgba(255, 255, 255, 0.62));
  border: 1px solid var(--c-bot-border, rgba(255, 255, 255, 0.78));
  color: var(--c-bot-text, #4a3a30);
  font-size: 15px;
  line-height: 1.65;
  letter-spacing: 0.3px;
  word-break: break-word;
  text-align: left;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: var(--c-shadow, 0 8px 24px rgba(0, 0, 0, 0.08));
  opacity: 1;
  transform: translateY(0) scale(1);
  transition:
    opacity 0.3s ease,
    transform 0.3s ease,
    background 0.6s ease,
    color 0.6s ease,
    border-color 0.6s ease,
    box-shadow 0.6s ease;
}

.stage__bubble--hidden {
  opacity: 0;
  transform: translateY(-6px) scale(0.96);
  pointer-events: none;
}

/* ---- 句子情绪符号 ---- */
.stage__emotion {
  flex: 0 0 auto;
  font-size: 18px;
  line-height: 1;
}

.stage__text {
  white-space: pre-wrap;
}

/* ---- 流式光标 ---- */
.stage__cursor {
  display: inline-block;
  width: 6px;
  height: 1em;
  vertical-align: -0.16em;
  margin-left: 2px;
  background: currentColor;
  border-radius: 1px;
  opacity: 0.55;
  animation: cursor-blink 1s steps(2, start) infinite;
}

@keyframes cursor-blink {

  0%,
  50% {
    opacity: 0.55;
  }

  51%,
  100% {
    opacity: 0;
  }
}

/* ---- emoji 容器 ---- */
.stage__emoji {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ---- 移动端 ---- */
@media (max-width: 520px) {
  .stage {
    gap: 18px;
    padding: 16px;
  }

  .stage__bubble {
    font-size: 14px;
    padding: 12px 16px;
    min-height: 50px;
  }
}

/* ---- 减少动效 ---- */
@media (prefers-reduced-motion: reduce) {
  .stage__cursor {
    animation: none;
    opacity: 0.55;
  }
}
</style>
