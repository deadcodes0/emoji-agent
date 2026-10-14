// src/composables/useSidebar.ts
import { ref, watch } from 'vue'

// 模块级单例，跨组件共享
const isOpen = ref(false)

// 打开时锁定 body 滚动
watch(isOpen, (v) => {
  if (typeof document === 'undefined') return
  document.body.style.overflow = v ? 'hidden' : ''
})

export function useSidebar() {
  return {
    isOpen,
    open: () => { isOpen.value = true },
    close: () => { isOpen.value = false },
    toggle: () => { isOpen.value = !isOpen.value },
  }
}