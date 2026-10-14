<!-- src/components/EmojiGif.vue -->
<template>
  <div class="blink" :class="{ 'is-clickable': clickable, 'is-disabled': disabled }" @pointerdown="onPointerDown">
    <!-- 八个环绕方位：URL 编进 key，URL 变 → 旧元素 leave、新元素 enter -->
    <TransitionGroup name="fade-slot">
      <div v-for="key in visibleSlots" :key="`${key}:${props.images?.[key]}`" class="blink__slot"
        :class="`blink__slot--${key}`" :style="slotStyle(key)">
        <img class="blink__gif" :src="props.images?.[key] ?? ''" alt="表情" draggable="false" />
      </div>
    </TransitionGroup>

    <!-- 居中主图：同样把 src 编进 key -->
    <Transition name="fade-slot">
      <div v-if="props.src" :key="props.src" class="blink__slot blink__slot--center" :style="slotStyle('center')">
        <img class="blink__gif" :src="props.src" alt="表情" draggable="false" />
      </div>
    </Transition>

    <span v-for="h in hearts" :key="h.id" class="blink__heart" :style="heartStyle(h)"
      @animationend="removeHeart(h.id)">{{ icon }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

/* ---------------- 类型 ---------------- */
type SlotKey = 'tl' | 't' | 'tr' | 'r' | 'br' | 'b' | 'bl' | 'l'
type AllSlotKey = SlotKey | 'center'
type AnchorValue = 0 | 50 | 100

interface Offset {
  x?: number
  y?: number
}

interface Heart {
  id: number
  x: number
  y: number
  size: number
  dx: number
  rot: number
}

interface HeartPayload {
  x: number
  y: number
}

/* ---------------- 常量表 ---------------- */
const SLOT_KEYS: readonly SlotKey[] = ['tl', 't', 'tr', 'r', 'br', 'b', 'bl', 'l'] as const

const ANCHOR_X: Record<AllSlotKey, AnchorValue> = {
  center: 50,
  tl: 0, t: 50, tr: 100, r: 100,
  br: 100, b: 50, bl: 0, l: 0,
}
const ANCHOR_Y: Record<AllSlotKey, AnchorValue> = {
  center: 50,
  tl: 0, t: 0, tr: 0, r: 50,
  br: 100, b: 100, bl: 100, l: 50,
}

const DEFAULT_SIZES: Record<AllSlotKey, number> = {
  center: 120,
  tl: 48, t: 56, tr: 48,
  r: 56, l: 56,
  bl: 48, b: 56, br: 48,
}

const DEFAULT_OFFSETS: Record<AllSlotKey, Required<Offset>> = {
  center: { x: 0, y: 0 },
  tl: { x: -6, y: -6 },
  t: { x: 0, y: -10 },
  tr: { x: 6, y: -6 },
  r: { x: 10, y: 0 },
  br: { x: 6, y: 6 },
  b: { x: 0, y: 10 },
  bl: { x: -6, y: 6 },
  l: { x: -10, y: 0 },
}

/* ---------------- props ---------------- */
const props = withDefaults(
  defineProps<{
    /* ---- 布局 ---- */
    /** 居中主图（可选） */
    src?: string | null
    /** 八个环绕方位（可选） */
    images?: Partial<Record<SlotKey, string | null | undefined>> | null
    /** 覆盖单格尺寸（px） */
    sizes?: Partial<Record<AllSlotKey, number>>
    /** 覆盖单格偏移（px） */
    offsets?: Partial<Record<AllSlotKey, Offset>>
    /** 环绕整体向内收缩：0 = 贴边（默认），建议 0–35 */
    inset?: number
    /** slot 过渡时长（秒），默认 0.35 */
    transitionDuration?: number

    /* ---- 点击冒爱心（原 HeartClick） ---- */
    /** 是否开启点击冒爱心 */
    clickable?: boolean
    /** 爱心图标 */
    icon?: string
    /** 爱心基础字号(px)，实际会随机浮动 */
    heartSize?: number
    /** 水平漂移范围(px) */
    spread?: number
    /** 上飘高度(px) */
    rise?: number
    /** 爱心动画时长(秒) */
    heartDuration?: number
    /** 同时存在的爱心上限，超出则移除最旧的 */
    maxHearts?: number
    /** 禁用点击（同时会降低透明度） */
    disabled?: boolean
  }>(),
  {
    src: null,
    images: null,
    sizes: () => ({}),
    offsets: () => ({}),
    inset: 0,
    transitionDuration: 0.35,

    clickable: false,
    icon: '❤️',
    heartSize: 24,
    spread: 70,
    rise: 100,
    heartDuration: 1.2,
    maxHearts: 50,
    disabled: false,
  },
)

/* ---------------- emits ---------------- */
const emit = defineEmits<{
  /** 每次冒爱心时触发（视口坐标） */
  (e: 'heart', payload: HeartPayload): void
}>()

/* ---------------- 布局计算 ---------------- */
const visibleSlots = computed<SlotKey[]>(() =>
  SLOT_KEYS.filter((k) => !!props.images?.[k]),
)

function anchorPercent(v: AnchorValue, key: AllSlotKey): string {
  if (key === 'center') return '50%'
  if (v === 0) return `${props.inset}%`
  if (v === 100) return `calc(100% - ${props.inset}%)`
  return '50%'
}

function slotStyle(key: AllSlotKey): Record<string, string> {
  const size = props.sizes[key] ?? DEFAULT_SIZES[key]
  const offset = props.offsets[key] ?? DEFAULT_OFFSETS[key]
  return {
    '--size': `${size}px`,
    '--ax': anchorPercent(ANCHOR_X[key], key),
    '--ay': anchorPercent(ANCHOR_Y[key], key),
    '--tx': `${offset.x ?? 0}px`,
    '--ty': `${offset.y ?? 0}px`,
    '--t-dur': `${props.transitionDuration}s`,
  }
}

/* ---------------- 爱心状态 ---------------- */
const hearts = ref<Heart[]>([])
let uid = 0

function onPointerDown(e: PointerEvent): void {
  if (!props.clickable || props.disabled) return

  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()

  hearts.value.push({
    id: uid++,
    x: e.clientX - rect.left,
    y: e.clientY - rect.top,
    size: props.heartSize * (0.75 + Math.random() * 0.6),
    dx: (Math.random() - 0.5) * props.spread,
    rot: (Math.random() - 0.5) * 80,
  })

  if (hearts.value.length > props.maxHearts) {
    hearts.value.splice(0, hearts.value.length - props.maxHearts)
  }

  emit('heart', { x: e.clientX, y: e.clientY })
}

function removeHeart(id: number): void {
  const i = hearts.value.findIndex((h) => h.id === id)
  if (i > -1) hearts.value.splice(i, 1)
}

function heartStyle(h: Heart): Record<string, string> {
  return {
    left: `${h.x}px`,
    top: `${h.y}px`,
    fontSize: `${h.size}px`,
    animationDuration: `${props.heartDuration}s`,
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
/* ====== 舞台容器 ====== */
.blink {
  position: relative;
  width: 200px;
  height: 200px;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}

.blink.is-clickable {
  cursor: pointer;
}

.blink.is-disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* ====== 每个方位 ====== */
.blink__slot {
  position: absolute;
  width: var(--size, 56px);
  height: var(--size, 56px);
  left: var(--ax, 50%);
  top: var(--ay, 50%);
  transform: translate(-50%, -50%) translate(var(--tx, 0), var(--ty, 0));
  /* 不拦截 pointerdown，让爱心能落到 .blink 上 */
  pointer-events: none;
}

.blink__slot--center {
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

/* =========================================================
 * slot 过渡
 * ========================================================= */
.fade-slot-enter-active,
.fade-slot-leave-active {
  transition: opacity var(--t-dur, 0.35s) ease;
}

.fade-slot-enter-from,
.fade-slot-leave-to {
  opacity: 0;
}

/* ====== 内层 img 的漂浮动画 ====== */
.blink__gif {
  width: 100%;
  height: 100%;
  object-fit: contain;
  user-select: none;
  animation: float 3s ease-in-out infinite;
  will-change: transform, opacity;
  pointer-events: none;
}

.blink__slot--center .blink__gif {
  animation: none;
}

.blink__slot--tl .blink__gif {
  animation-duration: 3.4s;
  animation-delay: -0.2s;
}

.blink__slot--t .blink__gif {
  animation-duration: 2.8s;
  animation-delay: -1.1s;
}

.blink__slot--tr .blink__gif {
  animation-duration: 3.1s;
  animation-delay: -0.6s;
}

.blink__slot--r .blink__gif {
  animation-duration: 3.6s;
  animation-delay: -1.6s;
}

.blink__slot--br .blink__gif {
  animation-duration: 3.2s;
  animation-delay: -0.9s;
}

.blink__slot--b .blink__gif {
  animation-duration: 3.0s;
  animation-delay: -1.4s;
}

.blink__slot--bl .blink__gif {
  animation-duration: 3.5s;
  animation-delay: -0.4s;
}

.blink__slot--l .blink__gif {
  animation-duration: 2.9s;
  animation-delay: -1.8s;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-6px);
  }
}

/* =========================================================
 * 爱心层（原 HeartClick）
 * ========================================================= */
.blink__heart {
  position: absolute;
  pointer-events: none;
  line-height: 1;
  transform: translate(-50%, -50%);
  animation: heart-float 1.2s ease-out forwards;
  will-change: transform, opacity;
  filter: drop-shadow(0 2px 4px rgba(255, 80, 120, 0.35));
  z-index: 2;
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
    transform: translate(calc(-50% + var(--dx) * 0.3),
        calc(-50% + var(--rise) * 0.3)) scale(1.2) rotate(calc(var(--rot) * 0.4));
  }

  100% {
    opacity: 0;
    transform: translate(calc(-50% + var(--dx)), calc(-50% + var(--rise))) scale(0.85) rotate(var(--rot));
  }
}

/* ====== 移动端 ====== */
@media (max-width: 520px) {
  .blink {
    width: 160px;
    height: 160px;
  }
}

/* ====== 尊重系统「减少动效」偏好 ====== */
@media (prefers-reduced-motion: reduce) {
  .blink__gif {
    animation: none;
  }

  .fade-slot-enter-active,
  .fade-slot-leave-active {
    transition: none;
  }
}
</style>