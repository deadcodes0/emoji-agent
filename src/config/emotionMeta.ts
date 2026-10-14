// src/config/emotionMeta.ts
// 情绪 / 风险 / 主题元数据（纯数据，无副作用）
import { THEME_ORDER, THEMES, type Theme } from '@/config/themes'
import type { EmotionMeta, RiskLevel, RiskMeta } from '@/types/diary'

/** 兜底情绪：未知情绪统一落到这里 */
export const FALLBACK_EMOTION: EmotionMeta = { emoji: '❓', color: '#A0A0A0' }

export type EmotionKey =
  | 'Joy'             // 喜悦
  | 'Calm'            // 平静
  | 'Gratitude'       // 感激
  | 'Anticipation'    // 期待
  | 'Surprise'        // 惊讶
  | 'Fear'            // 恐惧
  | 'Anger'           // 愤怒
  | 'Disgust'         // 厌恶
  | 'Sadness'         // 悲伤
  | 'Despair'         // 绝望
  | 'Loneliness'      // 孤独
  | 'Shame'           // 羞耻
  | 'Fatigue'         // 疲惫
  | 'Numbness'        // 麻木
  | 'Confusion'       // 困惑
  | 'Jealousy'        // 嫉妒
  | 'Anxiety'         // 焦虑
  | 'Guilt'           // 内疚
  | 'Disappointment'  // 失望
  | 'Envy'            // 羡慕
  | 'Hope'            // 希望
  | 'Contentment'     // 满足，含欣慰、满意
  | 'Love'            // 爱意，含亲密、温暖、爱
  | 'Security'        // 安心，含信任、安全、安全感
  | 'Boredom'         // 无聊
  | 'Emptiness'       // 空虚
  | 'Helplessness'    // 无助
  | 'Rejection'       // 被拒绝，含被抛弃、被排斥
  | 'Grievance'       // 委屈
  | 'Contempt'        // 轻蔑，含鄙视
  | 'Hostility'       // 敌意，含怨恨
  | 'Embarrassment'   // 尴尬
  | 'Regret'          // 后悔
  | 'Pride'           // 自豪
  | 'Moved'           // 感动
  | 'Sympathy'        // 同情，含共情
  | 'Curiosity'       // 好奇，含兴趣
  | 'Awe'             // 敬畏，含惊叹
  | 'Stress'          // 压力，含紧张
  | 'Irritability'    // 烦躁，含易怒
  | 'Frustration'     // 挫败
  | 'LowMood'         // 低落，含抑郁情绪
  | 'Relief'          // 释然
  | 'Belonging'       // 归属，含连接感
  | 'Neutral'         // 中性，含无特别情绪
  | 'Nostalgia'       // 怀旧
  | 'Longing'         // 思念，含想念
  | 'SexualDesire'    // 性欲
  | 'Dissociation'    // 解离感，含不真实感
  | 'Indecision'      // 犹豫
  | 'Unknown'         // 未知

export const EMOTION_META: Record<EmotionKey, EmotionMeta> = {
  // 原始保留
  Joy:            { emoji: '😄', color: '#F6C453' }, // 喜悦
  Calm:           { emoji: '😌', color: '#A8E6CF' }, // 平静
  Gratitude:      { emoji: '🙏', color: '#FFB6B9' }, // 感激
  Anticipation:   { emoji: '👀', color: '#FFC857' }, // 期待
  Surprise:       { emoji: '😲', color: '#FFE066' }, // 惊讶
  Fear:           { emoji: '😨', color: '#6C5CE7' }, // 恐惧
  Anger:          { emoji: '😠', color: '#E74C3C' }, // 愤怒
  Disgust:        { emoji: '🤢', color: '#8B9467' }, // 厌恶
  Sadness:        { emoji: '😢', color: '#5DADE2' }, // 悲伤
  Despair:        { emoji: '💔', color: '#2C3E50' }, // 绝望
  Loneliness:     { emoji: '🧍', color: '#5D6D7E' }, // 孤独
  Shame:          { emoji: '😳', color: '#F1948A' }, // 羞耻
  Fatigue:        { emoji: '😩', color: '#AAB7B8' }, // 疲惫
  Numbness:       { emoji: '🫥', color: '#BDC3C7' }, // 麻木
  Confusion:      { emoji: '😕', color: '#F9E79F' }, // 困惑
  Jealousy:       { emoji: '😒', color: '#58D68D' }, // 嫉妒
  Anxiety:        { emoji: '😰', color: '#F39C12' }, // 焦虑
  Guilt:          { emoji: '😞', color: '#D98880' }, // 内疚
  Disappointment: { emoji: '😔', color: '#85929E' }, // 失望
  Envy:           { emoji: '🤩', color: '#F7DC6F' }, // 羡慕
  Hope:           { emoji: '🌟', color: '#82E0AA' }, // 希望
  Contentment:    { emoji: '😊', color: '#A9DFBF' }, // 满足，含欣慰、满意
  Love:           { emoji: '❤️', color: '#E84393' }, // 爱意，含亲密、温暖、爱
  Security:       { emoji: '🛡️', color: '#73C6B6' }, // 安心，含信任、安全、安全感
  Boredom:        { emoji: '🥱', color: '#D5D8DC' }, // 无聊
  Emptiness:      { emoji: '🕳️', color: '#34495E' }, // 空虚
  Helplessness:   { emoji: '🆘', color: '#A569BD' }, // 无助
  Rejection:      { emoji: '🙅', color: '#EC7063' }, // 被拒绝，含被抛弃、被排斥
  Grievance:      { emoji: '🥺', color: '#F5B041' }, // 委屈
  Contempt:       { emoji: '🙄', color: '#B7950B' }, // 轻蔑，含鄙视
  Hostility:      { emoji: '👿', color: '#C0392B' }, // 敌意，含怨恨
  Embarrassment:  { emoji: '😅', color: '#F8C471' }, // 尴尬
  Regret:         { emoji: '😣', color: '#A04000' }, // 后悔
  Pride:          { emoji: '😎', color: '#F4D03F' }, // 自豪
  Moved:          { emoji: '🥹', color: '#F5B7B1' }, // 感动
  Sympathy:       { emoji: '🫂', color: '#AED6F1' }, // 同情，含共情
  Curiosity:      { emoji: '🧐', color: '#48C9B0' }, // 好奇，含兴趣
  Awe:            { emoji: '🤯', color: '#9B59B6' }, // 敬畏，含惊叹
  Stress:         { emoji: '😫', color: '#E59866' }, // 压力，含紧张
  Irritability:   { emoji: '😤', color: '#F0B27A' }, // 烦躁，含易怒
  Frustration:    { emoji: '😖', color: '#CD6155' }, // 挫败
  LowMood:        { emoji: '🌧️', color: '#7FB3D5' }, // 低落，含抑郁情绪
  Relief:         { emoji: '😮‍💨', color: '#ABEBC6' }, // 释然
  Belonging:      { emoji: '🏠', color: '#F5CBA7' }, // 归属，含连接感
  Neutral:        { emoji: '😐', color: '#D7DBDD' }, // 中性，含无特别情绪
  Nostalgia:      { emoji: '📼', color: '#D2B4DE' }, // 怀旧
  Longing:        { emoji: '💭', color: '#BB8FCE' }, // 思念，含想念
  SexualDesire:   { emoji: '🔥', color: '#FF6B6B' }, // 性欲
  Dissociation:   { emoji: '🌫️', color: '#A6ACAF' }, // 解离感，含不真实感
  Indecision:     { emoji: '🤔', color: '#F7DC6F' }, // 犹豫
  Unknown:        { emoji: '❓', color: '#95A5A6' }, // 未知
}

/** 中文情绪标签 → EmotionKey（与后端 UserEmotion.label 保持一致） */
export const EMOTION_LABEL_KEY: Record<string, EmotionKey> = {
  喜悦: 'Joy',
  平静: 'Calm',
  感激: 'Gratitude',
  期待: 'Anticipation',
  惊讶: 'Surprise',
  恐惧: 'Fear',
  愤怒: 'Anger',
  厌恶: 'Disgust',
  悲伤: 'Sadness',
  绝望: 'Despair',
  孤独: 'Loneliness',
  羞耻: 'Shame',
  疲惫: 'Fatigue',
  麻木: 'Numbness',
  困惑: 'Confusion',
  嫉妒: 'Jealousy',
  焦虑: 'Anxiety',
  内疚: 'Guilt',
  失望: 'Disappointment',
  羡慕: 'Envy',
  希望: 'Hope',
  满足: 'Contentment',
  爱意: 'Love',
  安心: 'Security',
  无聊: 'Boredom',
  空虚: 'Emptiness',
  无助: 'Helplessness',
  被拒绝: 'Rejection',
  委屈: 'Grievance',
  轻蔑: 'Contempt',
  敌意: 'Hostility',
  尴尬: 'Embarrassment',
  后悔: 'Regret',
  自豪: 'Pride',
  感动: 'Moved',
  同情: 'Sympathy',
  好奇: 'Curiosity',
  敬畏: 'Awe',
  压力: 'Stress',
  烦躁: 'Irritability',
  挫败: 'Frustration',
  低落: 'LowMood',
  释然: 'Relief',
  归属: 'Belonging',
  中性: 'Neutral',
  怀旧: 'Nostalgia',
  思念: 'Longing',
  性欲: 'SexualDesire',
  解离感: 'Dissociation',
  犹豫: 'Indecision',
  未知: 'Unknown',
}

/** 解析情绪名：兼容 EmotionKey（Anxiety）与中文标签（焦虑） */
export function resolveEmotionKey(name: string): EmotionKey {
  if (name in EMOTION_META) return name as EmotionKey
  return EMOTION_LABEL_KEY[name] ?? 'Unknown'
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

export const CALENDAR_WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日']

export const MAX_SEGMENTS = 6
