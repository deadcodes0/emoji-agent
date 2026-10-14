// src/types/chat.ts
// 心理健康对话 Agent 的 SSE 事件载荷类型（与后端 dto 对应）

import type { RiskLevel } from '@/types/diary'

/** event:sentence 载荷 */
export interface SentencePayload {
  i: number
  reply: string
  aiEmotion: string
  aiEmotionIntensity: number
  emoji: string
}

/** event:done 载荷 */
export interface DonePayload {
  userEmotion: string
  userEmotionLabel: string
  userEmotionIntensity: number
  userEmotionEmoji: string
  riskLevel: RiskLevel
  suggestions: string[]
  safetyDisclaimer: string
  requestId: string
}

/** event:error 载荷 */
export interface ErrorPayload {
  code: string
  message: string
}

/** 历史对话条目（发给后端） */
export interface ChatHistoryItem {
  role: 'user' | 'assistant'
  content: string
}
