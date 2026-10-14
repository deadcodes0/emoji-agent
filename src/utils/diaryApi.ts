// src/utils/diaryApi.ts
import { request } from '@/utils/auth'

export interface SaveTurnPayload {
  date: string      // yyyy-MM-dd
  time: string      // HH:mm
  userMsg: string
  reply: string
  emotion: string
  riskLevel: string
  suggestions: string[]
}

/** 落库一轮对话；失败会抛 Error（调用方自行 catch） */
export async function saveTurn(payload: SaveTurnPayload): Promise<void> {
  await request<void>('/api/diary/turns', {
    method: 'POST',
    body: payload,
  })
}

export interface DiaryTurnVO {
  time: string
  userMsg: string
  reply: string
  emotion: string
  riskLevel: string
  suggestions: string[]
}

export interface DiaryDayVO {
  date: string
  weekday: string
  summary: string | null
  dominantEmotion: string | null
  riskLevel: string | null
  turns: DiaryTurnVO[]
}

/** 按日期读某天日记；后端无数据时返回 null（不抛错） */
export async function getDiaryDay(date: string): Promise<DiaryDayVO | null> {
  return request<DiaryDayVO | null>(`/api/diary/days/${encodeURIComponent(date)}`)
}

/** 批量读 [from, to] 区间内所有有数据的日期（含完整 turns） */
export async function getDiaryDays(
  from: string,
  to: string,
): Promise<DiaryDayVO[]> {
  const qs = `from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`
  return request<DiaryDayVO[]>(`/api/diary/days?${qs}`)
}