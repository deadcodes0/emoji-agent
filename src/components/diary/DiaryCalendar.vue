<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
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

/* ------------------------------------------------------------------
 * 可查日期范围
 *   - 下限：用户注册时间（占位值，后续换成真实数据）
 *   - 上限：今天
 * ------------------------------------------------------------------ */
const REGISTERED_AT = '2026-01-01' // TODO: 替换为用户真实注册时间

const MIN_YEAR = 1970
const MAX_YEAR = new Date().getFullYear()

const registeredDate = (() => {
  const d = new Date(REGISTERED_AT)
  d.setHours(0, 0, 0, 0)
  return d
})()

const todayDate = (() => {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  return d
})()

/** 把「年 + 月」压成一个可比较的整数 */
const toYm = (y: number, m: number) => y * 12 + (m - 1)
const registeredYM = toYm(registeredDate.getFullYear(), registeredDate.getMonth() + 1)
const todayYM = toYm(todayDate.getFullYear(), todayDate.getMonth() + 1)
const currentYM = computed(() => toYm(currentYear.value, currentMonth.value))

/** 早于注册时间 → 不能再往前；晚于今天 → 不能再往后 */
const canPrevMonth = computed(() => currentYM.value > registeredYM)
const canNextMonth = computed(() => currentYM.value < todayYM)

/* ------------------------------------------------------------------
 * 手动输入的年 / 月 / 日
 * ------------------------------------------------------------------ */
const inputYear = ref<number | ''>(currentYear.value)
const inputMonth = ref<number | ''>(currentMonth.value)
const inputDay = ref<number | ''>(1)

/** 从 store 当前状态回填输入框（用于初始化、外部变化、以及非法输入回退） */
function syncFromStore() {
  if (activeDate.value) {
    const [y, m, d] = String(activeDate.value).split('-').map(Number)
    inputYear.value = Number.isFinite(y) && y ? y : currentYear.value
    inputMonth.value = Number.isFinite(m) && m ? m : currentMonth.value
    inputDay.value = Number.isFinite(d) && d ? d : 1
    return
  }
  inputYear.value = currentYear.value
  inputMonth.value = currentMonth.value
  inputDay.value = 1
}

/** activeDate / currentYear / currentMonth 变化时同步输入框 */
watch([currentYear, currentMonth, activeDate], syncFromStore, { immediate: true })

/** 校验并把输入框的值落到 store，同时切换日历显示的年月。
 *  任一字段非法（越界 / 空 / 非整数 / 日期不存在 / 超出可查范围）则整组回退。 */
function applyInput() {
  if (calendarCollapsed.value) return

  const y = Number(inputYear.value)
  const m = Number(inputMonth.value)
  const d = Number(inputDay.value)

  const basicInvalid =
    !Number.isInteger(y) || y < MIN_YEAR || y > MAX_YEAR ||
    !Number.isInteger(m) || m < 1 || m > 12 ||
    !Number.isInteger(d) || d < 1 ||
    d > new Date(y, m, 0).getDate()

  if (basicInvalid) {
    syncFromStore()
    return
  }

  const dateObj = new Date(y, m - 1, d)
  if (dateObj < registeredDate || dateObj > todayDate) {
    syncFromStore()
    return
  }

  // 1) 先把日历切到输入的年月，网格会重新生成
  currentYear.value = y
  currentMonth.value = m

  // 2) 再选中具体日期
  const key = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  selectDate(key, dateObj, true)
}

/* ------------------------------------------------------------------
 * 输入防抖：停手 500ms 后自动应用，无需回车 / 失焦
 * ------------------------------------------------------------------ */
let debounceTimer: number | undefined

function clearDebounce() {
  if (debounceTimer !== undefined) {
    window.clearTimeout(debounceTimer)
    debounceTimer = undefined
  }
}

function scheduleApply() {
  clearDebounce()
  debounceTimer = window.setTimeout(() => {
    debounceTimer = undefined
    applyInput()
  }, 500)
}

function applyNow() {
  clearDebounce()
  applyInput()
}

onUnmounted(clearDebounce)

function onCellClick(cell: CalCell) {
  selectDate(cell.dateKey, cell.dateObj, cell.inMonth)
}

/* ------------------------------------------------------------------
 * 月份切换：受可查范围限制
 * ------------------------------------------------------------------ */
function onPrevMonth() {
  if (canPrevMonth.value) prevMonth()
}
function onNextMonth() {
  if (canNextMonth.value) nextMonth()
}
</script>

<template>
  <section class="calendar" :class="{ 'is-collapsed': calendarCollapsed }">
    <!-- Header 触发器：年月日输入 + 折叠按钮，紧挨在一起 -->
    <div class="calendar__head">
      <div class="month-nav">
        <input v-model.number="inputYear" class="date-input date-input--y" type="number" :min="MIN_YEAR" :max="MAX_YEAR"
          inputmode="numeric" aria-label="年" :disabled="calendarCollapsed" @input="scheduleApply" @change="applyNow"
          @keyup.enter="applyNow" />
        <span class="date-sep">年</span>
        <input v-model.number="inputMonth" class="date-input date-input--md" type="number" min="1" max="12"
          inputmode="numeric" aria-label="月" :disabled="calendarCollapsed" @input="scheduleApply" @change="applyNow"
          @keyup.enter="applyNow" />
        <span class="date-sep">月</span>
        <input v-model.number="inputDay" class="date-input date-input--md" type="number" min="1" max="31"
          inputmode="numeric" aria-label="日" :disabled="calendarCollapsed" @input="scheduleApply" @change="applyNow"
          @keyup.enter="applyNow" />
        <span class="date-sep">日</span>
      </div>

      <button class="calendar__toggle" type="button" aria-label="收起或展开日历" @click="toggleCalendar">
        <span>{{ calendarCollapsed ? '展开日历' : '收起日历' }}</span>
        <span class="calendar__toggle-arrow">▾</span>
      </button>
    </div>

    <!-- 独立浮层：日期网格 + 底部月份切换 -->
    <div class="calendar__body">
      <div class="calendar__panel">
        <div class="calendar__weekdays">
          <span v-for="w in CALENDAR_WEEKDAYS" :key="w">{{ w }}</span>
        </div>

        <div class="calendar__grid">
          <button v-for="cell in calendarCells" :key="cell.dateKey" type="button" class="cal-cell" :class="{
            'is-outside': !cell.inMonth,
            'has-data': cell.hasData,
            'is-selected': cell.dateKey === activeDate,
          }" :style="cell.color ? { '--cell-color': cell.color } : undefined" @click="onCellClick(cell)">
            <span class="cal-cell__day">{{ cell.day }}</span>
            <span class="cal-cell__bar">
              <span v-for="(seg, i) in cell.segments" :key="i" class="cal-cell__seg" :style="{ background: seg }" />
            </span>
          </button>
        </div>

        <!-- 底部：月份切换（超出可查范围时对应按钮禁用） -->
        <div class="calendar__foot">
          <button class="month-btn" type="button" aria-label="上个月" :disabled="!canPrevMonth" @click="onPrevMonth">
            ‹
          </button>
          <span class="calendar__foot-label">
            {{ currentYear }} 年 {{ currentMonth }} 月
          </span>
          <button class="month-btn" type="button" aria-label="下个月" :disabled="!canNextMonth" @click="onNextMonth">
            ›
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* ============================================================
 * 触发器容器：本身不改变高度，仅作为浮层的定位父级
 * ============================================================ */
.calendar {
  position: relative;
  flex: 0 1 420px;
  max-width: 420px;
  min-width: 0;
}

.calendar__head {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  /* 与 .diary-layout__header-left 的 padding-top 一致，两边顶部对齐 */
  padding-top: 4px;
}

/* -------------------- 手动输入年月日 -------------------- */
.month-nav {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--text);
  transition: color 0.6s ease;
}

.date-input {
  padding: 2px 0;
  border: none;
  border-bottom: 1px dashed color-mix(in srgb, var(--text) 32%, transparent);
  background: transparent;
  color: inherit;
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-align: center;
  outline: none;
  transition: border-color 0.2s ease, color 0.6s ease;
}

.date-input--y {
  width: 42px;
}

.date-input--md {
  width: 28px;
}

.date-input:focus {
  border-bottom-color: var(--text);
  border-bottom-style: solid;
}

.date-input:hover:not(:disabled) {
  border-bottom-color: color-mix(in srgb, var(--text) 60%, transparent);
}

.date-input:disabled {
  cursor: default;
  opacity: 1;
  -webkit-text-fill-color: var(--text);
  border-bottom-color: transparent;
}

.date-input::-webkit-outer-spin-button,
.date-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.date-input {
  -moz-appearance: textfield;
  appearance: textfield;
}

.date-sep {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--text);
  opacity: 0.75;
  padding: 0 1px;
  user-select: none;
}

/* -------------------- 折叠按钮 -------------------- */
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

/* ============================================================
 * 独立浮层：绝对定位在 header 下方，不占据文档流高度
 * ============================================================ */
.calendar__body {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 340px;
  z-index: 30;
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
  transition: opacity 0.28s ease,
    transform 0.28s cubic-bezier(0.22, 1, 0.36, 1),
    visibility 0.28s ease;
}

.calendar.is-collapsed .calendar__body {
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  pointer-events: none;
}

.calendar__panel {
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--panel-bg);
  border: 1px solid var(--panel-border);
  backdrop-filter: blur(18px) saturate(120%);
  -webkit-backdrop-filter: blur(18px) saturate(120%);
  box-shadow: var(--panel-shadow, 0 16px 40px rgba(0, 0, 0, 0.14));
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
  background: var(--card-active);
  border-color: color-mix(in srgb, var(--text) 38%, transparent);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--text) 14%, transparent);
  opacity: 1;
}

.cal-cell.is-selected:hover {
  background: var(--card-active);
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

/* -------------------- 底部月份切换 -------------------- */
.calendar__foot {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid color-mix(in srgb, var(--text) 12%, transparent);
}

.month-btn {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--text-sub);
  font-family: inherit;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, transform 0.15s ease,
    opacity 0.2s ease;
}

.month-btn:hover {
  background: var(--card-hover);
  color: var(--text);
}

.month-btn:active {
  transform: scale(0.9);
}

/* 禁用态放在 :hover 之后，确保覆盖 hover 高亮 */
.month-btn:disabled,
.month-btn:disabled:hover,
.month-btn:disabled:active {
  opacity: 0.28;
  cursor: not-allowed;
  background: transparent;
  color: var(--text-sub);
  transform: none;
}

.calendar__foot-label {
  min-width: 92px;
  text-align: center;
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.5px;
  color: var(--text);
  transition: color 0.6s ease;
}

@media (max-width: 960px) {
  .calendar {
    width: 100%;
    max-width: none;
  }

  .calendar__body {
    left: 0;
    right: 0;
    width: auto;
  }

  .date-input,
  .date-sep {
    font-size: 12px;
  }

  .date-input--y {
    width: 38px;
  }

  .date-input--md {
    width: 26px;
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