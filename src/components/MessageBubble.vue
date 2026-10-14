<!-- src/components/MessageBubble.vue -->
<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'

const props = withDefaults(defineProps<{
    /** 上游累积的完整文本（流式追加） */
    text: string
    /** 上游是否仍在流式生成 */
    streaming?: boolean
    /** 每段最多显示多少字 */
    chunkSize?: number
    /** 每段之间停留时长（毫秒） */
    pauseMs?: number
    /** 打字机单字间隔（毫秒） */
    charDelayMs?: number
}>(), {
    streaming: true,
    chunkSize: 150,
    pauseMs: 3000,
    charDelayMs: 60,
})

const emit = defineEmits<{
    /** 所有分段播报完毕 */
    complete: []
}>()

/** 当前气泡里显示的内容 */
const displayed = ref('')
/** 气泡是否可见（控制淡入淡出） */
const bubbleVisible = ref(false)
/** 是否处于「逐字打字」阶段（用于光标） */
const typing = ref(false)

/** 每次挂载 / 卸载时自增，用于作废上一次 run 循环 */
let currentRun = 0

const sleep = (ms: number) => new Promise<void>((r) => setTimeout(r, ms))

/**
 * 等待 props.text 至少填充到 index+1 个字符。
 * - 若数据到了 → 返回 true
 * - 若流结束且仍不足 → 返回 false
 * - 若被外部作废（卸载 / 重启） → 返回 false
 */
async function waitForChar(index: number, id: number): Promise<boolean> {
    while (currentRun === id) {
        if (props.text.length > index) return true
        if (!props.streaming) return false
        await sleep(30)
    }
    return false
}

async function run(id: number) {
    let pos = 0

    while (currentRun === id) {
        // 1) 数据还没到就等一等
        if (pos >= props.text.length) {
            if (!props.streaming) break
            const ok = await waitForChar(pos, id)
            if (!ok || currentRun !== id) break
        }

        // 2) 开一段新气泡
        displayed.value = ''
        const blockStart = pos
        const blockMax = blockStart + props.chunkSize

        // 3) 打字机：逐字揭示，最多到 blockMax
        typing.value = true
        bubbleVisible.value = true
        for (let i = blockStart; i < blockMax; i++) {
            if (currentRun !== id) return

            if (i >= props.text.length) {
                if (!props.streaming) break
                const ok = await waitForChar(i, id)
                if (!ok || currentRun !== id) break
            }

            displayed.value = props.text.slice(blockStart, i + 1)
            pos = i + 1
            await sleep(props.charDelayMs)
        }
        typing.value = false
        if (currentRun !== id) return

        // 4) 停留
        await sleep(props.pauseMs)
        if (currentRun !== id) return

        // 5) 淡出 → 清空
        bubbleVisible.value = false
        await sleep(320) // 与 CSS transition 时长保持一致
        if (currentRun !== id) return
        displayed.value = ''
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
        <!-- 头顶气泡 -->
        <div class="stage__bubble" :class="{ 'stage__bubble--hidden': !bubbleVisible }">
            {{ displayed }}<span v-if="typing" class="stage__cursor" />
        </div>

        <!-- emoji 由父组件通过插槽传入（Emoji / 图片 / 任意内容） -->
        <div class="stage__emoji">
            <slot />
        </div>
    </div>
</template>

<style scoped>
/* =========================================================
 * 舞台：垂直居中，气泡在上、emoji 在下
 * 主题变量（--c-bot-bg / --c-bot-border / --c-bot-text / --c-shadow）
 * 由父级 .chat[data-theme=...] 定义，通过 CSS 变量继承自动生效，
 * 每个值都带兜底，脱离 ChatView 单独用也不会崩。
 * ========================================================= */
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
    padding: 14px 20px;
    border-radius: 20px;
    background: var(--c-bot-bg, rgba(255, 255, 255, .62));
    border: 1px solid var(--c-bot-border, rgba(255, 255, 255, .78));
    color: var(--c-bot-text, #4A3A30);
    font-size: 15px;
    line-height: 1.65;
    letter-spacing: .3px;
    white-space: pre-wrap;
    word-break: break-word;
    text-align: left;
    backdrop-filter: blur(14px);
    -webkit-backdrop-filter: blur(14px);
    box-shadow: var(--c-shadow, 0 8px 24px rgba(0, 0, 0, .08));
    opacity: 1;
    transform: translateY(0) scale(1);
    transition:
        opacity .3s ease,
        transform .3s ease,
        background .6s ease,
        color .6s ease,
        border-color .6s ease,
        box-shadow .6s ease;
}

.stage__bubble--hidden {
    opacity: 0;
    transform: translateY(-6px) scale(.96);
    pointer-events: none;
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
    opacity: .55;
    animation: cursor-blink 1s steps(2, start) infinite;
}

@keyframes cursor-blink {

    0%,
    50% {
        opacity: .55;
    }

    51%,
    100% {
        opacity: 0;
    }
}

/* ---- emoji 插槽容器 ---- */
.stage__emoji {
    display: flex;
    align-items: center;
    justify-content: center;
}

/* =========================================================
 * 移动端
 * ========================================================= */
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

/* =========================================================
 * 减少动效
 * ========================================================= */
@media (prefers-reduced-motion: reduce) {
    .stage__cursor {
        animation: none;
        opacity: .55;
    }
}
</style>