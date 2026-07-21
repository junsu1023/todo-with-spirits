import ky from 'ky'

// ─── Response Types ───────────────────────────────────────────────────────────

export interface ApiSuccess<T> {
  result: 'success'
  detail: T
}

export interface ApiErrorDescription {
  field: string | null
  message: string
}

export interface ApiErrorDetail {
  status: number
  timestamp: string
  errorCode: string
  description: ApiErrorDescription[]
}

export interface ApiFail {
  result: 'fail'
  detail: ApiErrorDetail
}

export type ApiResponse<T> = ApiSuccess<T> | ApiFail

// ─── Client ───────────────────────────────────────────────────────────────────

export const apiClient = ky.create({
  baseUrl:
    import.meta.env.VITE_API_BASE_URL ??
    'https://todo-with-spirits.onrender.com',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 60000, // Render 무료 플랜 cold start 대응 (60초)
})
