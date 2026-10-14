<script setup lang="ts">
import EmotionTag from './EmotionTag.vue'
import { useDiaryStore } from '@/stores/diary'
import { emotionMeta, riskMeta } from '@/utils/diary'
import type { DonePayload } from '@/types/chat'

const props = defineProps<{
  /** 流式进行中：面板显示实时分析卡片（占位 → done 事件刷新） */
  analyzing?: boolean
  /** event:done 的实时载荷 */
  liveMeta?: DonePayload | null
}>()

const {
  selected,
  panelEmotions,
  dominantEmotion,
  highlightedEmotion,
  panelOpen,
  toggleEmotion,
  closePanel,
} = useDiaryStore()
</script>

<template>
  <aside class="panel" :class="{ 'is-open': panelOpen }">
    <div class="panel__inner">
      <!-- ============ 流式实时分析卡片 ============ -->
      <template v-if="props.analyzing">
        <div class="panel__head">
          <span class="panel__head-label">Live Analysis</span>
          <span class="panel__head-time">对话进行中</span>
          <span class="panel__head-sub">逐句共情回复 · 实时分析</span>
        </div>

        <!-- emotion -->
        <div class="field">
          <div class="field__label">
            <code>emotion</code><span>当前情绪</span>
          </div>
          <div v-if="liveMeta" class="p-emotion"
            :style="{ '--tag-color': emotionMeta(liveMeta.userEmotionLabel).color }">
            <span class="emoji p-emotion__emoji">
              {{ emotionMeta(liveMeta.userEmotionLabel).emoji }}
            </span>
            <span>{{ liveMeta.userEmotionLabel }}</span>
            <span class="p-intensity">{{ liveMeta.userEmotionIntensity }}/10</span>
          </div>
          <div v-else class="p-pending">分析中…</div>
        </div>

        <!-- riskLevel -->
        <div class="field" :style="liveMeta ? {
          '--risk-color': riskMeta(liveMeta.riskLevel).hex,
          '--risk-rgb': riskMeta(liveMeta.riskLevel).rgb,
        } : {}">
          <div class="field__label">
            <code>riskLevel</code><span>风险等级</span>
          </div>
          <div v-if="liveMeta" class="p-risk">
            <span class="p-risk__dot" />
            <span>{{ riskMeta(liveMeta.riskLevel).label }}</span>
            <span class="p-risk__code">{{ liveMeta.riskLevel }}</span>
          </div>
          <div v-else class="p-pending">评估中…</div>
        </div>

        <!-- suggestions -->
        <div class="field">
          <div class="field__label">
            <code>suggestions</code><span>建议</span>
          </div>
          <ul v-if="liveMeta && liveMeta.suggestions.length" class="p-sugg">
            <li v-for="(s, i) in liveMeta.suggestions" :key="i">
              <span class="p-sugg__idx">{{ i + 1 }}</span>
              <span>{{ s }}</span>
            </li>
          </ul>
          <div v-else class="p-pending">生成中…</div>
        </div>

        <p v-if="liveMeta" class="p-disclaimer">{{ liveMeta.safetyDisclaimer }}</p>
      </template>

      <!-- ============ 已选中轮次详情 ============ -->
      <template v-else-if="selected">
        <div class="panel__head">
          <span class="panel__head-label">
            Turn {{ selected.index + 1 }} / {{ selected.day.turns.length }}
          </span>
          <span class="panel__head-time">
            {{ selected.date }} {{ selected.turn.time }}
          </span>
          <span class="panel__head-sub">
            {{ selected.weekday }} · 当天第 {{ selected.index + 1 }} 条
          </span>
        </div>

        <!-- emotion -->
        <div class="field">
          <div class="field__label">
            <code>emotion</code><span>当前情绪</span>
          </div>
          <div class="p-emotion" :style="{ '--tag-color': emotionMeta(selected.turn.emotion).color }">
            <span class="emoji p-emotion__emoji">
              {{ emotionMeta(selected.turn.emotion).emoji }}
            </span>
            <span>{{ selected.turn.emotion }}</span>
          </div>
        </div>

        <!-- riskLevel -->
        <div class="field" :style="{
          '--risk-color': riskMeta(selected.turn.riskLevel).hex,
          '--risk-rgb': riskMeta(selected.turn.riskLevel).rgb,
        }">
          <div class="field__label">
            <code>riskLevel</code><span>风险等级</span>
          </div>
          <div class="p-risk">
            <span class="p-risk__dot" />
            <span>{{ riskMeta(selected.turn.riskLevel).label }}</span>
            <span class="p-risk__code">{{ selected.turn.riskLevel }}</span>
          </div>
        </div>

        <!-- suggestions -->
        <div class="field">
          <div class="field__label">
            <code>suggestions</code><span>建议</span>
          </div>
          <ul class="p-sugg">
            <li v-for="(s, i) in selected.turn.suggestions" :key="i">
              <span class="p-sugg__idx">{{ i + 1 }}</span>
              <span>{{ s }}</span>
            </li>
          </ul>
        </div>

        <!-- 当天情绪 -->
        <div class="p-today">
          <div class="p-today__head">
            <span class="p-today__label">当天情绪</span>
            <span class="p-today__count">
              {{ panelEmotions.length }} 种 / {{ selected.day.turns.length }} 轮
            </span>
          </div>

          <div class="p-today__tags" :class="{ 'has-active': !!highlightedEmotion }">
            <EmotionTag v-for="stat in panelEmotions" :key="stat.emotion" :emotion="stat.emotion" :count="stat.count"
              :active="highlightedEmotion === stat.emotion" @click="toggleEmotion(stat.emotion)" />
          </div>

          <div class="p-today__hint">
            {{
              highlightedEmotion
                ? `已筛选「${highlightedEmotion}」，再点一次取消`
                : '点击标签筛选对应轮次'
            }}
          </div>

          <div class="p-dominant">
            <span class="emoji p-dominant__emoji">
              {{ emotionMeta(dominantEmotion).emoji }}
            </span>
            <div class="p-dominant__text">
              <span class="p-dominant__label">主导情绪</span>
              <span class="p-dominant__value" :style="{ '--tag-color': emotionMeta(dominantEmotion).color }">
                {{ dominantEmotion }}
              </span>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="panel__empty">选择左侧任一记录</div>

      <!-- 常驻安全提示 -->
      <p class="panel-foot">AI 回复不能替代专业心理帮助 · 危机请拨 12356 / 110 / 120</p>
    </div>
  </aside>
</template>

<style scoped>
.panel {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 24px 22px;
  border-left: 1px solid var(--panel-border);
  background: var(--panel-bg);
  backdrop-filter: blur(16px) saturate(120%);
  -webkit-backdrop-filter: blur(16px) saturate(120%);
  transition: background 0.6s ease, border-color 0.6s ease;
  scrollbar-width: thin;
  scrollbar-color: var(--scroll) transparent;
}

.panel::-webkit-scrollbar {
  width: 5px;
}

.panel::-webkit-scrollbar-thumb {
  background: var(--scroll);
  border-radius: 3px;
}

.panel__inner {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel__head {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--panel-border);
}

.panel__head-label {
  font-size: 10px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-sub);
  text-shadow: var(--panel-text-shadow, none);
}

.panel__head-time {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.8px;
  color: var(--text);
  text-shadow: var(--text-shadow, none);
}

.panel__head-sub {
  font-size: 11px;
  color: var(--text-dim);
  text-shadow: var(--panel-text-shadow, none);
}

.panel__empty {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-dim);
  font-size: 12px;
  letter-spacing: 1px;
  text-shadow: var(--panel-text-shadow, none);
}

.field {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.field__label {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 10px;
  letter-spacing: 0.1em;
  color: var(--text-sub);
  text-transform: uppercase;
  text-shadow: var(--panel-text-shadow, none);
}

.field__label code {
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  font-weight: 600;
  font-size: 10px;
  opacity: 0.9;
}

.field__label span {
  letter-spacing: 0.18em;
  opacity: 0.75;
}

/* ---------------- 当前情绪气泡 ----------------
 * 底色 = 情绪色 × --tag-tint + 主题基底 --tag-base
 * 描边 = 情绪色 × --tag-border-strength
 * 文字阴影 = --tag-text-shadow
 * 全部由主题在 EmotionDiaryView 上定义
 * ------------------------------------------------- */
.p-emotion {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px 7px 10px;
  border-radius: 999px;
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.4px;
  color: var(--tag-color);
  background: color-mix(in srgb,
      var(--tag-color) var(--tag-tint, 16%),
      var(--tag-base, rgba(255, 255, 255, 0.55)));
  border: 1px solid color-mix(in srgb,
      var(--tag-color) var(--tag-border-strength, 55%),
      transparent);
  text-shadow: var(--tag-text-shadow, none);
  transition: background 0.6s ease, border-color 0.6s ease, color 0.6s ease;
}

.p-emotion__emoji {
  font-size: 18px;
}

/* ---------------- 流式占位 / 强度 / 免责 ---------------- */
.p-pending {
  align-self: flex-start;
  font-size: 12.5px;
  letter-spacing: 0.5px;
  color: var(--text-dim);
  text-shadow: var(--panel-text-shadow, none);
  animation: pendingPulse 1.6s ease-in-out infinite;
}

@keyframes pendingPulse {

  0%,
  100% {
    opacity: 0.55;
  }

  50% {
    opacity: 1;
  }
}

.p-intensity {
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  font-size: 10px;
  letter-spacing: 0.08em;
  opacity: 0.7;
}

.p-disclaimer {
  margin: 4px 0 0;
  font-size: 10.5px;
  line-height: 1.6;
  letter-spacing: 0.3px;
  color: var(--text-dim);
  text-shadow: var(--panel-text-shadow, none);
  padding: 8px 10px;
  border-radius: 10px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
}

.panel-foot {
  margin: 4px 0 0;
  padding-top: 12px;
  border-top: 1px solid var(--panel-border);
  font-size: 10px;
  letter-spacing: 0.4px;
  line-height: 1.6;
  color: var(--text-dim);
  text-shadow: var(--panel-text-shadow, none);
  text-align: center;
}

.p-risk {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
  color: var(--risk-color);
  background: rgba(var(--risk-rgb), var(--risk-bg-alpha));
  border: 1px solid rgba(var(--risk-rgb), var(--risk-border-alpha));
  text-shadow: var(--risk-text-glow);
}

.p-risk__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--risk-color);
  box-shadow: 0 0 0 3px rgba(var(--risk-rgb), 0.22);
  animation: riskPulse 2.2s ease-in-out infinite;
}

@keyframes riskPulse {

  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 3px rgba(var(--risk-rgb), 0.22);
  }

  50% {
    transform: scale(1.2);
    opacity: 0.8;
    box-shadow: 0 0 0 6px rgba(var(--risk-rgb), 0.08);
  }
}

.p-risk__code {
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  font-size: 9px;
  letter-spacing: 0.12em;
  opacity: 0.7;
}

.p-sugg {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.p-sugg li {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  font-size: 12.5px;
  line-height: 1.55;
  color: var(--text);
  text-shadow: var(--panel-text-shadow, none);
}

.p-sugg__idx {
  flex: 0 0 auto;
  width: 18px;
  height: 18px;
  margin-top: 1px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 10px;
  font-weight: 700;
  color: var(--text);
  background: var(--idx-bg);
  text-shadow: none;
}

.p-today {
  margin-top: 8px;
  padding-top: 14px;
  border-top: 1px solid var(--panel-border);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.p-today__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.p-today__label {
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-sub);
  text-shadow: var(--panel-text-shadow, none);
}

.p-today__count {
  font-size: 10px;
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  color: var(--text-dim);
  text-shadow: var(--panel-text-shadow, none);
}

.p-today__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.p-today__tags.has-active :deep(.p-emotion-tag:not(.is-active)) {
  opacity: 0.4;
}

.p-today__hint {
  font-size: 10px;
  letter-spacing: 0.3px;
  color: var(--text-dim);
  opacity: 0.95;
  text-shadow: var(--panel-text-shadow, none);
}

.p-dominant {
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  display: flex;
  align-items: center;
  gap: 10px;
  transition: background 0.6s ease, border-color 0.6s ease;
}

.p-dominant__emoji {
  font-size: 20px;
}

.p-dominant__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.p-dominant__label {
  font-size: 9px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-sub);
  text-shadow: var(--panel-text-shadow, none);
}

.p-dominant__value {
  font-size: 13px;
  font-weight: 700;
  color: var(--tag-color);
  text-shadow: var(--tag-text-shadow, none);
  transition: color 0.6s ease;
}

@media (max-width: 960px) {
  .panel {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    top: auto;
    max-height: 64vh;
    border-left: none;
    border-top: 1px solid var(--panel-border);
    border-radius: 22px 22px 0 0;
    transform: translateY(100%);
    transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1), background 0.6s ease,
      border-color 0.6s ease;
    z-index: 30;
    box-shadow: 0 -20px 60px -20px rgba(0, 0, 0, 0.4);
    padding: 18px 20px 28px;
  }

  .panel.is-open {
    transform: translateY(0);
  }

  .panel::before {
    content: '';
    display: block;
    width: 36px;
    height: 4px;
    margin: -4px auto 12px;
    border-radius: 2px;
    background: var(--text-dim);
  }
}
</style>