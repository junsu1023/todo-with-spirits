import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { loginResponse, SocialLoginResponse } from './type'

// loginResponse 필드를 nullable로 변환
type NullableAuth = { [K in keyof loginResponse]: loginResponse[K] | null }

interface AuthState extends NullableAuth {
	setAuth: (payload: loginResponse) => void
	setTokens: (
		tokens: Pick<loginResponse, 'accessToken' | 'refreshToken' | 'tokenType'>,
	) => void
	setSocialAuth: (payload: SocialLoginResponse) => void
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
			setTokens: (tokens) => set(tokens),
			setSocialAuth: ({ userId, accessToken, refreshToken }) =>
				set({
					...initialState,
					userId,
					accessToken,
					refreshToken,
					tokenType: 'Bearer',
				}),
			clearAuth: () => set(initialState),
		}),
		{
			name: 'auth',
		},
	),
)
