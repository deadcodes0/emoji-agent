// src/utils/auth.ts
const TOKEN_KEY = 'token'
const USER_KEY = 'user'

export interface AuthUser {
  id: number
  accountNumber: number
  nickname: string
  avatarUrl: string | null
}

interface LoginResponse {
  token: string
  expiresIn: number
  user: AuthUser
}

// ---------- 登录态存取 ----------

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

function setUser(user: AuthUser): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function logout(): void {
  clearAuth()
  location.href = '/login'
}

// ---------- 通用请求 ----------

export async function request<T>(
  url: string,
  { method = 'GET', body }: { method?: string; body?: object } = {},
): Promise<T> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  })

  if (res.status === 401) {
    clearAuth()
    location.href = '/login'
    throw new Error('登录已过期')
  }
  if (!res.ok) {
    const err = (await res.json().catch(() => ({}))) as { message?: string }
    throw new Error(err.message || `请求失败 ${res.status}`)
  }
  if (res.status === 204) return null as T

  // ★ 空 body（如 void 接口）返回 null，避免 res.json() 抛错
  const text = await res.text()
  if (!text) return null as T
  try {
    return JSON.parse(text) as T
  } catch {
    return null as T
  }
}

// ---------- 鉴权接口 ----------

export async function login(accountNumber: number, password: number): Promise<AuthUser> {
  const data = await request<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: { accountNumber, password },
  })
  setToken(data.token)
  setUser(data.user)
  return data.user
}

export async function register(
  accountNumber: number,
  password: number,
  nickname?: string,
): Promise<AuthUser> {
  const data = await request<LoginResponse>('/api/auth/register', {
    method: 'POST',
    body: { accountNumber, password, nickname: nickname || undefined },
  })
  setToken(data.token)
  setUser(data.user)
  return data.user
}

export async function me(): Promise<AuthUser> {
  return request<AuthUser>('/api/auth/me')
}

/** 修改密码：旧密码、新密码均为数字（与后端 Integer 对应） */
export async function updatePassword(
  oldPassword: number,
  newPassword: number,
): Promise<void> {
  await request<void>('/api/auth/password', {
    method: 'POST',
    body: { oldPassword, newPassword },
  })
}

export async function updateProfile(patch: {
  nickname?: string
  avatarUrl?: string | null
}): Promise<AuthUser> {
  const user = await request<AuthUser>('/api/auth/profile', {
    method: 'POST',
    body: patch,
  })
  setUser(user)
  return user
}

/** 供非 JSON 请求（如 stop）携带 token 的 header 构造 */
export function authHeaders(): Record<string, string> {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}
