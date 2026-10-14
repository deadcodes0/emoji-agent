<script setup lang="ts">
import { computed } from 'vue'
import { emotionMeta } from '@/utils/diary'

const props = defineProps<{
  emotion: string
  count: number
  active: boolean
}>()

const emit = defineEmits<{ (e: 'click'): void }>()

const meta = computed(() => emotionMeta(props.emotion))
</script>

<template>
  <button type="button" class="p-emotion-tag" :class="{ 'is-active': active }" :style="{ '--tag-color': meta.color }"
    @click="emit('click')">
    <span class="emoji p-emotion-tag__emoji">{{ meta.emoji }}</span>
    <span class="p-emotion-tag__label">{{ emotion }}</span>
    <span class="p-emotion-tag__count">×{{ count }}</span>
  </button>
</template>

<style scoped>
.p-emotion-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 11px 5px 8px;
  border-radius: 999px;
  font-family: inherit;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;

  /*
   * 底色 = 情绪色 × --tag-tint + 主题基底 --tag-base
   * 主题可在祖先节点覆盖 --tag-base / --tag-tint / --tag-border-strength /
   * --tag-text-shadow，未提供时使用这里的 fallback。
   */
  color: var(--tag-color);
  background: color-mix(in srgb,
      var(--tag-color) var(--tag-tint, 16%),
      var(--tag-base, rgba(255, 255, 255, 0.55)));
  border: 1px solid color-mix(in srgb,
      var(--tag-color) var(--tag-border-strength, 55%),
      transparent);
  text-shadow: var(--tag-text-shadow, none);

  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.25s ease,
    filter 0.25s ease,
    opacity 0.25s ease,
    background 0.6s ease,
    border-color 0.6s ease,
    color 0.6s ease;
}

.p-emotion-tag:hover {
  transform: translateY(-1px);
  filter: brightness(1.08);
}

.p-emotion-tag__emoji {
  font-size: 14px;
  line-height: 1;
}

.p-emotion-tag__count {
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  font-size: 10px;
  font-weight: 700;
  opacity: 0.75;
  margin-left: 1px;
}

.p-emotion-tag.is-active {
  transform: translateY(-1px) scale(1.04);
  box-shadow:
    0 0 0 2px color-mix(in srgb, currentColor 60%, transparent),
    0 4px 14px -4px color-mix(in srgb, currentColor 60%, transparent);
}
</style>