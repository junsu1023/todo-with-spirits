import ky from 'ky'
import { authStorage } from '@/feature/auth/model/authStorage'
import { API_BASE_URL } from './config'

// ─── Response Types (web/src/lib/api.ts와 동일) ───────────────────────────────

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

// 웹과 달리 401이어도 토큰 재발급(api/auth/reissue)을 하지 않는다.
// 웹과 refresh token을 공유하면 백엔드가 둘 다 로그아웃시키기 때문 (docs/spec.md 7번)
export const apiClient = ky.create({
  baseUrl: API_BASE_URL,
  timeout: 60000,
  hooks: {
    beforeRequest: [
      async ({ request }) => {
        const auth = await authStorage.getValue()
        if (auth) {
          request.headers.set('Authorization', `Bearer ${auth.accessToken}`)
        }
      },
    ],
    afterResponse: [
      async ({ response }) => {
        // 토큰 만료 → 익스텐션 로그아웃. 사이드패널이 로그인 화면으로 바뀐다
        if (response.status === 401) await authStorage.setValue(null)
        return response
      },
    ],
  },
})
