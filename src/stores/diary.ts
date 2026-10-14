// src/stores/diary.ts
// 模块级单例状态 + actions：多个组件调用 useDiaryStore() 拿到同一份状态，无需 prop drilling
import { computed, reactive, ref } from 'vue'
import { DIARY } from '@/config/diaryData'
import {
  buildCalendarCells,
  findDay,
  formatDate,
  listAllEmotions,
  pad,
  turnId,
} from '@/utils/diary'
import type { DiaryDay, Turn } from '@/types/diary'
import { getDiaryDay, getDiaryDays, type DiaryDayVO } from '@/utils/diaryApi'

/* ---------- state ---------- */
/** 首次进入时以「今天」为焦点；真实数据由 loadMonth / loadDay 从后端填充 */
const today = formatDate(new Date())

const currentYear = ref(Number(today.slice(0, 4)))
const currentMonth = ref(Number(today.slice(5, 7)))
const activeDate = ref(today)
const selectedTurnId = ref<string | null>(null)
const highlightedEmotion = ref<string | null>(null)

/**
 * ★ 默认收起：首次进入 diary 页时日历就是折叠状态。
 * 每次重新进入由 EmotionDiaryView.onMounted 再次强制重置。
 */
const calendarCollapsed = ref(true)

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
  const parts = id.split('__')
  const date = parts[0] ?? ''
  const index = Number(parts[1] ?? '')
  const day = findDay(date)
  if (!day) return null
  const turn = day.turns[index]
  if (!turn) return null
  return { date, weekday: day.weekday, day, index, turn }
})

const panelEmotions = computed(() =>
  selected.value ? listAllEmotions(selected.value.day) : [],
)

const dominantEmotion = computed(
  () => panelEmotions.value[0]?.emotion ?? '未知',
)

/* ---------- helpers ---------- */
const WEEKDAY_CN = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

function getWeekday(dateKey: string): string {
  const d = new Date(`${dateKey}T00:00:00`)
  return Number.isNaN(d.getTime()) ? '' : WEEKDAY_CN[d.getDay()]!
}

/** 把日期/月份状态对齐到某一天 */
function focusDate(dateKey: string) {
  activeDate.value = dateKey
  currentYear.value = Number(dateKey.slice(0, 4))
  currentMonth.value = Number(dateKey.slice(5, 7))
  highlightedEmotion.value = null
}

/** 若当前没有选中轮次，且 activeDate 已加载出数据，则默认选中最新一轮 */
function ensureSelection() {
  if (selectedTurnId.value) return
  const day = findDay(activeDate.value)
  if (day && day.turns.length > 0) {
    selectedTurnId.value = turnId(activeDate.value, day.turns.length - 1)
  }
}

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

/**
 * ★ 向某一天追加一轮对话。
 * - 该日已存在 → push 到 turns
 * - 不存在   → 新建 DiaryDay 并按日期倒序插入
 * - 自动跳转到该日 + 选中新的一轮（详情面板/日历同步刷新）
 * 返回新一轮的下标。
 */
function appendTurn(dateKey: string, turn: Turn): number {
  let day = findDay(dateKey)

  if (!day) {
    const created = reactive<DiaryDay>({
      date: dateKey,
      weekday: getWeekday(dateKey),
      turns: [],
    })
    DIARY.push(created)
    // 保持倒序（最新在前）
    DIARY.sort((a, b) => (a.date < b.date ? 1 : -1))
    day = findDay(dateKey)!
  }

  day.turns.push(turn)

  const idx = day.turns.length - 1
  focusDate(dateKey)
  selectedTurnId.value = turnId(dateKey, idx)
  return idx
}

/* ---------- 后端读取缓存 ---------- */
/** 已成功请求过的「单日」；避免同一天反复拉 */
const loadedDates = new Set<string>()
/** 已成功请求过的「整月」，key = YYYY-MM；避免切月反复拉 */
const loadedMonths = new Set<string>()

/**
 * 把后端某天合并进本地 DIARY。
 * - 不存在 → 新建并按日期倒序插入
 * - 已存在但本地 turn 更多 → 保留本地（保护尚未落库的乐观更新）
 * - 否则 → 用后端数据覆盖
 */
function mergeServerDay(data: DiaryDayVO) {
  const turns = data.turns as unknown as Turn[]
  const existing = DIARY.find((d) => d.date === data.date)

  if (existing) {
    if (turns.length >= existing.turns.length) {
      existing.weekday = data.weekday
      existing.turns = turns
    }
  } else {
    const created = reactive<DiaryDay>({
      date: data.date,
      weekday: data.weekday,
      turns,
    })
    DIARY.push(created)
    DIARY.sort((a, b) => (a.date < b.date ? 1 : -1))
  }
}

/** 按日期拉取某天；命中缓存直接返回。force = true 时强制重拉。 */
async function loadDay(dateKey: string, force = false) {
  if (!force && loadedDates.has(dateKey)) return
  loadedDates.add(dateKey)

  try {
    const data = await getDiaryDay(dateKey)
    if (!data) return
    mergeServerDay(data)
    ensureSelection()
  } catch (err) {
    loadedDates.delete(dateKey) // 失败后允许下次重试
    console.error('[diary] 加载某天失败:', err)
  }
}

/**
 * 按年月拉取整月数据（用于日历色点预取）。
 * 成功后把该月每一天都标记为「单日已加载」，之后点日历不再重复请求。
 */
async function loadMonth(year: number, month: number, force = false) {
  const key = `${year}-${pad(month)}`
  if (!force && loadedMonths.has(key)) return
  loadedMonths.add(key)

  const lastDay = new Date(year, month, 0).getDate()
  const from = `${year}-${pad(month)}-01`
  const to = `${year}-${pad(month)}-${pad(lastDay)}`

  try {
    const days = await getDiaryDays(from, to)
    for (const day of days) {
      mergeServerDay(day)
      loadedDates.add(day.date) // 整月已含完整 turns，单日无需再拉
    }
    ensureSelection()
  } catch (err) {
    loadedMonths.delete(key)
    console.error('[diary] 加载整月失败:', err)
  }
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
    appendTurn,
    loadDay,
    loadMonth,
  }
}
