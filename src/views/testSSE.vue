<template>
    <div>
        <button @click="handleSend" :disabled="loading">发送请求</button>
        <button @click="stop">停止</button>
        <div class="output">{{ output }}</div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useSSE } from '@/composables/useSSE'

const output = ref('')
const { loading, start, stop } = useSSE()

async function handleSend() {
    output.value = ''
    await start(
        '/api/mood/stream',
        { message: '什么是存在先于本质？' },
        (chunk) => {
            // 逐块追加到界面，实现打字机效果
            output.value += chunk
        },
    )
}
</script>