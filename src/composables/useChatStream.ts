// src/composables/useChatStream.ts
import { computed, ref } from 'vue'
import { useSSE } from '@/composables/useSSE'
import { authHeaders } from '@/utils/auth'
import type { ChatHistoryItem, DonePayload, SentencePayload } from '@/types/chat'

export interface ChatMessage {
    id: number
    role: 'bot' | 'user'
    text: string
}

/** 一轮完整对话的结果 */
export interface FinishPayload {
    /** 用户这轮说的话（恢复流时可能为空） */
    userMsg: string
    /** bot 回复的完整文本（逐句拼接） */
    reply: string
    /** done 事件的全局状态；异常时为 null */
    meta: DonePayload | null
}

export type FinishHandler = (payload: FinishPayload) => void

/**
 * 心理健康对话 Agent 的前端逻辑：
 * SEND / RESUME / 逐句流式舞台（sentence 事件）/ done 全局状态 / 中断 / 错误兜底。
 */
export function useChatStream() {
    /* ---------------- 消息模型 ---------------- */
    const messages = ref<ChatMessage[]>([])
    let uid = 1

    /* ---------------- 流式展示模式 ---------------- */
    const streamingMode = ref(false)
    /** 逐句流：event:sentence 的载荷序列 */
    const sentences = ref<SentencePayload[]>([])
    /** 全局状态：event:done 的载荷 */
    const meta = ref<DonePayload | null>(null)
    const errorOccurred = ref(false)
    /** 本轮用户输入（用于 finish 时组装 Turn） */
    const lastUserText = ref('')
    let finished = false

    /* ---------------- SSE ---------------- */
    const { loading: isStreaming, start, stop: abortStream } = useSSE()
    const busy = computed(() => streamingMode.value || isStreaming.value)

    /* ---------------- 结束订阅 ---------------- */
    const finishHandlers = new Set<FinishHandler>()

    function onFinish(cb: FinishHandler) {
        finishHandlers.add(cb)
        return () => {
            finishHandlers.delete(cb)
        }
    }

    function emitFinish(payload: FinishPayload) {
        finishHandlers.forEach((cb) => cb(payload))
    }

    /** 统一收尾：文本 push 到消息列表 + 广播结构化结果，并重置本轮状态 */
    function finishWith(fallbackReply?: string) {
        const userMsg = lastUserText.value
        const reply =
            fallbackReply ?? sentences.value.map((s) => s.reply).join('')

        messages.value.push({ id: uid++, role: 'bot', text: reply })
        streamingMode.value = false

        emitFinish({ userMsg, reply, meta: meta.value })

        // 重置本轮状态（finish 订阅者已同步拿到数据）
        sentences.value = []
        meta.value = null
        lastUserText.value = ''
        errorOccurred.value = false
    }

    /** SSE 事件统一处理（SEND 与 RESUME 共用） */
    function makeHandlers(opts: { onEnter?: () => void } = {}) {
        let entered = false

        function enterStreaming() {
            if (entered) return
            entered = true
            streamingMode.value = true
        }

        return {
            onEvent: (event: string, data: string) => {
                if (event === 'open') {
                    enterStreaming()
                    opts.onEnter?.()
                    return
                }
                if (event === 'sentence') {
                    enterStreaming()
                    try {
                        sentences.value.push(JSON.parse(data) as SentencePayload)
                    } catch {
                        /* 单行解析失败，跳过 */
                    }
                    return
                }
                if (event === 'done') {
                    enterStreaming()
                    try {
                        meta.value = JSON.parse(data) as DonePayload
                    } catch {
                        /* done 解析失败保持 null，收尾时走兜底 */
                    }
                    return
                }
                if (event === 'error') {
                    errorOccurred.value = true
                }
            },
            onError: () => {
                if (entered) errorOccurred.value = true
            },
        }
    }

    /** SEND：发起一次 bot 回复 */
    async function askBot(userText: string, history: ChatHistoryItem[] = []) {
        if (busy.value) return

        lastUserText.value = userText
        finished = false
        errorOccurred.value = false
        sentences.value = []
        meta.value = null
        streamingMode.value = true

        const handlers = makeHandlers()
        await start(
            '/api/chat/send',
            { msg: userText, history },
            {
                onEvent: handlers.onEvent,
                onError: (err) => {
                    errorOccurred.value = true
                    handlers.onError()
                    return false
                },
            },
        )
    }

    /**
     * RESUME：进页面 / 重连。
     * 有进行中的流 → 回放 + 跟随；没有 → onClose 立刻触发，什么都不做。
     */
    async function resumeIfAny() {
        let enteredStage = false

        const handlers = makeHandlers({
            onEnter: () => {
                enteredStage = true
                finished = false
            },
        })

        await start(
            '/api/chat/stream',
            undefined,
            {
                onEvent: handlers.onEvent,
                onClose: () => {
                    if (!enteredStage) return
                    // 流已关闭：舞台播完后由 onStageComplete 收尾；
                    // 这里兜底「一句都没收到且没有错误」的空流
                    if (finished) return
                    if (sentences.value.length === 0 && !errorOccurred.value) {
                        finished = true
                        finishWith('（这次没有回应，要不要再说一次？）')
                    }
                },
                onError: (err) => {
                    if (enteredStage) errorOccurred.value = true
                    handlers.onError()
                    return false
                },
            },
        )
    }

    /** 舞台逐句播报完毕 —— 收尾 */
    function onStageComplete() {
        if (finished) return

        if (errorOccurred.value && sentences.value.length === 0) {
            // 全程没收到一句且出错了：给一句占位回复
            finished = true
            finishWith('（我好像断线了…）')
            return
        }

        finished = true
        finishWith(
            sentences.value.length === 0 ? '（这次没有回应，要不要再说一次？）' : undefined,
        )
    }

    /** 用户主动中断当前流式回复 */
    async function stopStreaming() {
        if (finished) return
        finished = true
        finishWith(sentences.value.length === 0 ? '（已停止）' : undefined)

        try {
            await fetch('/api/chat/stop', {
                method: 'POST',
                headers: authHeaders(),
            })
        } catch {
            /* 停止接口失败无所谓，前端已经收尾 */
        }

        abortStream()
    }

    return {
        messages,
        streamingMode,
        sentences,
        meta,
        errorOccurred,
        busy,
        isStreaming,
        askBot,
        resumeIfAny,
        onStageComplete,
        stopStreaming,
        onFinish,
    }
}
