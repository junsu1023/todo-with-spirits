import ky from 'ky'
import { useAuthStore } from '@/feature/auth/model/authStore'

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

// ─── Reissue ─────────────────────────────────────────────────────────────────

interface ReissueDetail {
  accessToken: string
  refreshToken: string
  tokenType: string
}

const BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? 'https://todo-with-spirits.onrender.com'

// apiClient와 분리된 인스턴스 — afterResponse 훅이 없어 무한 루프 방지
const plainKy = ky.create({ baseUrl: BASE_URL, timeout: 60000 })

async function tryReissue(): Promise<ReissueDetail | null> {
  const { refreshToken } = useAuthStore.getState()
  if (!refreshToken) return null

  try {
    const res = await plainKy
      .post('api/auth/reissue', { json: { refreshToken } })
      .json<ApiResponse<ReissueDetail>>()

    return res.result === 'success' ? res.detail : null
  } catch {
    return null
  }
}

// ─── Client ───────────────────────────────────────────────────────────────────

export const apiClient = ky.create({
  baseUrl: BASE_URL,
  timeout: 60000,
  hooks: {
    beforeRequest: [
      ({ request }) => {
        const { accessToken } = useAuthStore.getState()
        if (accessToken) {
          request.headers.set('Authorization', `Bearer ${accessToken}`)
        }
      },
    ],
    afterResponse: [
      async ({ request, response }) => {
        if (response.status !== 401) return response

        const tokens = await tryReissue()

        if (!tokens) {
          // reissue도 401 → 로그아웃 (ProtectedRoute가 /login으로 리다이렉트)
          useAuthStore.getState().clearAuth()
          return response
        }

        // 새 토큰 저장 후 원래 요청 재시도
        useAuthStore.getState().setTokens(tokens)
        request.headers.set('Authorization', `Bearer ${tokens.accessToken}`)
        return ky(request)
      },
    ],
  },
})
