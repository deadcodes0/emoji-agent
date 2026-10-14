// src/types/diary.ts
// 情绪日记相关的全部 TS 类型定义

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

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
