// src/utils/device.ts
// 响应式设备判断（无状态、无副作用）

export function isMobile(): boolean {
  return window.matchMedia('(max-width: 960px)').matches
}