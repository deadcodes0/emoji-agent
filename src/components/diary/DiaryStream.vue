<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import TurnCard from './TurnCard.vue'
import { useDiaryStore } from '@/stores/diary'
import { countEmotions, turnId } from '@/utils/diary'
import { isMobile } from '@/utils/device'

const {
  activeDate,
  activeDay,
  dayTurnCount,
  selectedTurnId,
  highlightedEmotion,
  selectTurn,
  openPanel,
} = useDiaryStore()

const streamEl = ref<HTMLElement | null>(null)

const dayLabel = computed(() => {
  const day = activeDay.value
  if (!day) return ''
  const total = day.turns.length
  const unique = Object.keys(countEmotions(day)).length
  return `${day.date} · ${day.weekday} · ${total} 轮 · ${unique} 种情绪`
})

const items = computed(() => {
  const day = activeDay.value
  if (!day) return []
  return day.turns.map((turn, idx) => {
    const id = turnId(day.date, idx)
    const isMatch =
      !highlightedEmotion.value || turn.emotion === highlightedEmotion.value
    return {
      id,
      idx,
      turn,
      isSelected: id === selectedTurnId.value,
      isDimmed: !isMatch,
      isHighlighted: !!highlightedEmotion.value && isMatch,
    }
  })
})

function handleSelect(id: string) {
  selectTurn(id)
  if (isMobile()) openPanel()
}

async function scrollToBottom() {
  await nextTick()
  const el = streamEl.value
  if (el) el.scrollTop = el.scrollHeight
}

/**
 * ★ 改动：同时 watch 轮次数量。
 * appendTurn 向"当前正在看的那一天"追加时，activeDate 不会变，
 * 只有 dayTurnCount 变化 —— 旧写法不会自动滚到底。
 */
watch([activeDate, dayTurnCount], scrollToBottom)
onMounted(scrollToBottom)
</script>

<template>
  <main ref="streamEl" class="stream">
    <div class="stream__inner">
      <div v-if="!activeDay || dayTurnCount === 0" class="stream__empty">
        这一天还没有记录
      </div>

      <template v-else>
        <div class="stream__day">{{ dayLabel }}</div>

        <TurnCard v-for="item in items" :key="item.id" :turn="item.turn" :index="item.idx" :total="dayTurnCount"
          :selected="item.isSelected" :dimmed="item.isDimmed" :highlighted="item.isHighlighted"
          @select="handleSelect(item.id)" />
      </template>
    </div>
  </main>
</template>

<style scoped>
.stream {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px 22px 40px;
  scrollbar-width: thin;
  scrollbar-color: var(--scroll) transparent;
}

.stream::-webkit-scrollbar {
  width: 6px;
}

.stream::-webkit-scrollbar-thumb {
  background: var(--scroll);
  border-radius: 3px;
}

.stream__inner {
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.stream__day {
  text-align: center;
  font-size: 12px;
  letter-spacing: 1.5px;
  color: var(--text-dim);
  margin: 4px 0 6px;
}

.stream__empty {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-dim);
  font-size: 13px;
  letter-spacing: 1px;
}

@media (max-width: 960px) {
  .stream {
    padding: 16px 14px 80px;
  }
}
</style>