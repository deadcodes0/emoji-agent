<!-- src/components/ChatInput.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { useTheme } from '@/composables/useTheme'

/* ---------------- props / emits ---------------- */
const props = withDefaults(defineProps<{
  modelValue: string
  placeholder?: string
  maxlength?: number
  /** 禁用输入和发送（例如流式回复进行中） */
  disabled?: boolean
  /** 是否处于流式接收状态：为 true 时按钮切换为「停止」 */
  streaming?: boolean
}>(), {
  placeholder: '说点什么…',
  maxlength: 200,
  disabled: false,
  streaming: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'send': [value: string]
  'stop': []
  'focus': []
  'blur': []
}>()

/* ---------------- 主题 ---------------- */
const { theme } = useTheme()

/* ---------------- 逻辑 ---------------- */
const canSend = computed(
  () =>
    !props.disabled &&
    !props.streaming &&
    props.modelValue.trim().length > 0,
)

function onInput(e: Event) {
  emit('update:modelValue', (e.target as HTMLInputElement).value)
}

function send() {
  if (!canSend.value) return
  emit('send', props.modelValue.trim())
  emit('update:modelValue', '')
}

function stop() {
  emit('stop')
}
</script>

<template>
  <div class="chat-input" :data-theme="theme">
    <div class="chat-input__box">
      <input class="chat-input__field" :value="modelValue" :placeholder="placeholder" :maxlength="maxlength"
        :disabled="disabled || streaming" type="text" autocomplete="off" @input="onInput" @keydown.enter.prevent="send"
        @focus="emit('focus')" @blur="emit('blur')" />

      <!-- 流式中：停止 -->
      <button v-if="streaming" class="chat-input__send chat-input__send--stop" type="button" aria-label="停止生成"
        @click="stop">
        <span class="chat-input__stop-square" aria-hidden="true" />
        <span class="chat-input__send-text">停止</span>
      </button>

      <!-- 平时：发送 -->
      <button v-else class="chat-input__send" type="button" :disabled="!canSend" :aria-label="'发送'" @click="send">
        <span class="chat-input__send-text">发送</span>
        <svg class="chat-input__send-icon" viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
          <path d="M4 12l16-8-6 8 6 8-16-8z" fill="currentColor" />
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
/* =========================================================
 * 主题变量：全部集中在根元素上，四种主题各覆盖一遍
 * ========================================================= */
.chat-input {
  /* 白天 */
  --ci-bg: rgba(255, 255, 255, .55);
  --ci-border: rgba(255, 255, 255, .7);
  --ci-text: #6B4A3A;
  --ci-placeholder: rgba(107, 74, 58, .45);
  --ci-shadow: 0 8px 24px rgba(0, 0, 0, .08);
  --ci-ring: rgba(255, 154, 118, .35);

  --ci-send-bg: linear-gradient(135deg, #FFB88C, #FF9A76);
  --ci-send-color: #fff;
  --ci-send-shadow: 0 6px 16px rgba(255, 154, 118, .35);

  width: 100%;
  pointer-events: auto;
  /* 父级 .content 是 pointer-events:none */
}

/* 黄昏 */
.chat-input[data-theme='dusk'] {
  --ci-bg: rgba(255, 255, 255, .18);
  --ci-border: rgba(255, 255, 255, .35);
  --ci-text: #FFFFFF;
  --ci-placeholder: rgba(255, 255, 255, .55);
  --ci-shadow: 0 8px 24px rgba(60, 30, 80, .18);
  --ci-ring: rgba(232, 139, 160, .45);

  --ci-send-bg: linear-gradient(135deg, #E88BA0, #A87AD8);
  --ci-send-color: #fff;
  --ci-send-shadow: 0 6px 18px rgba(168, 122, 216, .4);
}

/* 夜晚 */
.chat-input[data-theme='night'] {
  --ci-bg: rgba(255, 255, 255, .06);
  --ci-border: rgba(200, 200, 255, .22);
  --ci-text: #F0EFFF;
  --ci-placeholder: rgba(200, 200, 255, .45);
  --ci-shadow: 0 0 24px rgba(120, 140, 255, .12);
  --ci-ring: rgba(140, 160, 255, .35);

  --ci-send-bg: rgba(140, 160, 255, .22);
  --ci-send-color: #E8E8FF;
  --ci-send-shadow: 0 0 22px rgba(140, 160, 255, .3);
}

/* 深夜 */
.chat-input[data-theme='midnight'] {
  --ci-bg: rgba(255, 255, 255, .04);
  --ci-border: rgba(150, 160, 220, .18);
  --ci-text: #C8C8E8;
  --ci-placeholder: rgba(150, 160, 220, .4);
  --ci-shadow: 0 0 20px rgba(100, 120, 220, .1);
  --ci-ring: rgba(120, 140, 255, .3);

  --ci-send-bg: rgba(120, 140, 255, .18);
  --ci-send-color: #C8C8E8;
  --ci-send-shadow: 0 0 20px rgba(120, 140, 255, .25);
}

/* =========================================================
 * 外层胶囊
 * ========================================================= */
.chat-input__box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 6px 6px 20px;
  border-radius: 999px;
  background: var(--ci-bg);
  border: 1px solid var(--ci-border);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: var(--ci-shadow);
  transition:
    background .6s ease,
    border-color .6s ease,
    box-shadow .3s ease;
}

.chat-input__box:focus-within {
  border-color: var(--ci-ring);
  box-shadow: var(--ci-shadow), 0 0 0 3px var(--ci-ring);
}

/* 夜 / 深夜，额外描一层淡边 */
.chat-input[data-theme='night'] .chat-input__box,
.chat-input[data-theme='midnight'] .chat-input__box {
  border-color: rgba(200, 200, 255, .28);
}

/* =========================================================
 * 输入框本体
 * ========================================================= */
.chat-input__field {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: var(--ci-text);
  font-family: inherit;
  font-size: 15px;
  line-height: 1.4;
  padding: 8px 0;
  caret-color: var(--ci-text);
  transition: color .6s ease, opacity .3s ease;
}

.chat-input__field::placeholder {
  color: var(--ci-placeholder);
  transition: color .6s ease;
}

.chat-input__field:disabled {
  cursor: not-allowed;
  opacity: .55;
}

/* =========================================================
 * 发送 / 停止 按钮（共用一套基础样式）
 * ========================================================= */
.chat-input__send {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: 999px;
  background: var(--ci-send-bg);
  color: var(--ci-send-color);
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 1px;
  cursor: pointer;
  box-shadow: var(--ci-send-shadow);
  transition:
    transform .2s ease,
    box-shadow .3s ease,
    opacity .3s ease,
    background .6s ease,
    color .6s ease,
    border-color .3s ease;
}

.chat-input__send:hover:not(:disabled) {
  transform: translateY(-1px);
}

.chat-input__send:active:not(:disabled) {
  transform: translateY(0) scale(.97);
}

.chat-input__send:disabled {
  opacity: .4;
  cursor: not-allowed;
  box-shadow: none;
}

/* 夜 / 深夜，按钮加内描边 */
.chat-input[data-theme='night'] .chat-input__send,
.chat-input[data-theme='midnight'] .chat-input__send {
  border: 1px solid rgba(200, 200, 255, .35);
}

/* ---- 停止按钮：视觉上柔一档，避免被误当发送 ---- */
.chat-input__send--stop {
  background: transparent;
  color: var(--ci-text);
  border: 1px solid var(--ci-border);
  box-shadow: none;
}

.chat-input[data-theme='night'] .chat-input__send--stop,
.chat-input[data-theme='midnight'] .chat-input__send--stop {
  border-color: rgba(200, 200, 255, .45);
}

.chat-input__send--stop:hover:not(:disabled) {
  border-color: var(--ci-ring);
  background: var(--ci-bg);
}

/* 停止图标：小方块 */
.chat-input__stop-square {
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 2px;
  background: currentColor;
}

/* 发送图标 */
.chat-input__send-icon {
  display: block;
  transform: translateX(-1px);
  transition: transform .25s ease;
}

.chat-input__send:hover:not(:disabled) .chat-input__send-icon {
  transform: translateX(2px);
}

/* =========================================================
 * 移动端
 * ========================================================= */
@media (max-width: 520px) {
  .chat-input__box {
    padding: 4px 4px 4px 16px;
  }

  .chat-input__field {
    font-size: 14px;
  }

  .chat-input__send {
    padding: 9px 14px;
    font-size: 13px;
    letter-spacing: 0;
  }

  .chat-input__send-text {
    display: none;
    /* 窄屏只留图标 */
  }

  .chat-input__send {
    padding: 9px 12px;
  }

  /* 停止按钮窄屏只留方块，稍放大防误触 */
  .chat-input__stop-square {
    width: 10px;
    height: 10px;
  }
}
</style>