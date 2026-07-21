import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { loginResponse } from './type'

// loginResponse 필드를 nullable로 변환
type NullableAuth = { [K in keyof loginResponse]: loginResponse[K] | null }

interface AuthState extends NullableAuth {
  setAuth: (payload: loginResponse) => void
  clearAuth: () => void
}

const initialState: NullableAuth = {
  userId: null,
  email: null,
  nickname: null,
  accessToken: null,
  refreshToken: null,
  tokenType: null,
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      ...initialState,

      setAuth: (payload) => set(payload),
      clearAuth: () => set(initialState),
    }),
    {
      name: 'auth',
    },
  ),
)
