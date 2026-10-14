可以，`EmotionDiaryView.vue` 目前确实太重了（数据、工具、状态、UI 全挤在一起）。下面按"类型 → 配置 → 工具 → 状态 → 组件 → 页面"的分层拆成 13 个文件，你只要按路径落盘即可。

## 目录结构

```
src/
├─ types/
│   └─ diary.ts                    # 类型定义
├─ config/
│   ├─ emotionMeta.ts              # 情绪/风险/主题元数据
│   └─ diaryData.ts                # 静态数据（可换成接口）
├─ utils/
│   ├─ diary.ts                    # 纯函数工具
│   └─ device.ts                   # 响应式设备判断
├─ stores/
│   └─ diary.ts                    # 模块级单例状态 + actions
├─ components/
│   └─ diary/
│       ├─ DiaryToolbar.vue
│       ├─ DiaryCalendar.vue
│       ├─ DiaryStream.vue
│       ├─ TurnCard.vue
│       ├─ DiaryDetailPanel.vue
│       └─ EmotionTag.vue
└─ view/
    └─ EmotionDiaryView.vue        # 只负责组装
```

---

## 1. `src/types/diary.ts`

```ts
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type ThemeName = 'day' | 'dusk' | 'night' | 'midnight'

export interface Turn {
  time: string
  userMsg: string
  reply: string
  emotion: string
  riskLevel: RiskLevel
  suggestions: string[]
}

export interface DiaryDay {
  date: string
  weekday: string
  turns: Turn[]
}

export interface EmotionMeta {
  emoji: string
  color: string
}

export interface RiskMeta {
  label: string
  hex: string
  rgb: string
}

export interface CalCell {
  day: number
  inMonth: boolean
  dateObj: Date
  dateKey: string
  hasData: boolean
  color: string
  segments: string[]
}

export interface EmotionStat {
  emotion: string
  count: number
  maxRisk: number
}
```

---

## 2. `src/config/emotionMeta.ts`

```ts
import type { EmotionMeta, RiskLevel, RiskMeta, ThemeName } from '@/types/diary'

export const EMOTION_META: Record<string, EmotionMeta> = {
  喜悦: { emoji: '😊', color: '#F5B942' },
  平静: { emoji: '😌', color: '#6FC8A8' },
  感激: { emoji: '🥰', color: '#E8A87C' },
  期待: { emoji: '🤩', color: '#9FD356' },
  焦虑: { emoji: '😰', color: '#E8A23C' },
  愤怒: { emoji: '😠', color: '#D9433C' },
  恐惧: { emoji: '😨', color: '#B05CE0' },
  烦躁: { emoji: '😤', color: '#E07A3C' },
  羞耻: { emoji: '😳', color: '#C97BA8' },
  悲伤: { emoji: '😢', color: '#5B8DEF' },
  孤独: { emoji: '🥺', color: '#7A8BC8' },
  疲惫: { emoji: '😩', color: '#8B9AA8' },
  麻木: { emoji: '😐', color: '#9A9AB0' },
  无助: { emoji: '😞', color: '#6B7AA8' },
  矛盾: { emoji: '😕', color: '#A87AD8' },
  失落: { emoji: '😔', color: '#8A7AC8' },
  压力: { emoji: '😣', color: '#E09A3C' },
  困惑: { emoji: '🤔', color: '#7FB8C8' },
  绝望: { emoji: '😭', color: '#8B2C3C' },
  自伤倾向: { emoji: '🆘', color: '#B01030' },
  未知: { emoji: '❓', color: '#A0A0A0' },
}

export const RISK_META: Record<RiskLevel, RiskMeta> = {
  LOW: { label: '低风险', hex: '#4FBF72', rgb: '79, 191, 114' },
  MEDIUM: { label: '中等风险', hex: '#E0A83C', rgb: '224, 168, 60' },
  HIGH: { label: '高风险', hex: '#E0742E', rgb: '224, 116, 46' },
  CRITICAL: { label: '危急', hex: '#D9433C', rgb: '217, 67, 60' },
}

export const RISK_SCORE: Record<RiskLevel, number> = {
  LOW: 1,
  MEDIUM: 2,
  HIGH: 3,
  CRITICAL: 4,
}

export const THEMES: { value: ThemeName; emoji: string; title: string }[] = [
  { value: 'day', emoji: '☀️', title: '白天' },
  { value: 'dusk', emoji: '🌆', title: '黄昏' },
  { value: 'night', emoji: '🌙', title: '夜晚' },
  { value: 'midnight', emoji: '🌌', title: '深夜' },
]

export const CALENDAR_WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日']

export const MAX_SEGMENTS = 6
```

---

## 3. `src/config/diaryData.ts`

```ts
import type { DiaryDay } from '@/types/diary'

export const DIARY: DiaryDay[] = [
  {
    date: '2026-10-10',
    weekday: '周五',
    turns: [
      {
        time: '07:30',
        userMsg: '醒来还是有点困，昨晚没睡好',
        reply:
          '睡眠被打断后，第二天早上身体会有明显的迟滞感。先别急着用咖啡硬撑，让身体慢慢醒过来。',
        emotion: '疲惫',
        riskLevel: 'LOW',
        suggestions: ['先喝一杯温水', '早餐吃点蛋白质', '中午小睡 15 分钟'],
      },
      {
        time: '09:15',
        userMsg: '明天要汇报，PPT 还没改完，心跳有点快',
        reply:
          '汇报前的这种紧张，身体先于脑子感受到压力，是很正常的。心跳加快是身体在准备应对挑战。',
        emotion: '焦虑',
        riskLevel: 'MEDIUM',
        suggestions: ['先做 3 次 4-7-8 呼吸', '把 PPT 最重要的 3 页标出来', '今晚 23 点前关电脑'],
      },
      {
        time: '14:30',
        userMsg: '中午出去走了走，感觉好一点了',
        reply:
          '能主动离开屏幕去走一走，本身就是很好的自我调节。身体动起来之后，情绪会跟着松动。',
        emotion: '平静',
        riskLevel: 'LOW',
        suggestions: ['保持午后散步的习惯', '记下这个"走一走就有用"的经验'],
      },
      {
        time: '18:40',
        userMsg: '妈妈打电话来问近况，说着说着有点想她',
        reply:
          '和家人的连接会同时唤起温暖和想念。这种情绪里没有危险，它只是提醒你有些关系很重要。',
        emotion: '感激',
        riskLevel: 'LOW',
        suggestions: ['周末给家里打个视频', '把今天想说的话记下来'],
      },
      {
        time: '22:45',
        userMsg: '忙完一天，现在整个人是空的',
        reply: '连续高强度之后，身体会主动要求停下来。这种"空"不是懒，是耗竭的信号。',
        emotion: '疲惫',
        riskLevel: 'LOW',
        suggestions: ['今晚允许自己什么都不做', '明天减少一项非必要安排', '23 点前入睡'],
      },
    ],
  },
  {
    date: '2026-10-09',
    weekday: '周四',
    turns: [
      {
        time: '22:18',
        userMsg: '加了三天班，现在只想躺着',
        reply: '身体在发出明确的信号了。需要的不是"再撑一下"，而是真正的休息。',
        emotion: '疲惫',
        riskLevel: 'LOW',
        suggestions: ['今晚不安排任何事', '喝杯温水，早点躺下'],
      },
    ],
  },
  {
    date: '2026-10-08',
    weekday: '周三',
    turns: [
      {
        time: '19:05',
        userMsg: '室友都出门了，一个人在家，有点说不出的难受',
        reply:
          '一个人待着的时候，那种说不出的空落感会特别明显。这种感觉是真实的，不是你太敏感。',
        emotion: '孤独',
        riskLevel: 'MEDIUM',
        suggestions: ['给一个信任的人发条消息', '去楼下走 10 分钟', '泡一杯热饮'],
      },
      {
        time: '21:40',
        userMsg: '刚刚和朋友视频了一会儿，好多了',
        reply: '主动建立连接之后，孤独感会明显缓解。你做到了很重要的一步。',
        emotion: '感激',
        riskLevel: 'LOW',
        suggestions: ['记住这个"打一个电话就好"的经验'],
      },
    ],
  },
  {
    date: '2026-10-07',
    weekday: '周二',
    turns: [
      {
        time: '10:47',
        userMsg: '早上做了 20 分钟冥想，一整天节奏都挺稳',
        reply: '听起来这个早晨的节奏让你比较稳。能把冥想坚持下来本身就不容易。',
        emotion: '平静',
        riskLevel: 'LOW',
        suggestions: ['保持这个晨间习惯', '记下今天做得好的三件事'],
      },
    ],
  },
  {
    date: '2026-10-06',
    weekday: '周一',
    turns: [
      {
        time: '23:51',
        userMsg: '突然想起以前的事，心里堵得慌',
        reply: '有些情绪会在你以为过去之后突然回来，这很正常。它可能需要被看见一会儿。',
        emotion: '悲伤',
        riskLevel: 'MEDIUM',
        suggestions: ['允许自己难过一会儿', '写下来，不用给任何人看', '睡前做一次身体扫描'],
      },
    ],
  },
  {
    date: '2026-10-05',
    weekday: '周日',
    turns: [
      {
        time: '16:20',
        userMsg: '和朋友爬山，山顶风好大，但特别爽',
        reply:
          '这种开阔的感觉值得记住。身体的疲惫和心里的舒展同时出现，是很好的信号。',
        emotion: '喜悦',
        riskLevel: 'LOW',
        suggestions: ['把照片存进"好日子"相册', '记住这个身体的感觉'],
      },
    ],
  },
  {
    date: '2026-10-04',
    weekday: '周六',
    turns: [
      {
        time: '20:33',
        userMsg: '有个新机会，但舍不得现在的工作，纠结好几天了',
        reply: '两边都有真实的重量，才会这么难选。这种纠结本身说明你在认真对待。',
        emotion: '矛盾',
        riskLevel: 'LOW',
        suggestions: ['分别写下两边最怕失去什么', '给自己三天不决定'],
      },
    ],
  },
]
```

---

## 4. `src/utils/diary.ts`

```ts
import { DIARY } from '@/config/diaryData'
import { EMOTION_META, MAX_SEGMENTS, RISK_META, RISK_SCORE } from '@/config/emotionMeta'
import type { CalCell, DiaryDay, EmotionStat, RiskLevel } from '@/types/diary'

export const pad = (n: number) => String(n).padStart(2, '0')

export const formatDate = (d: Date) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`

export const turnId = (date: string, idx: number) => `${date}__${idx}`

export const findDay = (date: string): DiaryDay | undefined =>
  DIARY.find(d => d.date === date)

export const emotionMeta = (e: string) => EMOTION_META[e] ?? EMOTION_META['未知']

export const riskMeta = (r: RiskLevel) => RISK_META[r] ?? RISK_META.LOW

export function countEmotions(day: DiaryDay): Record<string, number> {
  const count: Record<string, number> = {}
  day.turns.forEach(t => {
    count[t.emotion] = (count[t.emotion] || 0) + 1
  })
  return count
}

export function listAllEmotions(day: DiaryDay): EmotionStat[] {
  const count = countEmotions(day)
  const list: EmotionStat[] = Object.entries(count).map(([emotion, n]) => {
    const maxRisk = day.turns
      .filter(t => t.emotion === emotion)
      .reduce((acc, t) => Math.max(acc, RISK_SCORE[t.riskLevel]), 0)
    return { emotion, count: n, maxRisk }
  })

  list.sort((a, b) => {
    if (b.count !== a.count) return b.count - a.count
    if (b.maxRisk !== a.maxRisk) return b.maxRisk - a.maxRisk
    return a.emotion.localeCompare(b.emotion, 'zh')
  })

  return list
}

export function getDominantEmotion(day: DiaryDay): string {
  const list = listAllEmotions(day)
  return list.length ? list[0].emotion : '未知'
}

function buildSegments(day: DiaryDay): string[] {
  const turns = day.turns
  if (turns.length <= MAX_SEGMENTS) {
    return turns.map(t => emotionMeta(t.emotion).color)
  }
  const head = turns.slice(0, MAX_SEGMENTS - 1).map(t => emotionMeta(t.emotion).color)
  return [...head, 'var(--text-dim)']
}

function makeCell(day: number, inMonth: boolean, dateObj: Date): CalCell {
  const dateKey = formatDate(dateObj)
  const dayData = findDay(dateKey)
  const hasData = !!dayData
  return {
    day,
    inMonth,
    dateObj,
    dateKey,
    hasData,
    color: hasData ? emotionMeta(getDominantEmotion(dayData!)).color : '',
    segments: hasData ? buildSegments(dayData!) : [],
  }
}

export function buildCalendarCells(year: number, month: number): CalCell[] {
  const firstDay = new Date(year, month - 1, 1)
  const daysInMonth = new Date(year, month, 0).getDate()
  const startOffset = (firstDay.getDay() + 6) % 7
  const prevMonthDays = new Date(year, month - 1, 0).getDate()
  const totalCells = Math.ceil((startOffset + daysInMonth) / 7) * 7

  const cells: CalCell[] = []

  for (let i = startOffset; i > 0; i--) {
    const d = prevMonthDays - i + 1
    cells.push(makeCell(d, false, new Date(year, month - 2, d)))
  }
  for (let d = 1; d <= daysInMonth; d++) {
    cells.push(makeCell(d, true, new Date(year, month - 1, d)))
  }
  let nextDay = 1
  while (cells.length < totalCells) {
    cells.push(makeCell(nextDay, false, new Date(year, month, nextDay)))
    nextDay++
  }

  return cells
}
```

---

## 5. `src/utils/device.ts`

```ts
export function isMobile(): boolean {
  return window.matchMedia('(max-width: 960px)').matches
}
```

---

## 6. `src/stores/diary.ts`

```ts
import { computed, ref } from 'vue'
import { DIARY } from '@/config/diaryData'
import {
  buildCalendarCells,
  findDay,
  listAllEmotions,
  turnId,
} from '@/utils/diary'

const FIRST = DIARY[0]

/* ---------- state ---------- */
const currentYear = ref(Number(FIRST.date.slice(0, 4)))
const currentMonth = ref(Number(FIRST.date.slice(5, 7)))
const activeDate = ref(FIRST.date)
const selectedTurnId = ref<string | null>(turnId(FIRST.date, FIRST.turns.length - 1))
const highlightedEmotion = ref<string | null>(null)
const calendarCollapsed = ref(false)
const panelOpen = ref(false)

/* ---------- derived ---------- */
const activeDay = computed(() => findDay(activeDate.value) ?? null)

const dayTurnCount = computed(() => activeDay.value?.turns.length ?? 0)

const calendarCells = computed(() =>
  buildCalendarCells(currentYear.value, currentMonth.value),
)

const selected = computed(() => {
  const id = selectedTurnId.value
  if (!id) return null
  const [date, idxStr] = id.split('__')
  const day = findDay(date)
  if (!day) return null
  const index = Number(idxStr)
  const turn = day.turns[index]
  if (!turn) return null
  return { date, weekday: day.weekday, day, index, turn }
})

const panelEmotions = computed(() =>
  selected.value ? listAllEmotions(selected.value.day) : [],
)

const dominantEmotion = computed(() => panelEmotions.value[0]?.emotion ?? '未知')

/* ---------- actions ---------- */
function selectDate(dateKey: string, dateObj: Date, inMonth: boolean) {
  if (!inMonth) {
    currentYear.value = dateObj.getFullYear()
    currentMonth.value = dateObj.getMonth() + 1
  }
  activeDate.value = dateKey
  highlightedEmotion.value = null

  const d = findDay(dateKey)
  selectedTurnId.value =
    d && d.turns.length > 0 ? turnId(dateKey, d.turns.length - 1) : null
}

function prevMonth() {
  currentMonth.value--
  if (currentMonth.value < 1) {
    currentMonth.value = 12
    currentYear.value--
  }
}

function nextMonth() {
  currentMonth.value++
  if (currentMonth.value > 12) {
    currentMonth.value = 1
    currentYear.value++
  }
}

function selectTurn(id: string) {
  selectedTurnId.value = id
}

function toggleEmotion(e: string) {
  highlightedEmotion.value = highlightedEmotion.value === e ? null : e
}

function toggleCalendar() {
  calendarCollapsed.value = !calendarCollapsed.value
}

function openPanel() {
  panelOpen.value = true
}

function closePanel() {
  panelOpen.value = false
}

export function useDiaryStore() {
  return {
    // state
    currentYear,
    currentMonth,
    activeDate,
    selectedTurnId,
    highlightedEmotion,
    calendarCollapsed,
    panelOpen,

    // derived
    activeDay,
    dayTurnCount,
    calendarCells,
    selected,
    panelEmotions,
    dominantEmotion,

    // actions
    selectDate,
    prevMonth,
    nextMonth,
    selectTurn,
    toggleEmotion,
    toggleCalendar,
    openPanel,
    closePanel,
  }
}
```

---

## 7. `src/components/diary/DiaryToolbar.vue`

```vue
<script setup lang="ts">
import { THEMES } from '@/config/emotionMeta'
import { useTheme } from '@/composables/useTheme'

// 若你的 useTheme 没有导出 setTheme，请替换成对应方法名
const { theme, setTheme } = useTheme()
</script>

<template>
  <header class="toolbar">
    <div class="toolbar__title">
      <h1>情绪记录</h1>
      <p>当天全部情绪 · 一览无余</p>
    </div>
    <div class="toolbar__spacer" />
    <div class="themes">
      <button
        v-for="t in THEMES"
        :key="t.value"
        class="theme-btn"
        :class="{ 'is-active': theme === t.value }"
        :title="t.title"
        type="button"
        @click="setTheme(t.value)"
      >
        {{ t.emoji }}
      </button>
    </div>
  </header>
</template>

<style scoped>
.toolbar {
  grid-column: 1 / -1;
  grid-row: 1;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 14px 22px;
  background: var(--panel-bg);
  border-bottom: 1px solid var(--panel-border);
  backdrop-filter: blur(16px) saturate(120%);
  -webkit-backdrop-filter: blur(16px) saturate(120%);
  transition: background 0.6s ease, border-color 0.6s ease;
  z-index: 5;
}
.toolbar__title h1 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1.2px;
  color: var(--text);
  transition: color 0.6s ease;
}
.toolbar__title p {
  margin: 2px 0 0;
  font-size: 11px;
  letter-spacing: 0.5px;
  color: var(--text-sub);
  transition: color 0.6s ease;
}
.toolbar__spacer {
  flex: 1;
}
.themes {
  display: flex;
  gap: 3px;
  padding: 3px;
  border-radius: 999px;
  background: var(--card-bg);
  border: 1px solid var(--panel-border);
  transition: background 0.6s ease, border-color 0.6s ease;
}
.theme-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  display: grid;
  place-items: center;
  transition: background 0.3s, transform 0.2s;
}
.theme-btn:hover {
  transform: scale(1.12);
}
.theme-btn.is-active {
  background: var(--card-hover);
}
</style>
```

---

## 8. `src/components/diary/DiaryCalendar.vue`

```vue
<script setup lang="ts">
import { CALENDAR_WEEKDAYS } from '@/config/emotionMeta'
import { useDiaryStore } from '@/stores/diary'
import type { CalCell } from '@/types/diary'

const {
  currentYear,
  currentMonth,
  calendarCollapsed,
  activeDate,
  calendarCells,
  selectDate,
  prevMonth,
  nextMonth,
  toggleCalendar,
} = useDiaryStore()

function onCellClick(cell: CalCell) {
  selectDate(cell.dateKey, cell.dateObj, cell.inMonth)
}
</script>

<template>
  <section class="calendar" :class="{ 'is-collapsed': calendarCollapsed }">
    <div class="calendar__inner">
      <div class="calendar__head">
        <div class="month-nav">
          <button class="month-nav__btn" type="button" aria-label="上个月" @click="prevMonth">
            ‹
          </button>
          <span class="month-nav__label">{{ currentYear }} 年 {{ currentMonth }} 月</span>
          <button class="month-nav__btn" type="button" aria-label="下个月" @click="nextMonth">
            ›
          </button>
        </div>

        <button
          class="calendar__toggle"
          type="button"
          aria-label="收起或展开日历"
          @click="toggleCalendar"
        >
          <span>{{ calendarCollapsed ? '展开日历' : '收起日历' }}</span>
          <span class="calendar__toggle-arrow">▾</span>
        </button>
      </div>

      <div class="calendar__body">
        <div class="calendar__weekdays">
          <span v-for="w in CALENDAR_WEEKDAYS" :key="w">{{ w }}</span>
        </div>

        <div class="calendar__grid">
          <button
            v-for="cell in calendarCells"
            :key="cell.dateKey"
            type="button"
            class="cal-cell"
            :class="{
              'is-outside': !cell.inMonth,
              'has-data': cell.hasData,
              'is-selected': cell.dateKey === activeDate,
            }"
            :style="cell.color ? { '--cell-color': cell.color } : undefined"
            @click="onCellClick(cell)"
          >
            <span class="cal-cell__day">{{ cell.day }}</span>
            <span class="cal-cell__bar">
              <span
                v-for="(seg, i) in cell.segments"
                :key="i"
                class="cal-cell__seg"
                :style="{ background: seg }"
              />
            </span>
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.calendar {
  grid-column: 1 / -1;
  grid-row: 2;
  padding: 14px 22px 16px;
  background: var(--panel-bg);
  border-bottom: 1px solid var(--panel-border);
  backdrop-filter: blur(16px) saturate(120%);
  -webkit-backdrop-filter: blur(16px) saturate(120%);
  transition: background 0.6s ease, border-color 0.6s ease, padding 0.35s ease;
  z-index: 4;
}
.calendar.is-collapsed {
  padding-top: 10px;
  padding-bottom: 10px;
}
.calendar__inner {
  max-width: 640px;
  margin: 0 auto;
  padding: 12px 16px;
  border-radius: 16px;
  background: var(--card-bg);
  border: 1px solid var(--panel-border);
  transition: background 0.6s ease, border-color 0.6s ease;
}
.calendar__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.month-nav {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  border-radius: 999px;
  background: var(--panel-bg);
  border: 1px solid var(--panel-border);
  transition: background 0.6s ease, border-color 0.6s ease;
}
.month-nav__btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--text-sub);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: color 0.2s, background 0.2s, transform 0.15s;
}
.month-nav__btn:hover {
  color: var(--text);
  background: var(--card-hover);
}
.month-nav__btn:active {
  transform: scale(0.9);
}
.month-nav__label {
  padding: 0 8px;
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.5px;
  color: var(--text);
  min-width: 100px;
  text-align: center;
}
.calendar__toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: var(--text-sub);
  font-family: inherit;
  font-size: 11px;
  letter-spacing: 0.4px;
  cursor: pointer;
  transition: background 0.25s, color 0.25s;
}
.calendar__toggle:hover {
  background: var(--card-hover);
  color: var(--text);
}
.calendar__toggle-arrow {
  display: inline-block;
  font-size: 10px;
  transition: transform 0.35s ease;
}
.calendar.is-collapsed .calendar__toggle-arrow {
  transform: rotate(-180deg);
}
.calendar__body {
  margin-top: 8px;
  max-height: 480px;
  overflow: hidden;
  opacity: 1;
  transition: max-height 0.4s ease, opacity 0.3s ease, margin-top 0.35s ease;
}
.calendar.is-collapsed .calendar__body {
  max-height: 0;
  opacity: 0;
  margin-top: 0;
}
.calendar__weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 6px;
}
.calendar__weekdays span {
  text-align: center;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.1em;
  color: var(--text-dim);
  padding: 2px 0;
}
.calendar__grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.cal-cell {
  position: relative;
  height: 42px;
  border-radius: 10px;
  border: 1px solid transparent;
  background: transparent;
  cursor: pointer;
  font-family: inherit;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 4px 6px;
  transition: background 0.25s, border-color 0.25s, transform 0.15s, opacity 0.3s;
}
.cal-cell:hover {
  background: var(--card-hover);
}
.cal-cell:active {
  transform: scale(0.94);
}
.cal-cell.is-outside {
  opacity: 0.3;
}
.cal-cell.has-data {
  background: color-mix(in srgb, var(--cell-color) 12%, transparent);
  border-color: color-mix(in srgb, var(--cell-color) 26%, transparent);
}
.cal-cell.has-data:hover {
  background: color-mix(in srgb, var(--cell-color) 22%, transparent);
}
.cal-cell.is-selected {
  background: color-mix(in srgb, var(--cell-color, #ff9a76) 30%, transparent);
  border-color: color-mix(in srgb, var(--cell-color, #ff9a76) 65%, transparent);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--cell-color, #ff9a76) 22%, transparent);
  opacity: 1;
}
.cal-cell__day {
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0.3px;
  color: var(--text);
  line-height: 1;
}
.cal-cell__bar {
  display: flex;
  width: 100%;
  height: 5px;
  border-radius: 3px;
  overflow: hidden;
  gap: 1px;
}
.cal-cell__seg {
  flex: 1;
  min-width: 3px;
  border-radius: 1px;
}

@media (max-width: 960px) {
  .calendar {
    padding: 10px 14px 12px;
  }
  .calendar__inner {
    padding: 10px 12px;
    border-radius: 14px;
  }
  .month-nav__label {
    min-width: 88px;
    font-size: 12px;
  }
  .cal-cell {
    height: 38px;
    border-radius: 8px;
  }
  .cal-cell__day {
    font-size: 11px;
  }
  .calendar__weekdays span {
    font-size: 9px;
  }
}
</style>
```

---

## 9. `src/components/diary/TurnCard.vue`

```vue
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
```

---

## 10. `src/components/diary/DiaryStream.vue`

```vue
<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import TurnCard from './TurnCard.vue'
import { useDiaryStore } from '@/stores/diary'
import { countEmotions, turnId } from '@/utils/diary'
import { isMobile } from '@/utils/device'

const {
  activeDate,
  activeDay,
  dayTurnCount,
  selectedTurnId,
  highlightedEmotion,
  selectTurn,
  openPanel,
} = useDiaryStore()

const streamEl = ref<HTMLElement | null>(null)

const dayLabel = computed(() => {
  const day = activeDay.value
  if (!day) return ''
  const total = day.turns.length
  const unique = Object.keys(countEmotions(day)).length
  return `${day.date} · ${day.weekday} · ${total} 轮 · ${unique} 种情绪`
})

const items = computed(() => {
  const day = activeDay.value
  if (!day) return []
  return day.turns.map((turn, idx) => {
    const id = turnId(day.date, idx)
    const isMatch = !highlightedEmotion.value || turn.emotion === highlightedEmotion.value
    return {
      id,
      idx,
      turn,
      isSelected: id === selectedTurnId.value,
      isDimmed: !isMatch,
      isHighlighted: !!highlightedEmotion.value && isMatch,
    }
  })
})

function handleSelect(id: string) {
  selectTurn(id)
  if (isMobile()) openPanel()
}

async function scrollToBottom() {
  await nextTick()
  const el = streamEl.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(activeDate, scrollToBottom)
onMounted(scrollToBottom)
</script>

<template>
  <main ref="streamEl" class="stream">
    <div class="stream__inner">
      <div v-if="!activeDay || dayTurnCount === 0" class="stream__empty">
        这一天还没有记录
      </div>

      <template v-else>
        <div class="stream__day">{{ dayLabel }}</div>

        <TurnCard
          v-for="item in items"
          :key="item.id"
          :turn="item.turn"
          :index="item.idx"
          :total="dayTurnCount"
          :selected="item.isSelected"
          :dimmed="item.isDimmed"
          :highlighted="item.isHighlighted"
          @select="handleSelect(item.id)"
        />
      </template>
    </div>
  </main>
</template>

<style scoped>
.stream {
  grid-column: 1;
  grid-row: 3;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px 22px 40px;
  scrollbar-width: thin;
  scrollbar-color: var(--scroll) transparent;
}
.stream::-webkit-scrollbar {
  width: 6px;
}
.stream::-webkit-scrollbar-thumb {
  background: var(--scroll);
  border-radius: 3px;
}
.stream__inner {
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 22px;
}
.stream__day {
  text-align: center;
  font-size: 12px;
  letter-spacing: 1.5px;
  color: var(--text-dim);
  margin: 4px 0 6px;
}
.stream__empty {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-dim);
  font-size: 13px;
  letter-spacing: 1px;
}

@media (max-width: 960px) {
  .stream {
    padding: 16px 14px 80px;
  }
}
</style>
```

---

## 11. `src/components/diary/EmotionTag.vue`

```vue
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
  <button
    type="button"
    class="p-emotion-tag"
    :class="{ 'is-active': active }"
    :style="{
      color: meta.color,
      background: meta.color + '20',
      borderColor: meta.color + '60',
    }"
    @click="emit('click')"
  >
    <span class="emoji p-emotion-tag__emoji">{{ meta.emoji }}</span>
    <span>{{ emotion }}</span>
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
  border: 1px solid transparent;
  background: transparent;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.25s ease, filter 0.25s ease, opacity 0.25s ease;
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
  opacity: 0.7;
  margin-left: 1px;
}
.p-emotion-tag.is-active {
  transform: translateY(-1px) scale(1.04);
  box-shadow: 0 0 0 2px color-mix(in srgb, currentColor 60%, transparent),
    0 4px 14px -4px color-mix(in srgb, currentColor 60%, transparent);
}
</style>
```

---

## 12. `src/components/diary/DiaryDetailPanel.vue`

```vue
<script setup lang="ts">
import EmotionTag from './EmotionTag.vue'
import { useDiaryStore } from '@/stores/diary'
import { emotionMeta, riskMeta } from '@/utils/diary'

const {
  selected,
  panelEmotions,
  dominantEmotion,
  highlightedEmotion,
  panelOpen,
  toggleEmotion,
  closePanel,
} = useDiaryStore()
</script>

<template>
  <aside class="panel" :class="{ 'is-open': panelOpen }">
    <div class="panel__inner">
      <template v-if="selected">
        <div class="panel__head">
          <span class="panel__head-label">
            Turn {{ selected.index + 1 }} / {{ selected.day.turns.length }}
          </span>
          <span class="panel__head-time">
            {{ selected.date }} {{ selected.turn.time }}
          </span>
          <span class="panel__head-sub">
            {{ selected.weekday }} · 当天第 {{ selected.index + 1 }} 条
          </span>
        </div>

        <!-- emotion -->
        <div class="field">
          <div class="field__label">
            <code>emotion</code><span>当前情绪</span>
          </div>
          <div
            class="p-emotion"
            :style="{
              color: emotionMeta(selected.turn.emotion).color,
              background: emotionMeta(selected.turn.emotion).color + '20',
              borderColor: emotionMeta(selected.turn.emotion).color + '60',
            }"
          >
            <span class="emoji p-emotion__emoji">
              {{ emotionMeta(selected.turn.emotion).emoji }}
            </span>
            <span>{{ selected.turn.emotion }}</span>
          </div>
        </div>

        <!-- riskLevel -->
        <div
          class="field"
          :style="{
            '--risk-color': riskMeta(selected.turn.riskLevel).hex,
            '--risk-rgb': riskMeta(selected.turn.riskLevel).rgb,
          }"
        >
          <div class="field__label">
            <code>riskLevel</code><span>风险等级</span>
          </div>
          <div class="p-risk">
            <span class="p-risk__dot" />
            <span>{{ riskMeta(selected.turn.riskLevel).label }}</span>
            <span class="p-risk__code">{{ selected.turn.riskLevel }}</span>
          </div>
        </div>

        <!-- suggestions -->
        <div class="field">
          <div class="field__label">
            <code>suggestions</code><span>建议</span>
          </div>
          <ul class="p-sugg">
            <li v-for="(s, i) in selected.turn.suggestions" :key="i">
              <span class="p-sugg__idx">{{ i + 1 }}</span>
              <span>{{ s }}</span>
            </li>
          </ul>
        </div>

        <!-- 当天情绪 -->
        <div class="p-today">
          <div class="p-today__head">
            <span class="p-today__label">当天情绪</span>
            <span class="p-today__count">
              {{ panelEmotions.length }} 种 / {{ selected.day.turns.length }} 轮
            </span>
          </div>

          <div class="p-today__tags" :class="{ 'has-active': !!highlightedEmotion }">
            <EmotionTag
              v-for="stat in panelEmotions"
              :key="stat.emotion"
              :emotion="stat.emotion"
              :count="stat.count"
              :active="highlightedEmotion === stat.emotion"
              @click="toggleEmotion(stat.emotion)"
            />
          </div>

          <div class="p-today__hint">
            {{
              highlightedEmotion
                ? `已筛选「${highlightedEmotion}」，再点一次取消`
                : '点击标签筛选对应轮次'
            }}
          </div>

          <div class="p-dominant">
            <span class="emoji p-dominant__emoji">
              {{ emotionMeta(dominantEmotion).emoji }}
            </span>
            <div class="p-dominant__text">
              <span class="p-dominant__label">主导情绪</span>
              <span
                class="p-dominant__value"
                :style="{ color: emotionMeta(dominantEmotion).color }"
              >
                {{ dominantEmotion }}
              </span>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="panel__empty">选择左侧任一记录</div>
    </div>
  </aside>
</template>

<style scoped>
.panel {
  grid-column: 2;
  grid-row: 3;
  overflow-y: auto;
  padding: 24px 22px;
  border-left: 1px solid var(--panel-border);
  background: var(--panel-bg);
  backdrop-filter: blur(16px) saturate(120%);
  -webkit-backdrop-filter: blur(16px) saturate(120%);
  transition: background 0.6s ease, border-color 0.6s ease;
  scrollbar-width: thin;
  scrollbar-color: var(--scroll) transparent;
}
.panel::-webkit-scrollbar {
  width: 5px;
}
.panel::-webkit-scrollbar-thumb {
  background: var(--scroll);
  border-radius: 3px;
}
.panel__inner {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.panel__head {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--panel-border);
}
.panel__head-label {
  font-size: 10px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-sub);
}
.panel__head-time {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.8px;
  color: var(--text);
}
.panel__head-sub {
  font-size: 11px;
  color: var(--text-dim);
}
.panel__empty {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-dim);
  font-size: 12px;
  letter-spacing: 1px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 9px;
}
.field__label {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 10px;
  letter-spacing: 0.1em;
  color: var(--text-sub);
  text-transform: uppercase;
}
.field__label code {
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  font-weight: 600;
  font-size: 10px;
  opacity: 0.9;
}
.field__label span {
  letter-spacing: 0.18em;
  opacity: 0.7;
}

.p-emotion {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px 7px 10px;
  border-radius: 999px;
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.4px;
  border: 1px solid transparent;
  transition: all 0.3s;
}
.p-emotion__emoji {
  font-size: 18px;
}

.p-risk {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
  color: var(--risk-color);
  background: rgba(var(--risk-rgb), var(--risk-bg-alpha));
  border: 1px solid rgba(var(--risk-rgb), var(--risk-border-alpha));
  text-shadow: var(--risk-text-glow);
}
.p-risk__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--risk-color);
  box-shadow: 0 0 0 3px rgba(var(--risk-rgb), 0.22);
  animation: riskPulse 2.2s ease-in-out infinite;
}
@keyframes riskPulse {
  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 3px rgba(var(--risk-rgb), 0.22);
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
    box-shadow: 0 0 0 6px rgba(var(--risk-rgb), 0.08);
  }
}
.p-risk__code {
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  font-size: 9px;
  letter-spacing: 0.12em;
  opacity: 0.65;
}

.p-sugg {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 9px;
}
.p-sugg li {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  font-size: 12.5px;
  line-height: 1.55;
  color: var(--text);
}
.p-sugg__idx {
  flex: 0 0 auto;
  width: 18px;
  height: 18px;
  margin-top: 1px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 10px;
  font-weight: 700;
  color: var(--text);
  background: var(--idx-bg);
}

.p-today {
  margin-top: 8px;
  padding-top: 14px;
  border-top: 1px solid var(--panel-border);
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.p-today__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.p-today__label {
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-sub);
}
.p-today__count {
  font-size: 10px;
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  color: var(--text-dim);
}
.p-today__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.p-today__tags.has-active :deep(.p-emotion-tag:not(.is-active)) {
  opacity: 0.35;
}
.p-today__hint {
  font-size: 10px;
  letter-spacing: 0.3px;
  color: var(--text-dim);
  opacity: 0.8;
}

.p-dominant {
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  display: flex;
  align-items: center;
  gap: 10px;
  transition: background 0.6s ease, border-color 0.6s ease;
}
.p-dominant__emoji {
  font-size: 20px;
}
.p-dominant__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.p-dominant__label {
  font-size: 9px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-sub);
}
.p-dominant__value {
  font-size: 13px;
  font-weight: 700;
}

@media (max-width: 960px) {
  .panel {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    top: auto;
    max-height: 64vh;
    border-left: none;
    border-top: 1px solid var(--panel-border);
    border-radius: 22px 22px 0 0;
    transform: translateY(100%);
    transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1), background 0.6s ease,
      border-color 0.6s ease;
    z-index: 30;
    box-shadow: 0 -20px 60px -20px rgba(0, 0, 0, 0.4);
    padding: 18px 20px 28px;
  }
  .panel.is-open {
    transform: translateY(0);
  }
  .panel::before {
    content: '';
    display: block;
    width: 36px;
    height: 4px;
    margin: -4px auto 12px;
    border-radius: 2px;
    background: var(--text-dim);
  }
}
</style>
```

---

## 13. `src/view/EmotionDiaryView.vue`

```vue
<script setup lang="ts">
import DiaryToolbar from '@/components/diary/DiaryToolbar.vue'
import DiaryCalendar from '@/components/diary/DiaryCalendar.vue'
import DiaryStream from '@/components/diary/DiaryStream.vue'
import DiaryDetailPanel from '@/components/diary/DiaryDetailPanel.vue'
import { useDiaryStore } from '@/stores/diary'
import { useTheme } from '@/composables/useTheme'
import { isMobile } from '@/utils/device'
import { onMounted, onUnmounted } from 'vue'

const { panelOpen, closePanel } = useDiaryStore()
const { theme } = useTheme()

function onResize() {
  if (!isMobile()) closePanel()
}

onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))
</script>

<template>
  <div class="emotion-diary-page" :data-theme="theme">
    <div class="bg-layer" />

    <div class="app">
      <DiaryToolbar />
      <DiaryCalendar />
      <DiaryStream />
      <DiaryDetailPanel />
    </div>

    <div
      class="panel-mask"
      :class="{ 'is-open': panelOpen }"
      @click="closePanel"
    />
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
.emoji {
  font-family: 'Apple Color Emoji', 'Segoe UI Emoji', 'Noto Color Emoji',
    'Segoe UI Symbol', sans-serif;
  font-style: normal;
  line-height: 1;
}

/* ============================================================
 * 主题变量（根节点定义，子组件通过继承使用）
 * ============================================================ */
.emotion-diary-page {
  --text: #6b4a3a;
  --text-sub: #9c7a68;
  --text-dim: rgba(107, 74, 58, 0.42);
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

  /* 风险徽章在不同主题下的透明度 / 发光（子组件通过 var() 消费） */
  --risk-bg-alpha: 0.14;
  --risk-border-alpha: 0.42;
  --risk-text-glow: none;
}

.emotion-diary-page[data-theme='dusk'] {
  --text: #ffffff;
  --text-sub: rgba(255, 255, 255, 0.82);
  --text-dim: rgba(255, 255, 255, 0.5);
  --panel-bg: rgba(255, 255, 255, 0.16);
  --panel-border: rgba(255, 255, 255, 0.32);
  --panel-shadow: 0 8px 24px rgba(60, 30, 80, 0.22);
  --card-bg: rgba(255, 255, 255, 0.12);
  --card-border: rgba(255, 255, 255, 0.28);
  --card-hover: rgba(255, 255, 255, 0.2);
  --card-active: rgba(255, 255, 255, 0.26);
  --user-bg: linear-gradient(135deg, #e88ba0, #a87ad8);
  --user-text: #ffffff;
  --idx-bg: rgba(255, 255, 255, 0.22);
  --scroll: rgba(255, 255, 255, 0.2);
}

.emotion-diary-page[data-theme='night'] {
  --text: #f0efff;
  --text-sub: #a8a8d0;
  --text-dim: rgba(200, 200, 255, 0.4);
  --panel-bg: rgba(255, 255, 255, 0.07);
  --panel-border: rgba(200, 200, 255, 0.18);
  --panel-shadow: 0 0 24px rgba(120, 140, 255, 0.14);
  --card-bg: rgba(255, 255, 255, 0.05);
  --card-border: rgba(200, 200, 255, 0.16);
  --card-hover: rgba(255, 255, 255, 0.1);
  --card-active: rgba(255, 255, 255, 0.14);
  --user-bg: linear-gradient(135deg, rgba(140, 160, 255, 0.34), rgba(180, 140, 255, 0.28));
  --user-text: #f0efff;
  --idx-bg: rgba(200, 200, 255, 0.14);
  --scroll: rgba(200, 200, 255, 0.18);

  --risk-bg-alpha: 0.18;
  --risk-border-alpha: 0.52;
  --risk-text-glow: 0 0 10px rgba(var(--risk-rgb), 0.45);
}

.emotion-diary-page[data-theme='midnight'] {
  --text: #c8c8e8;
  --text-sub: #7a7a9c;
  --text-dim: rgba(150, 160, 220, 0.35);
  --panel-bg: rgba(255, 255, 255, 0.045);
  --panel-border: rgba(150, 160, 220, 0.16);
  --panel-shadow: 0 0 20px rgba(100, 120, 220, 0.1);
  --card-bg: rgba(255, 255, 255, 0.035);
  --card-border: rgba(150, 160, 220, 0.14);
  --card-hover: rgba(255, 255, 255, 0.07);
  --card-active: rgba(255, 255, 255, 0.1);
  --user-bg: linear-gradient(135deg, rgba(120, 140, 255, 0.26), rgba(150, 120, 220, 0.22));
  --user-text: #e0e0ff;
  --idx-bg: rgba(150, 160, 220, 0.14);
  --scroll: rgba(150, 160, 220, 0.16);

  --risk-bg-alpha: 0.18;
  --risk-border-alpha: 0.52;
  --risk-text-glow: 0 0 10px rgba(var(--risk-rgb), 0.45);
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
  background: linear-gradient(
    180deg,
    #ffc48c 0%,
    #ff9a8b 22%,
    #e88ba0 45%,
    #b87ab0 68%,
    #7a6ba8 88%,
    #5a5a8c 100%
  );
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
.app {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 380px;
  grid-template-rows: auto auto 1fr;
  width: 100%;
  height: 100%;
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
  .app {
    grid-template-columns: 1fr;
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
```

---

## 重构说明

**分层逻辑：**

| 层 | 职责 | 文件 |
|---|---|---|
| **types** | 全部 TS 类型定义 | `types/diary.ts` |
| **config** | 静态元数据 / 静态数据 | `config/emotionMeta.ts`、`config/diaryData.ts` |
| **utils** | 纯函数（无副作用、无状态） | `utils/diary.ts`、`utils/device.ts` |
| **stores** | 模块级单例状态 + actions | `stores/diary.ts` |
| **components** | 各司其职的展示组件 | `components/diary/*.vue` |
| **view** | 只负责组装 + 全局副作用（resize） | `view/EmotionDiaryView.vue` |

**几个关键点：**

1. **状态单例在 `stores/diary.ts`**：所有 `ref`/`computed` 定义在模块顶部，多个组件调用 `useDiaryStore()` 拿到同一份状态，无需 prop drilling。后续想换 Pinia 也只需把这一段搬过去。

2. **风险徽章的跨主题样式**：原来用 `.emotion-diary-page[data-theme='night'] .card__risk { ... }` 这种后代选择器，拆成 scoped 组件后会失效，所以改成在根节点上暴露 `--risk-bg-alpha` / `--risk-border-alpha` / `--risk-text-glow` 三个 CSS 变量，子组件直接 `var()` 消费。

3. **滚动到底部**逻辑收进 `DiaryStream`，由 `watch(activeDate)` 触发，视图层不再关心。

4. **移动端检测** 抽成 `utils/device.ts`，`DiaryStream` 和视图层共用。

5. **`EmotionTag`** 拆成独立组件，`DiaryDetailPanel` 里的 `:deep()` 只用来处理 `.has-active` 的置灰规则。

6. **`useTheme`** 我按 `{ theme, setTheme }` 的 API 写法用了，如果你的 `useTheme` 里叫别的名字（比如 `setThemeName` / `changeTheme`），改 `DiaryToolbar.vue` 那一行即可。

**后续可继续优化的方向：**

- `config/diaryData.ts` 换成 `api/diary.ts`，用 `fetch` + 缓存替换静态数组
- `stores/diary.ts` 迁移到 Pinia，加上 `loadDiary()` action
- `TurnCard.vue` 里的 `emoji` 样式类如果全项目通用，可以提到全局 `assets/styles/base.css`