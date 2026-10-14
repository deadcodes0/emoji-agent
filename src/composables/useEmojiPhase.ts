// src/composables/useEmojiPhase.ts
import { computed, readonly, ref } from 'vue'
import { getGif } from './useGif'

/** 表情舞台的情绪阶段 —— 新增阶段只需在这里加一个键 */
export type EmojiPhase = 'thinking' | 'talking' | 'injured'

/** 八个环绕方位（与 EmojiGif 的 SlotKey 保持一致） */
type SlotKey = 'tl' | 't' | 'tr' | 'r' | 'br' | 'b' | 'bl' | 'l'

interface PhaseAssets {
    /** 居中主图的素材名 */
    src: string
    /** 环绕方位的素材名 */
    images: Partial<Record<SlotKey, string>>
}

/**
 * 阶段 → 素材名 的唯一事实来源。
 * 加阶段只改这里，composable 的 API 不变。
 */
const PHASE_ASSETS: Record<EmojiPhase, PhaseAssets> = {
    thinking: {
        src: 'thinking',
        images: { tl: 'stacked_book', tr: 'bubble' },
    },
    talking: {
        src: 'talking',
        images: { tr: 'opening_book' },
    },
    injured: {
        src: 'injured',
        images: { tl: 'x_mark', tr: 'pill' },
    },
}

function resolve(assets: PhaseAssets) {
    return {
        src: getGif(assets.src),
        images: Object.fromEntries(
            Object.entries(assets.images).map(([k, name]) => [k, getGif(name as string)]),
        ) as Partial<Record<SlotKey, string>>,
    }
}

/**
 * 管理「表情舞台」的阶段，并翻译成 EmojiGif 需要的 prop。
 * 状态内部持有，外部用单一的 setPhase() 切换。
 */
export function useEmojiPhase(initial: EmojiPhase = 'thinking') {
    const _phase = ref<EmojiPhase>(initial)

    const visual = computed(() => resolve(PHASE_ASSETS[_phase.value]))

    /** 唯一的切换入口：setPhase('thinking' | 'talking' | 'injured' | ...) */
    function setPhase(next: EmojiPhase): void {
        _phase.value = next
    }

    return {
        /** 当前阶段（只读，修改请用 setPhase） */
        phase: readonly(_phase),

        /** 居中主图 URL */
        emojiSrc: computed(() => visual.value.src),
        /** 环绕方位 URL 表 */
        overlayImages: computed(() => visual.value.images),

        /** 切换阶段 */
        setPhase,
    }
}