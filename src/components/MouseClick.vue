<template>
  <div
    class="heart-click"
    :class="{ 'is-disabled': disabled }"
    @pointerdown="onPointerDown"
  >
    <!-- 默认插槽：放图片或任意内容 -->
    <slot />

    <!-- 爱心层 -->
    <span
      v-for="h in hearts"
      :key="h.id"
      class="heart-click__item"
      :style="heartStyle(h)"
      @animationend="removeHeart(h.id)"
    >{{ icon }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

/* ---------------- 类型定义 ---------------- */
interface Heart {
  id: number
  x: number
  y: number
  size: number
  dx: number
  rot: number
}

interface Props {
  /** 爱心图标，可换成 💖 ✨ 🌸 👍 等 */
  icon?: string
  /** 基础字号(px)，实际大小会在此基础上随机浮动 */
  size?: number
  /** 水平漂移范围(px) */
  spread?: number
  /** 上飘高度(px) */
  rise?: number
  /** 动画时长(秒) */
  duration?: number
  /** 同时存在的爱心上限，超出则移除最旧的，防止狂点卡顿 */
  maxHearts?: number
  /** 禁用点击 */
  disabled?: boolean
}

/** 爱心点击位置（视口坐标） */
interface HeartPayload {
  x: number
  y: number
}

/* ---------------- props ---------------- */
const props = withDefaults(defineProps<Props>(), {
  icon: '❤️',
  size: 24,
  spread: 70,
  rise: 100,
  duration: 1.2,
  maxHearts: 50,
  disabled: false,
})

/* ---------------- emits ---------------- */
const emit = defineEmits<{
  /** 每次冒爱心时触发 */
  (e: 'heart', payload: HeartPayload): void
}>()

/* ---------------- 状态 ---------------- */
const hearts = ref<Heart[]>([])
let uid = 0

/* ---------------- 核心逻辑 ---------------- */
function onPointerDown(e: PointerEvent): void {
  if (props.disabled) return

  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()

  hearts.value.push({
    id: uid++,
    x: e.clientX - rect.left,
    y: e.clientY - rect.top,
    size: props.size * (0.75 + Math.random() * 0.6),
    dx: (Math.random() - 0.5) * props.spread,
    rot: (Math.random() - 0.5) * 80,
  })

  // 超出上限，移除最旧的
  if (hearts.value.length > props.maxHearts) {
    hearts.value.splice(0, hearts.value.length - props.maxHearts)
  }

  emit('heart', { x: e.clientX, y: e.clientY })
}

function removeHeart(id: number): void {
  const i = hearts.value.findIndex((h) => h.id === id)
  if (i > -1) hearts.value.splice(i, 1)
}

/* ---------------- 样式计算 ---------------- */
/** 返回给 :style 的对象，含 CSS 自定义属性，故用 Record<string, string> */
function heartStyle(h: Heart): Record<string, string> {
  return {
    left: `${h.x}px`,
    top: `${h.y}px`,
    fontSize: `${h.size}px`,
    animationDuration: `${props.duration}s`,
    '--dx': `${h.dx}px`,
    '--rise': `-${props.rise}px`,
    '--rot': `${h.rot}deg`,
  }
}

/* ---------------- 暴露给父组件 ---------------- */
defineExpose({
  /** 清空所有爱心 */
  clear: (): void => {
    hearts.value = []
  },
})
</script>

<style scoped>
.heart-click {
  position: relative;
  display: inline-block;
  cursor: pointer;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}

.heart-click.is-disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.heart-click__item {
  position: absolute;
  /* 关键：不遮挡后续点击 */
  pointer-events: none;
  line-height: 1;
  /* 让爱心从点击点中心开始 */
  transform: translate(-50%, -50%);
  animation: heart-float 1.2s ease-out forwards;
  will-change: transform, opacity;
  /* 让 emoji 稍微有点立体感 */
  filter: drop-shadow(0 2px 4px rgba(255, 80, 120, 0.35));
}

@keyframes heart-float {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.3) rotate(0deg);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.1) rotate(calc(var(--rot) * 0.2));
  }
  35% {
    opacity: 1;
    transform: translate(
        calc(-50% + var(--dx) * 0.3),
        calc(-50% + var(--rise) * 0.3)
      )
      scale(1.2) rotate(calc(var(--rot) * 0.4));
  }
  100% {
    opacity: 0;
    transform: translate(calc(-50% + var(--dx)), calc(-50% + var(--rise)))
      scale(0.85) rotate(var(--rot));
  }
}
</style>