<!-- src/components/ChangePasswordDialog.vue -->
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { updatePassword } from '@/utils/auth'
import { useTheme } from '@/composables/useTheme'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ (e: 'close'): void }>()

const { theme } = useTheme()

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const submitting = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

function reset() {
  oldPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  submitting.value = false
  errorMsg.value = ''
  successMsg.value = ''
}

watch(
  () => props.open,
  (v) => {
    if (v) reset()
  },
)

const isDigits = (s: string) => /^\d{1,9}$/.test(s)

const oldValid = computed(() => isDigits(oldPassword.value))
const newValid = computed(() => isDigits(newPassword.value))
const confirmMismatch = computed(
  () =>
    confirmPassword.value.length > 0 &&
    confirmPassword.value !== newPassword.value,
)
const sameAsOld = computed(
  () =>
    oldValid.value &&
    newValid.value &&
    oldPassword.value === newPassword.value,
)

const canSubmit = computed(() => {
  if (submitting.value) return false
  if (!oldValid.value || !newValid.value) return false
  if (confirmPassword.value !== newPassword.value) return false
  if (sameAsOld.value) return false
  return true
})

function onClose() {
  if (submitting.value) return
  emit('close')
}

async function onSubmit() {
  if (!canSubmit.value) return

  errorMsg.value = ''
  successMsg.value = ''
  submitting.value = true

  try {
    await updatePassword(Number(oldPassword.value), Number(newPassword.value))
    successMsg.value = '密码修改成功'
    setTimeout(() => emit('close'), 800)
  } catch (e) {
    errorMsg.value = e instanceof Error ? e.message : '修改失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="cpd-fade">
      <div
        v-if="open"
        class="cpd-mask"
        :data-theme="theme"
        @click.self="onClose"
      >
        <div class="cpd-card" role="dialog" aria-modal="true">
          <h2 class="cpd-title">修改密码</h2>
          <p class="cpd-subtitle">密码为 1–9 位数字</p>

          <form class="cpd-form" @submit.prevent="onSubmit">
            <label class="cpd-field">
              <span class="cpd-label">原密码</span>
              <input
                v-model="oldPassword"
                class="cpd-input"
                type="password"
                inputmode="numeric"
                maxlength="9"
                placeholder="请输入原密码"
                autocomplete="current-password"
              />
            </label>

            <label class="cpd-field">
              <span class="cpd-label">新密码</span>
              <input
                v-model="newPassword"
                class="cpd-input"
                :class="{ 'cpd-input--error': sameAsOld }"
                type="password"
                inputmode="numeric"
                maxlength="9"
                placeholder="请输入新密码"
                autocomplete="new-password"
              />
              <span v-if="sameAsOld" class="cpd-hint">新密码不能与原密码相同</span>
            </label>

            <label class="cpd-field">
              <span class="cpd-label">确认新密码</span>
              <input
                v-model="confirmPassword"
                class="cpd-input"
                :class="{ 'cpd-input--error': confirmMismatch }"
                type="password"
                inputmode="numeric"
                maxlength="9"
                placeholder="请再次输入新密码"
                autocomplete="new-password"
              />
              <span v-if="confirmMismatch" class="cpd-hint">两次输入的密码不一致</span>
            </label>

            <p v-if="errorMsg" class="cpd-msg cpd-msg--error">{{ errorMsg }}</p>
            <p v-else-if="successMsg" class="cpd-msg cpd-msg--ok">{{ successMsg }}</p>

            <div class="cpd-actions">
              <button
                type="button"
                class="cpd-btn cpd-btn--ghost"
                :disabled="submitting"
                @click="onClose"
              >
                取消
              </button>
              <button
                type="submit"
                class="cpd-btn cpd-btn--primary"
                :disabled="!canSubmit"
              >
                {{ submitting ? '提交中…' : '确认修改' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* =========================================================
 * 遮罩
 * ========================================================= */
.cpd-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.32);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
}

/* =========================================================
 * 卡片（跟随主题）
 * ========================================================= */
.cpd-card {
  --fg: #4a3a30;
  --fg-sub: rgba(74, 58, 48, 0.62);
  --glass-bg: rgba(255, 255, 255, 0.72);
  --glass-border: rgba(255, 255, 255, 0.85);
  --input-bg: rgba(255, 255, 255, 0.7);
  --input-border: rgba(107, 74, 58, 0.16);
  --btn-primary-bg: linear-gradient(135deg, #ffb88c, #ff9a76);
  --btn-primary-fg: #ffffff;
  --btn-ghost-bg: rgba(255, 255, 255, 0.5);
  --btn-ghost-border: rgba(107, 74, 58, 0.14);
  --error: #d9433c;
  --ok: #2e9d5a;

  width: min(380px, 100%);
  padding: 28px 26px 22px;
  border-radius: 22px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  box-shadow: 0 20px 52px rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(18px) saturate(120%);
  -webkit-backdrop-filter: blur(18px) saturate(120%);
  color: var(--fg);
  text-align: center;
}

.cpd-mask[data-theme='dusk'] .cpd-card {
  --fg: #ffffff;
  --fg-sub: rgba(255, 255, 255, 0.72);
  --glass-bg: rgba(40, 14, 52, 0.72);
  --glass-border: rgba(255, 255, 255, 0.24);
  --input-bg: rgba(255, 255, 255, 0.08);
  --input-border: rgba(255, 255, 255, 0.22);
  --btn-primary-bg: linear-gradient(135deg, #e88ba0, #a87ad8);
  --btn-ghost-bg: rgba(255, 255, 255, 0.08);
  --btn-ghost-border: rgba(255, 255, 255, 0.18);
}

.cpd-mask[data-theme='night'] .cpd-card {
  --fg: #f0efff;
  --fg-sub: rgba(200, 200, 255, 0.6);
  --glass-bg: rgba(18, 22, 54, 0.78);
  --glass-border: rgba(200, 200, 255, 0.22);
  --input-bg: rgba(255, 255, 255, 0.05);
  --input-border: rgba(200, 200, 255, 0.18);
  --btn-primary-bg: linear-gradient(135deg, rgba(140, 160, 255, 0.9), rgba(180, 140, 255, 0.9));
  --btn-ghost-bg: rgba(255, 255, 255, 0.06);
  --btn-ghost-border: rgba(200, 200, 255, 0.16);
  box-shadow: 0 20px 52px rgba(0, 0, 30, 0.55), 0 0 40px rgba(120, 140, 255, 0.14);
}

.cpd-mask[data-theme='midnight'] .cpd-card {
  --fg: #c8c8e8;
  --fg-sub: rgba(150, 160, 220, 0.55);
  --glass-bg: rgba(8, 10, 26, 0.82);
  --glass-border: rgba(150, 160, 220, 0.18);
  --input-bg: rgba(255, 255, 255, 0.04);
  --input-border: rgba(150, 160, 220, 0.16);
  --btn-primary-bg: linear-gradient(135deg, rgba(120, 140, 255, 0.8), rgba(150, 120, 220, 0.8));
  --btn-ghost-bg: rgba(255, 255, 255, 0.04);
  --btn-ghost-border: rgba(150, 160, 220, 0.16);
  box-shadow: 0 20px 52px rgba(0, 0, 20, 0.7), 0 0 40px rgba(100, 120, 220, 0.12);
}

/* =========================================================
 * 文案
 * ========================================================= */
.cpd-title {
  margin: 0;
  font-size: 20px;
  letter-spacing: 2px;
  font-weight: 600;
}

.cpd-subtitle {
  margin: 6px 0 20px;
  font-size: 12px;
  letter-spacing: 1px;
  color: var(--fg-sub);
}

/* =========================================================
 * 表单
 * ========================================================= */
.cpd-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  text-align: left;
}

.cpd-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cpd-label {
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--fg-sub);
}

.cpd-input {
  padding: 11px 14px;
  border-radius: 12px;
  border: 1px solid var(--input-border);
  background: var(--input-bg);
  color: var(--fg);
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 0.5px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.cpd-input::placeholder {
  color: var(--fg-sub);
  opacity: 0.7;
}

.cpd-input:focus {
  border-color: #ff9a76;
  box-shadow: 0 0 0 3px rgba(255, 154, 118, 0.18);
}

.cpd-input--error {
  border-color: var(--error);
}

.cpd-input--error:focus {
  border-color: var(--error);
  box-shadow: 0 0 0 3px rgba(217, 67, 60, 0.18);
}

.cpd-hint {
  font-size: 11px;
  letter-spacing: 0.5px;
  color: var(--error);
}

.cpd-msg {
  margin: 0;
  font-size: 12px;
  text-align: center;
  letter-spacing: 0.5px;
}

.cpd-msg--error {
  color: var(--error);
}

.cpd-msg--ok {
  color: var(--ok);
}

/* =========================================================
 * 按钮
 * ========================================================= */
.cpd-actions {
  display: flex;
  gap: 10px;
  margin-top: 6px;
}

.cpd-btn {
  flex: 1 1 0;
  padding: 11px 14px;
  border-radius: 12px;
  border: none;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1.5px;
  cursor: pointer;
  transition: transform 0.15s, filter 0.2s, background 0.2s;
}

.cpd-btn--primary {
  background: var(--btn-primary-bg);
  color: var(--btn-primary-fg);
}

.cpd-btn--primary:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.05);
}

.cpd-btn--ghost {
  background: var(--btn-ghost-bg);
  border: 1px solid var(--btn-ghost-border);
  color: var(--fg);
}

.cpd-btn--ghost:hover:not(:disabled) {
  filter: brightness(1.06);
}

.cpd-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* =========================================================
 * 动画
 * ========================================================= */
.cpd-fade-enter-active,
.cpd-fade-leave-active {
  transition: opacity 0.2s ease;
}

.cpd-fade-enter-from,
.cpd-fade-leave-to {
  opacity: 0;
}
</style>
