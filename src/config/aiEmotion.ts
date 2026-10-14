// src/config/aiEmotion.ts
// AI 情绪（aiEmotion 枚举名）→ 前端素材 的唯一事实来源。
// 后端 emoji key（如 sympathy_1）用于资源约定，舞台动图在此映射到现有 gif 素材。

export type AIEmotionName =
  | 'EMPATHY'
  | 'COMFORT'
  | 'ENCOURAGE'
  | 'CALM'
  | 'CONCERN'
  | 'SERIOUS'
  | 'GENTLE'
  | 'SUPPORTIVE'
  | 'HOPEFUL'
  | 'NEUTRAL'
  | 'PLAYFULNESS'
  | 'UNKNOWN'

/** AI 情绪 → 舞台 gif 素材名（src/assets/gif/） */
export const AI_EMOTION_GIF: Record<AIEmotionName, string> = {
  EMPATHY: 'hear',
  COMFORT: 'hugging',
  ENCOURAGE: 'happy',
  CALM: 'calmness',
  CONCERN: 'hear',
  SERIOUS: 'talking',
  GENTLE: 'hugging',
  SUPPORTIVE: 'hugging',
  HOPEFUL: 'happy',
  NEUTRAL: 'talking',
  PLAYFULNESS: 'blink',
  UNKNOWN: 'thinking',
}

/** AI 情绪 → 气泡左侧的表情符号 */
export const AI_EMOTION_EMOJI: Record<AIEmotionName, string> = {
  EMPATHY: '🫂',
  COMFORT: '🤗',
  ENCOURAGE: '💪',
  CALM: '😌',
  CONCERN: '🤲',
  SERIOUS: '🙁',
  GENTLE: '💗',
  SUPPORTIVE: '🤝',
  HOPEFUL: '🌟',
  NEUTRAL: '🙂',
  PLAYFULNESS: '😜',
  UNKNOWN: '❓',
}

export function aiEmotionGif(name: string): string {
  return AI_EMOTION_GIF[name as AIEmotionName] ?? 'talking'
}

export function aiEmotionEmoji(name: string): string {
  return AI_EMOTION_EMOJI[name as AIEmotionName] ?? '🙂'
}
