// src/config/diaryData.ts
// 数据全部来自后端：/api/diary/days 与 /api/diary/days/{date}
// 这里只保留一个响应式容器，store 和 utils 仍共用同一份引用。
import { reactive } from 'vue'
import type { DiaryDay } from '@/types/diary'

export const DIARY = reactive<DiaryDay[]>([])
