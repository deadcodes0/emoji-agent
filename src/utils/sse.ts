// src/utils/sse.ts
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { clearAuth, getToken } from './auth'

interface SSERequestOptions {
    url: string
    body?: Record<string, unknown>
    method?: string
    headers?: Record<string, string>
    /** 收到消息时的回调 */
    onMessage: (data: string) => void
    /** 收到带 event 类型的消息时触发（新增） */
    onEvent?: (event: string, data: string) => void
    onOpen?: (response: Response) => void
    onClose?: () => void
    onError?: (error: Error) => boolean | void
    signal?: AbortSignal
}

export async function sseRequest(options: SSERequestOptions): Promise<void> {
    const {
        url, body, method = 'POST', headers = {},
        onMessage, onEvent, onOpen, onClose, onError, signal,
    } = options

    const token = getToken()

    await fetchEventSource(url, {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
            ...headers,
        },
        body: body ? JSON.stringify(body) : undefined,
        signal,
        openWhenHidden: true,

        async onopen(response) {
            if (response.status === 401) {
                clearAuth()
                location.href = '/login'
                throw new Error('登录已过期')
            }
            const contentType = response.headers.get('content-type')
            if (response.ok && contentType?.includes('text/event-stream')) {
                onOpen?.(response)
                return
            }
            throw new Error(`Unexpected response: ${response.status}`)
        },

        onmessage(ev) {
            // 透传 event 类型（没有 event 字段时，默认是 'message'）
            if (ev.event) {
                onEvent?.(ev.event, ev.data)
            }
            // 兼容原有：只对 data 非空的内容调用 onMessage
            if (ev.data) {
                onMessage(ev.data)
            }
        },

        onclose() {
            onClose?.()
        },

        onerror(err) {
            // 默认不重试（尤其 401，否则会按固定间隔疯狂重连刷爆后端日志）。
            // 只有 onError 显式返回 true 才允许重连。
            const shouldRetry = onError?.(err as Error)
            if (shouldRetry !== true) {
                throw err
            }
        },
    })
}