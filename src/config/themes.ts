// src/config/themes.ts
// 纯数据配置，无任何副作用。想在别处 import 文案/边界，不会触发主题初始化。

export const THEME_ORDER = ['day', 'dusk', 'night', 'midnight'] as const
export type Theme = typeof THEME_ORDER[number]

export interface ThemeMeta {
    label: string
    icon: string      // 对应 src/assets/gif/<logo>.gif
    title: string
    sub: string
}

export const THEMES: Record<Theme, ThemeMeta> = {
    day: {
        label: '白天',
        icon: 'happy',
        title: '今天也请温柔地对待自己',
        sub: '你不需要每天都很好，你只需要记得，你值得被温柔对待',
    },
    dusk: {
        label: '黄昏',
        icon: 'blink',
        title: '慢下来，看看今天的晚霞',
        sub: '一天又过去了，你已经做了很多，允许自己停下来',
    },
    night: {
        label: '夜晚',
        icon: 'yawn',
        title: '夜深了，让心事也休息一下吧',
        sub: '你已经做得很好了，剩下的交给明天',
    },
    midnight: {
        label: '深夜',
        icon: 'sleeping',
        title: '世界都睡了，你也可以安心休息',
        sub: '这个时间还醒着，辛苦了。我陪着你',
    },
}

export const STORAGE_KEY = 'pet-theme-manual'

export interface Boundary {
    hour: number
    minute: number
}

export const DAY_START: Boundary = { hour: 6, minute: 0 }
export const DUSK_START: Boundary = { hour: 17, minute: 0 }
export const NIGHT_START: Boundary = { hour: 19, minute: 30 }
export const MIDNIGHT_START: Boundary = { hour: 23, minute: 0 }

export const BOUNDARIES: readonly Boundary[] = [
    DAY_START, DUSK_START, NIGHT_START, MIDNIGHT_START,
]