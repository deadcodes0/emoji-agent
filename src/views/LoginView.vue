<!-- src/views/LoginView.vue -->
<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { login, register } from '@/utils/auth'

const router = useRouter()
const { theme } = useTheme()

/* ---------------- 模式切换 ---------------- */
const isRegister = ref(false)
const modeText = computed(() => (isRegister.value ? '注册' : '登录'))
const toggleText = computed(() =>
  isRegister.value ? '已有账号？去登录' : '还没有账号？去注册',
)

/* ---------------- 表单 ---------------- */
const account = ref('')
const password = ref('')
const confirmPassword = ref('')
const nickname = ref('')
const submitting = ref(false)
const errorMsg = ref('')

const accountValid = computed(() => /^\d{1,9}$/.test(account.value))
const passwordValid = computed(() => /^\d{1,9}$/.test(password.value))

/** 两次密码是否不一致（仅注册、且确认框已有内容时提示） */
const confirmMismatch = computed(
  () =>
    isRegister.value &&
    confirmPassword.value.length > 0 &&
    confirmPassword.value !== password.value,
)

const canSubmit = computed(() => {
  if (submitting.value) return false
  if (!accountValid.value || !passwordValid.value) return false
  if (isRegister.value) {
    return confirmPassword.value.length > 0 && confirmPassword.value === password.value
  }
  return true
})

function onToggle() {
  isRegister.value = !isRegister.value
  confirmPassword.value = ''
  errorMsg.value = ''
}

async function onSubmit() {
  if (submitting.value) return

  if (isRegister.value && confirmPassword.value !== password.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  if (!canSubmit.value) return

  submitting.value = true
  errorMsg.value = ''

  try {
    if (isRegister.value) {
      await register(Number(account.value), Number(password.value), nickname.value.trim())
    } else {
      await login(Number(account.value), Number(password.value))
    }
    router.replace({ name: 'home' })
  } catch (e) {
    errorMsg.value = e instanceof Error ? e.message : '请求失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-page" :data-theme="theme">
    <div class="card">
      <img class="card__logo" src="/flower.png" alt="" />
      <h1 class="card__title">{{ modeText }}</h1>
      <p class="card__subtitle">账号和密码都是数字哦</p>

      <form class="form" @submit.prevent="onSubmit">
        <label class="field">
          <span class="field__label">账号</span>
          <input
            v-model="account"
            class="field__input"
            type="text"
            inputmode="numeric"
            maxlength="9"
            placeholder="请输入数字账号"
            autocomplete="username"
          />
        </label>

        <label class="field">
          <span class="field__label">密码</span>
          <input
            v-model="password"
            class="field__input"
            type="password"
            inputmode="numeric"
            maxlength="9"
            placeholder="请输入数字密码"
            :autocomplete="isRegister ? 'new-password' : 'current-password'"
          />
        </label>

        <label v-if="isRegister" class="field">
          <span class="field__label">确认密码</span>
          <input
            v-model="confirmPassword"
            class="field__input"
            :class="{ 'field__input--error': confirmMismatch }"
            type="password"
            inputmode="numeric"
            maxlength="9"
            placeholder="请再次输入数字密码"
            autocomplete="new-password"
          />
          <span v-if="confirmMismatch" class="field__hint">两次输入的密码不一致</span>
        </label>

        <label v-if="isRegister" class="field">
          <span class="field__label">昵称（可选）</span>
          <input
            v-model="nickname"
            class="field__input"
            type="text"
            maxlength="32"
            placeholder="给自己起个名字"
          />
        </label>

        <p v-if="errorMsg" class="form__error">{{ errorMsg }}</p>

        <button class="form__submit" type="submit" :disabled="!canSubmit">
          {{ submitting ? '请稍候…' : modeText }}
        </button>
      </form>

      <button class="card__toggle" type="button" @click="onToggle">{{ toggleText }}</button>
    </div>
  </div>
</template>

<style scoped>
/* ★ 关键改动：顶部留出空间，让卡片在「顶栏以下」的区域里垂直居中 */
.login-page {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;

  /* 顶部 ≥ 顶栏高度 + 间距；左右下保留原 24px */
  padding: 96px 24px 24px;
  box-sizing: border-box;
}

.card {
  --fg: #4a3a30;
  --fg-sub: rgba(74, 58, 48, 0.62);
  --glass-bg: rgba(255, 255, 255, 0.62);
  --glass-border: rgba(255, 255, 255, 0.78);
  --input-bg: rgba(255, 255, 255, 0.66);
  --input-border: rgba(107, 74, 58, 0.16);
  --btn-bg: linear-gradient(135deg, #ffb88c, #ff9a76);
  --btn-fg: #ffffff;
  --error: #d9433c;

  width: min(360px, 100%);
  padding: 34px 30px 26px;
  border-radius: 24px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(16px) saturate(120%);
  -webkit-backdrop-filter: blur(16px) saturate(120%);
  text-align: center;
  color: var(--fg);
}

.login-page[data-theme='dusk'] .card {
  --fg: #ffffff;
  --fg-sub: rgba(255, 255, 255, 0.72);
  --glass-bg: rgba(255, 255, 255, 0.14);
  --glass-border: rgba(255, 255, 255, 0.28);
  --input-bg: rgba(255, 255, 255, 0.1);
  --input-border: rgba(255, 255, 255, 0.24);
  --btn-bg: linear-gradient(135deg, #e88ba0, #a87ad8);
}

.login-page[data-theme='night'] .card {
  --fg: #f0efff;
  --fg-sub: rgba(200, 200, 255, 0.6);
  --glass-bg: rgba(255, 255, 255, 0.06);
  --glass-border: rgba(200, 200, 255, 0.2);
  --input-bg: rgba(255, 255, 255, 0.05);
  --input-border: rgba(200, 200, 255, 0.18);
  --btn-bg: linear-gradient(135deg, rgba(140, 160, 255, 0.9), rgba(180, 140, 255, 0.9));
  box-shadow: 0 0 40px rgba(120, 140, 255, 0.16);
}

.login-page[data-theme='midnight'] .card {
  --fg: #c8c8e8;
  --fg-sub: rgba(150, 160, 220, 0.55);
  --glass-bg: rgba(255, 255, 255, 0.045);
  --glass-border: rgba(150, 160, 220, 0.18);
  --input-bg: rgba(255, 255, 255, 0.04);
  --input-border: rgba(150, 160, 220, 0.16);
  --btn-bg: linear-gradient(135deg, rgba(120, 140, 255, 0.8), rgba(150, 120, 220, 0.8));
}

.card__logo {
  width: 52px;
  height: 52px;
  object-fit: contain;
  margin-bottom: 10px;
}

.card__title {
  margin: 0;
  font-size: 22px;
  letter-spacing: 2px;
  font-weight: 600;
}

.card__subtitle {
  margin: 6px 0 22px;
  font-size: 12px;
  letter-spacing: 1px;
  color: var(--fg-sub);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  text-align: left;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field__label {
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--fg-sub);
}

.field__input {
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

.field__input::placeholder {
  color: var(--fg-sub);
  opacity: 0.7;
}

.field__input:focus {
  border-color: #ff9a76;
  box-shadow: 0 0 0 3px rgba(255, 154, 118, 0.18);
}

.field__input--error {
  border-color: var(--error);
}

.field__input--error:focus {
  border-color: var(--error);
  box-shadow: 0 0 0 3px rgba(217, 67, 60, 0.18);
}

.field__hint {
  font-size: 11px;
  letter-spacing: 0.5px;
  color: var(--error);
}

.form__error {
  margin: 0;
  font-size: 12px;
  color: var(--error);
  text-align: center;
}

.form__submit {
  margin-top: 4px;
  padding: 12px;
  border: none;
  border-radius: 12px;
  background: var(--btn-bg);
  color: var(--btn-fg);
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  transition: transform 0.15s, filter 0.2s;
}

.form__submit:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.05);
}

.form__submit:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.card__toggle {
  margin-top: 18px;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--fg-sub);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: color 0.2s;
}

.card__toggle:hover {
  color: var(--fg);
}

/* ============================================================
 * 小屏：顶栏更矮（top 16px），底部 padding 收一点
 * ============================================================ */
@media (max-width: 520px) {
  .login-page {
    padding: 84px 16px 20px;
  }
}
</style>
