// src/utils/diary.ts
// 纯函数工具：日期、情绪统计、日历单元格构建（无副作用、无状态）
import { DIARY } from '@/config/diaryData'
import {
  EMOTION_META,
  FALLBACK_EMOTION,
  RISK_META,
  RISK_SCORE,
  resolveEmotionKey,
} from '@/config/emotionMeta'
import type {
  CalCell,
  DiaryDay,
  EmotionMeta,
  EmotionStat,
  RiskLevel,
  RiskMeta,
} from '@/types/diary'

export const pad = (n: number) => String(n).padStart(2, '0')

export const formatDate = (d: Date) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`

export const turnId = (date: string, idx: number) => `${date}__${idx}`

export const findDay = (date: string): DiaryDay | undefined =>
  DIARY.find((d) => d.date === date)

/** 情绪元数据查找：兼容 EmotionKey 与中文标签，未命中走兜底 */
export const emotionMeta = (e: string): EmotionMeta =>
  EMOTION_META[resolveEmotionKey(e)] ?? FALLBACK_EMOTION

export const riskMeta = (r: RiskLevel): RiskMeta =>
  RISK_META[r] ?? RISK_META.LOW

export function countEmotions(day: DiaryDay): Record<string, number> {
  const count: Record<string, number> = {}
  day.turns.forEach((t) => {
    count[t.emotion] = (count[t.emotion] || 0) + 1
  })
  return count
}

export function listAllEmotions(day: DiaryDay): EmotionStat[] {
  const count = countEmotions(day)
  const list: EmotionStat[] = Object.entries(count).map(([emotion, n]) => {
    const maxRisk = day.turns
      .filter((t) => t.emotion === emotion)
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
  return list.length ? list[0]!.emotion : '未知'
}

/**
 * 日历色点选色：
 * - 取当天出现次数最多的情绪；
 * - 若并列最多，取「在 turns 中索引更大」的那个（turns 按时间正序，索引越大越晚）；
 * - 无数据返回 ''。
 */
export function pickCalendarEmotion(day: DiaryDay): string {
  const turns = day.turns
  if (turns.length === 0) return ''

  const count: Record<string, number> = {}
  for (const t of turns) count[t.emotion] = (count[t.emotion] || 0) + 1

  let maxCount = 0
  for (const n of Object.values(count)) {
    if (n > maxCount) maxCount = n
  }

  let winner = ''
  let winnerIdx = -1
  for (let i = 0; i < turns.length; i++) {
    const e = turns[i]!.emotion
    if (count[e] === maxCount && i > winnerIdx) {
      winner = e
      winnerIdx = i
    }
  }
  return winner
}

function makeCell(day: number, inMonth: boolean, dateObj: Date): CalCell {
  const dateKey = formatDate(dateObj)
  const dayData = findDay(dateKey)
  const hasData = !!dayData
  const color = hasData ? emotionMeta(pickCalendarEmotion(dayData!)).color : ''
  return {
    day,
    inMonth,
    dateObj,
    dateKey,
    hasData,
    color,
    segments: color ? [color] : [],
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
