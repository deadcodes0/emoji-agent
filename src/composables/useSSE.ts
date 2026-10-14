// src/composables/useSSE.ts
import { ref, onUnmounted } from 'vue'
import { sseRequest } from '@/utils/sse'

interface StartHooks {
  onChunk?: (data: string) => void
  onFirstChunk?: (data: string) => void
  onEvent?: (event: string, data: string) => void
  onClose?: () => void
  onError?: (error: Error) => boolean | void
}

export function useSSE() {
  const loading = ref(false)
  const error = ref<string | null>(null)
  let controller: AbortController | null = null

  async function start(
    url: string,
    body: Record<string, unknown> | undefined,
    hooks: StartHooks | ((data: string) => void),
  ) {
    stop()

    const { onChunk, onFirstChunk, onEvent, onClose, onError } =
      typeof hooks === 'function'
        ? { onChunk: hooks, onFirstChunk: undefined, onEvent: undefined, onClose: undefined, onError: undefined }
        : hooks

    loading.value = true
    error.value = null
    controller = new AbortController()

    let hasReceivedFirst = false

    try {
      await sseRequest({
        url,
        body,
        method: body === undefined ? 'GET' : 'POST',
        signal: controller.signal,
        onEvent: (event, data) => {
          onEvent?.(event, data)
        },
        onMessage: (data) => {
          if (!hasReceivedFirst) {
            hasReceivedFirst = true
            onFirstChunk?.(data)
          }
          onChunk?.(data)
        },
        onClose: () => {
          loading.value = false
          onClose?.()
        },
        onError: (err) => {
          const e = err as Error
          error.value = e.message
          loading.value = false
          // 与 sse.ts 语义对齐：显式返回 true 才重连
          return onError?.(e) === true
        },
      })
    } catch {
      loading.value = false
    }
  }

  function stop() {
    if (controller) {
      controller.abort()
      controller = null
    }
    loading.value = false
  }

  onUnmounted(() => stop())

  return { loading, error, start, stop }
}