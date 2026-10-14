<script setup lang="ts">
import { computed } from 'vue'
import type { Turn } from '@/types/diary'
import { emotionMeta, riskMeta } from '@/utils/diary'

const props = defineProps<{
  turn: Turn
  index: number
  total: number
  selected: boolean
  dimmed: boolean
  highlighted: boolean
}>()

const emit = defineEmits<{ (e: 'select'): void }>()

const em = computed(() => emotionMeta(props.turn.emotion))
const rm = computed(() => riskMeta(props.turn.riskLevel))
</script>

<template>
  <div
    class="turn"
    :class="{
      'is-dimmed': dimmed,
      'is-highlighted': highlighted,
      'is-selected': selected,
    }"
    :style="{ '--emotion-color': em.color }"
    @click="emit('select')"
  >
    <div class="turn__user">
      <span class="turn__time">{{ turn.time }}</span>
      <div class="turn__bubble">{{ turn.userMsg }}</div>
    </div>

    <div
      class="turn__card"
      :style="{ '--risk-color': rm.hex, '--risk-rgb': rm.rgb }"
    >
      <div class="card__head">
        <span
          class="card__emotion"
          :style="{
            color: em.color,
            background: em.color + '20',
            border: `1px solid ${em.color}60`,
          }"
        >
          <span class="emoji card__emotion-emoji">{{ em.emoji }}</span>
          <span>{{ turn.emotion }}</span>
        </span>

        <span class="card__risk">
          <span class="card__risk-dot" />
          <span>{{ rm.label }}</span>
        </span>

        <span class="card__seq">#{{ index + 1 }}/{{ total }}</span>
        <span class="card__time">{{ turn.time }}</span>
      </div>

      <div class="card__reply">{{ turn.reply }}</div>
    </div>
  </div>
</template>

<style scoped>
.turn {
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  transition: opacity 0.3s ease;
}
.turn.is-dimmed {
  opacity: 0.22;
}
.turn__user {
  align-self: flex-end;
  max-width: 78%;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.turn__time {
  font-size: 10px;
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  letter-spacing: 0.5px;
  color: var(--text-dim);
  padding: 0 6px;
}
.turn__bubble {
  padding: 11px 16px;
  border-radius: 18px 18px 6px 18px;
  background: var(--user-bg);
  color: var(--user-text);
  font-size: 14px;
  line-height: 1.6;
  letter-spacing: 0.3px;
  word-break: break-word;
  box-shadow: var(--panel-shadow);
  transition: background 0.6s ease;
}
.turn__card {
  align-self: flex-start;
  max-width: 88%;
  width: 100%;
  border-radius: 18px 18px 18px 6px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  backdrop-filter: blur(14px) saturate(120%);
  -webkit-backdrop-filter: blur(14px) saturate(120%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08), var(--panel-shadow);
  transition: background 0.3s, border-color 0.3s, box-shadow 0.3s, transform 0.2s;
  overflow: hidden;
}
.turn:hover .turn__card {
  background: var(--card-hover);
  transform: translateY(-1px);
}
.turn.is-selected .turn__card {
  background: var(--card-active);
  border-color: var(--panel-border);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12),
    0 0 0 2px color-mix(in srgb, var(--emotion-color) 45%, transparent), var(--panel-shadow);
}
.turn.is-highlighted .turn__card {
  border-color: color-mix(in srgb, var(--emotion-color) 70%, transparent);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12),
    0 0 0 2px color-mix(in srgb, var(--emotion-color) 55%, transparent),
    0 0 24px -6px color-mix(in srgb, var(--emotion-color) 60%, transparent);
}
.card__head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px 8px;
  border-bottom: 1px solid var(--card-border);
  flex-wrap: wrap;
}
.card__emotion {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px 3px 6px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
}
.card__emotion-emoji {
  font-size: 15px;
}
.card__risk {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
  color: var(--risk-color);
  background: rgba(var(--risk-rgb), var(--risk-bg-alpha));
  border: 1px solid rgba(var(--risk-rgb), var(--risk-border-alpha));
  text-shadow: var(--risk-text-glow);
}
.card__risk-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--risk-color);
  box-shadow: 0 0 0 2px rgba(var(--risk-rgb), 0.22);
}
.card__seq {
  font-size: 10px;
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  letter-spacing: 0.5px;
  color: var(--text-dim);
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--card-border);
}
.card__time {
  margin-left: auto;
  font-size: 10px;
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  letter-spacing: 0.5px;
  color: var(--text-dim);
}
.card__reply {
  padding: 12px 16px;
  font-size: 13.5px;
  line-height: 1.7;
  letter-spacing: 0.2px;
  color: var(--text);
}
</style>