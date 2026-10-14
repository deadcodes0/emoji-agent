// src/utils/http.ts
import axios from 'axios'
import type {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig,
  AxiosError,
} from 'axios'

// ========== 1. 自定义类型 ==========

/** 后端统一响应结构 —— 默认泛型用 unknown 而不是 any */
interface BaseResponse<T = unknown> {
  code: number
  message: string
  data: T
  ok: boolean
}

/** 扩展 axios 请求配置 */
interface RequestConfig extends AxiosRequestConfig {
  showLoading?: boolean
  customError?: boolean
}

// ========== 2. 常量配置 ==========

const BASE_CONFIG: RequestConfig = {
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
  },
}

const SUCCESS_CODE = 200
const UNAUTHORIZED_CODES = [201, 206]

// ========== 3. 工具函数：安全地从 unknown 中取 message ==========

function extractMessage(data: unknown): string | undefined {
  if (
    data !== null &&
    typeof data === 'object' &&
    'message' in data &&
    typeof (data as { message: unknown }).message === 'string'
  ) {
    return (data as { message: string }).message
  }
  return undefined
}

// ========== 4. Http 类 ==========

class Http {
  private instance: AxiosInstance
  private baseConfig: RequestConfig

  constructor(config: RequestConfig = {}) {
    this.baseConfig = { ...BASE_CONFIG, ...config }
    this.instance = axios.create(this.baseConfig)
    this.setupInterceptors()
  }

  // ---------- 拦截器 ----------

  private setupInterceptors(): void {
    // 请求拦截：注入 Token
    this.instance.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const token = this.getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error: AxiosError) => Promise.reject(error),
    )

    // 响应拦截：统一处理业务状态码
    this.instance.interceptors.response.use(
      (response: AxiosResponse<BaseResponse>) => {
        const { code, message, data } = response.data

        if (code === SUCCESS_CODE) {
          return response
        }

        if (UNAUTHORIZED_CODES.includes(code)) {
          this.handleUnauthorized(message)
          return Promise.reject(new Error(message || '登录已失效'))
        }

        const errText = extractMessage(data) || message || '请求失败'
        this.showError(errText)
        return Promise.reject(new Error(message || '请求失败'))
      },
      (error: AxiosError) => {
        this.handleHttpError(error)
        return Promise.reject(error)
      },
    )
  }

  // ---------- 错误处理 ----------

  private handleUnauthorized(message?: string): void {
    this.removeToken()
    this.showError(message || '登录已失效，请重新登录')
  }

  private handleHttpError(error: AxiosError): void {
    let message = ''

    if (error.response) {
      const { status, data } = error.response
      const serverMessage = extractMessage(data)

      switch (status) {
        case 400:
          message = serverMessage || '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          break
        case 403:
          message = '没有权限访问该资源'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 408:
          message = '请求超时'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        default:
          message = serverMessage || `请求错误 (${status})`
      }
    } else if (error.request) {
      message = '网络异常，请检查网络连接'
    } else {
      message = error.message || '请求配置错误'
    }

    this.showError(message)
  }

  // ---------- 工具方法 ----------

  private getToken(): string | null {
    // return localStorage.getItem('token')
    return null
  }

  private removeToken(): void {
    // localStorage.removeItem('token')
  }

  private showError(message: string): void {
    // ElMessage.error(message)
    console.error('[HTTP Error]', message)
  }

  // ---------- 通用请求方法（泛型默认 unknown） ----------

  public request<T = unknown>(config: RequestConfig): Promise<BaseResponse<T>> {
    return this.instance.request<BaseResponse<T>>(config).then((res) => res.data)
  }

  public get<T = unknown>(
    url: string,
    params?: object,
    config?: RequestConfig,
  ): Promise<BaseResponse<T>> {
    return this.request<T>({ url, method: 'GET', params, ...config })
  }

  public post<T = unknown>(
    url: string,
    data?: object,
    config?: RequestConfig,
  ): Promise<BaseResponse<T>> {
    return this.request<T>({ url, method: 'POST', data, ...config })
  }

  public put<T = unknown>(
    url: string,
    data?: object,
    config?: RequestConfig,
  ): Promise<BaseResponse<T>> {
    return this.request<T>({ url, method: 'PUT', data, ...config })
  }

  public patch<T = unknown>(
    url: string,
    data?: object,
    config?: RequestConfig,
  ): Promise<BaseResponse<T>> {
    return this.request<T>({ url, method: 'PATCH', data, ...config })
  }

  public delete<T = unknown>(
    url: string,
    params?: object,
    config?: RequestConfig,
  ): Promise<BaseResponse<T>> {
    return this.request<T>({ url, method: 'DELETE', params, ...config })
  }
}

export default new Http()