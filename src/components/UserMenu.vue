<!-- src/components/UserMenu.vue -->
<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useTheme } from '@/composables/useTheme'
import { updateProfile } from '@/utils/auth'
import ChangePasswordDialog from '@/components/ChangePasswordDialog.vue'

const router = useRouter()
const { currentUser, logout, refresh } = useAuth()
const { theme } = useTheme()

const open = ref(false)
const showPasswordDialog = ref(false)

/* ---------- 昵称内联编辑 ---------- */
const editingNickname = ref(false)
const nicknameDraft = ref('')
const nicknameInputRef = ref<HTMLInputElement | null>(null)
let nicknameHandled = false

/* ---------- 头像上传 ---------- */
const avatarInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const errorMsg = ref('')

/** 原始文件大小上限 4MB（压缩前的保险丝） */
const MAX_AVATAR_SIZE = 4 * 1024 * 1024

const nickname = computed(() => currentUser.value?.nickname?.trim() || '我')
const account = computed(() => currentUser.value?.accountNumber ?? '')

function onEnter() {
  open.value = true
}

function onLeave() {
  if (editingNickname.value) return // 正在编辑时不关闭
  open.value = false
}

function close() {
  open.value = false
}

/* ============================================================
 * 昵称
 * ============================================================ */
function startEditNickname() {
  nicknameDraft.value = nickname.value
  nicknameHandled = false
  editingNickname.value = true
  errorMsg.value = ''
  nextTick(() => nicknameInputRef.value?.focus())
}

async function commitNickname() {
  const v = nicknameDraft.value.trim()

  if (!v || v === currentUser.value?.nickname) {
    editingNickname.value = false
    return
  }

  try {
    await updateProfile({ nickname: v })
    refresh()
  } catch (e) {
    errorMsg.value = e instanceof Error ? e.message : '昵称修改失败'
  } finally {
    editingNickname.value = false
  }
}

function cancelEditNickname() {
  editingNickname.value = false
  nicknameDraft.value = ''
}

function onNicknameKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    e.preventDefault()
    nicknameHandled = true
    void commitNickname()
  } else if (e.key === 'Escape') {
    e.preventDefault()
    nicknameHandled = true
    cancelEditNickname()
  }
}

function onNicknameBlur() {
  if (nicknameHandled) {
    nicknameHandled = false
    return
  }
  void commitNickname()
}

/* ============================================================
 * 头像
 * ============================================================ */
function onAvatarClick() {
  if (uploading.value) return
  avatarInputRef.value?.click()
}

/**
 * 把 File 缩放到 maxSize×maxSize 内（保持比例），输出 JPEG data URL。
 * - 统一去透明底为白（JPEG 不支持 alpha）
 * - 质量 0.82，256px 下通常 < 30KB
 */
function fileToCompressedDataUrl(
  file: File,
  maxSize = 256,
  quality = 0.82,
): Promise<string> {
  return new Promise((resolve, reject) => {
    const objectUrl = URL.createObjectURL(file)
    const img = new Image()

    img.onload = () => {
      URL.revokeObjectURL(objectUrl)

      const { width, height } = img
      const scale = Math.min(1, maxSize / Math.max(width, height))
      const w = Math.max(1, Math.round(width * scale))
      const h = Math.max(1, Math.round(height * scale))

      const canvas = document.createElement('canvas')
      canvas.width = w
      canvas.height = h

      const ctx = canvas.getContext('2d')
      if (!ctx) {
        reject(new Error('浏览器不支持 canvas'))
        return
      }

      // 白底，避免 PNG 透明变黑
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, w, h)
      ctx.drawImage(img, 0, 0, w, h)

      try {
        resolve(canvas.toDataURL('image/jpeg', quality))
      } catch (e) {
        reject(e instanceof Error ? e : new Error('图片处理失败'))
      }
    }

    img.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      reject(new Error('图片加载失败'))
    }

    img.src = objectUrl
  })
}

async function onAvatarFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = '' // 允许同一文件再次选择
  if (!file) return

  errorMsg.value = ''

  if (!file.type.startsWith('image/')) {
    errorMsg.value = '请选择图片文件'
    return
  }
  if (file.size > MAX_AVATAR_SIZE) {
    errorMsg.value = '图片请小于 4MB'
    return
  }

  uploading.value = true
  try {
    const dataUrl = await fileToCompressedDataUrl(file)
    await updateProfile({ avatarUrl: dataUrl })
    refresh()
  } catch (err) {
    errorMsg.value = err instanceof Error ? err.message : '头像修改失败'
  } finally {
    uploading.value = false
  }
}

/* ============================================================
 * 菜单动作
 * ============================================================ */
function onModifyPassword() {
  close()
  showPasswordDialog.value = true
}

function onPasswordDialogClose() {
  showPasswordDialog.value = false
}

function onLogout() {
  close()
  logout()
  router.replace({ name: 'home' })
}
</script>

<template>
  <div class="user-menu" :data-theme="theme" @mouseenter="onEnter" @mouseleave="onLeave">
    <!-- 触发器：头像 + 箭头 -->
    <button class="user-menu__trigger" type="button" :aria-expanded="open">
      <span class="user-menu__avatar">
        <img v-if="currentUser?.avatarUrl" :src="currentUser.avatarUrl" alt="" />
        <span v-else class="user-menu__initial">我</span>
      </span>
      <span class="user-menu__caret" aria-hidden="true">
        {{ open ? '^' : 'v' }}
      </span>
    </button>

    <!-- 下拉面板 -->
    <Transition name="menu">
      <div v-if="open" class="user-menu__panel">
        <div class="user-menu__panel-inner">
          <!-- 个人信息 -->
          <div class="user-menu__head">
            <!-- 头像：点击上传 -->
            <button
              class="user-menu__avatar user-menu__avatar--lg user-menu__avatar--clickable"
              type="button"
              title="点击更换头像"
              :disabled="uploading"
              @click="onAvatarClick"
            >
              <img v-if="currentUser?.avatarUrl" :src="currentUser.avatarUrl" alt="" />
              <span v-else class="user-menu__initial">我</span>
              <span class="user-menu__avatar-mask">
                {{ uploading ? '…' : '换' }}
              </span>
            </button>

            <div class="user-menu__info">
              <!-- 昵称：点击变输入框 -->
              <input
                v-if="editingNickname"
                ref="nicknameInputRef"
                v-model="nicknameDraft"
                class="user-menu__name-input"
                maxlength="32"
                @keydown="onNicknameKeydown"
                @blur="onNicknameBlur"
              />
              <p
                v-else
                class="user-menu__name"
                title="点击修改昵称"
                @click="startEditNickname"
              >
                {{ nickname }}
              </p>

              <p v-if="account !== ''" class="user-menu__account">
                账号 {{ account }}
              </p>
            </div>
          </div>

          <p v-if="errorMsg" class="user-menu__error">{{ errorMsg }}</p>

          <div class="user-menu__divider" />

          <button class="user-menu__item" type="button" @click="onModifyPassword">
            修改密码
          </button>

          <button class="user-menu__item" type="button" @click="onLogout">
            退出登录
          </button>
        </div>
      </div>
    </Transition>

    <!-- 隐藏的文件选择 -->
    <input
      ref="avatarInputRef"
      class="user-menu__file"
      type="file"
      accept="image/*"
      @change="onAvatarFileChange"
    />

    <!-- 修改密码弹窗 -->
    <ChangePasswordDialog :open="showPasswordDialog" @close="onPasswordDialogClose" />
  </div>
</template>

<style scoped>
/* =========================================================
 * 主题变量（默认：日间）
 * ========================================================= */
.user-menu {
  position: relative;
  display: inline-flex;
  align-items: center;

  --um-fg: rgba(107, 74, 58, 0.72);
  --um-fg-strong: #3a2419;
  --um-avatar-bg: rgba(255, 255, 255, 0.85);
  --um-avatar-fg: #6b4a3a;
  --um-avatar-border: rgba(255, 255, 255, 0.9);
  --um-panel-bg: rgba(255, 255, 255, 0.58);
  --um-panel-border: rgba(255, 255, 255, 0.78);
  --um-panel-shadow: 0 14px 36px rgba(0, 0, 0, 0.12);
  --um-divider: rgba(107, 74, 58, 0.14);
  --um-item-hover: rgba(0, 0, 0, 0.06);
  --um-error: #d9433c;
  --um-mask-bg: rgba(0, 0, 0, 0.45);
}

.user-menu[data-theme='dusk'] {
  --um-fg: rgba(255, 255, 255, 0.82);
  --um-fg-strong: #ffffff;
  --um-avatar-bg: rgba(255, 255, 255, 0.22);
  --um-avatar-fg: #ffffff;
  --um-avatar-border: rgba(255, 255, 255, 0.42);
  --um-panel-bg: rgba(40, 14, 52, 0.55);
  --um-panel-border: rgba(255, 255, 255, 0.24);
  --um-panel-shadow: 0 14px 36px rgba(60, 30, 80, 0.28);
  --um-divider: rgba(255, 255, 255, 0.16);
  --um-item-hover: rgba(255, 255, 255, 0.12);
  --um-error: #ff9a9a;
}

.user-menu[data-theme='night'] {
  --um-fg: rgba(232, 232, 255, 0.78);
  --um-fg-strong: #ffffff;
  --um-avatar-bg: rgba(255, 255, 255, 0.1);
  --um-avatar-fg: #e8e8ff;
  --um-avatar-border: rgba(200, 200, 255, 0.28);
  --um-panel-bg: rgba(18, 22, 54, 0.62);
  --um-panel-border: rgba(200, 200, 255, 0.2);
  --um-panel-shadow: 0 14px 36px rgba(0, 0, 30, 0.45);
  --um-divider: rgba(200, 200, 255, 0.14);
  --um-item-hover: rgba(255, 255, 255, 0.08);
  --um-error: #ff9a9a;
}

.user-menu[data-theme='midnight'] {
  --um-fg: rgba(200, 200, 232, 0.72);
  --um-fg-strong: #e8e8ff;
  --um-avatar-bg: rgba(255, 255, 255, 0.08);
  --um-avatar-fg: #c8c8e8;
  --um-avatar-border: rgba(150, 160, 220, 0.26);
  --um-panel-bg: rgba(8, 10, 26, 0.7);
  --um-panel-border: rgba(150, 160, 220, 0.18);
  --um-panel-shadow: 0 14px 36px rgba(0, 0, 20, 0.6);
  --um-divider: rgba(150, 160, 220, 0.14);
  --um-item-hover: rgba(255, 255, 255, 0.06);
  --um-error: #ff9a9a;
}

/* =========================================================
 * 触发器
 * ========================================================= */
.user-menu__trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  border: none;
  background: none;
  color: var(--um-fg);
  font-family: inherit;
  font-size: var(--topbar-font-size, 15px);
  line-height: 1;
  cursor: pointer;
  transition: color 0.3s ease;
}

.user-menu__trigger:hover {
  color: var(--um-fg-strong);
}

.user-menu__avatar {
  position: relative;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--um-avatar-bg);
  border: 1px solid var(--um-avatar-border);
  color: var(--um-avatar-fg);
  font-size: 14px;
  font-weight: 600;
  flex: 0 0 auto;
  transition: background 0.3s ease, border-color 0.3s ease, transform 0.2s ease;
}

.user-menu__trigger:hover .user-menu__avatar {
  transform: translateY(-1px);
}

.user-menu__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.user-menu__initial {
  line-height: 1;
}

.user-menu__caret {
  min-width: 12px;
  font-size: 12px;
  line-height: 1;
  text-align: center;
  opacity: 0.75;
  transition: opacity 0.2s ease;
}

.user-menu__trigger:hover .user-menu__caret {
  opacity: 1;
}

/* 头像大图（可点击） */
.user-menu__avatar--lg {
  width: 40px;
  height: 40px;
  font-size: 16px;
  padding: 0;
  cursor: default;
}

.user-menu__avatar--clickable {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.user-menu__avatar--clickable:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.18);
}

.user-menu__avatar--clickable:disabled {
  cursor: wait;
  opacity: 0.7;
}

/* 头像 hover 遮罩 */
.user-menu__avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--um-mask-bg);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.user-menu__avatar--clickable:hover .user-menu__avatar-mask {
  opacity: 1;
}

/* =========================================================
 * 下拉面板
 * ========================================================= */
.user-menu__panel {
  position: absolute;
  top: 100%;
  right: 0;
  z-index: 20;
  padding-top: 10px;
}

.user-menu__panel-inner {
  min-width: 220px;
  padding: 12px;
  border-radius: 16px;
  background: var(--um-panel-bg);
  border: 1px solid var(--um-panel-border);
  box-shadow: var(--um-panel-shadow);
  backdrop-filter: blur(18px) saturate(120%);
  -webkit-backdrop-filter: blur(18px) saturate(120%);
}

/* 个人信息头 */
.user-menu__head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 4px 10px;
}

.user-menu__info {
  flex: 1;
  min-width: 0;
}

/* 昵称（可点击） */
.user-menu__name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--um-fg-strong);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: color 0.2s ease;
}

.user-menu__name:hover {
  color: var(--um-fg-strong);
  text-decoration: underline;
  text-decoration-style: dotted;
  text-underline-offset: 3px;
}

/* 昵称内联输入（编辑态：下划线） */
.user-menu__name-input {
  width: 100%;
  padding: 0 0 2px;
  margin: 0;
  border: none;
  border-bottom: 1px solid var(--um-fg-strong);
  background: none;
  color: var(--um-fg-strong);
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.2;
  outline: none;
}

.user-menu__account {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--um-fg);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 错误提示 */
.user-menu__error {
  margin: 0 4px 6px;
  font-size: 12px;
  color: var(--um-error);
  text-align: left;
}

.user-menu__divider {
  height: 1px;
  background: var(--um-divider);
  margin: 4px 0;
}

/* 菜单条目 */
.user-menu__item {
  display: block;
  width: 100%;
  padding: 8px 10px;
  border: none;
  border-radius: 10px;
  background: none;
  color: var(--um-fg-strong);
  font-family: inherit;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s ease;
}

.user-menu__item:hover {
  background: var(--um-item-hover);
}

/* 隐藏的文件输入 */
.user-menu__file {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
  border: 0;
}

/* 展开 / 收起动画 */
.menu-enter-active,
.menu-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.menu-enter-from,
.menu-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
