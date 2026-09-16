export interface loginResponse {
	accessToken: string
	email: string
	nickname: string
	refreshToken: string
	tokenType: string
	userId: number
}

export type SocialProvider = 'KAKAO' | 'GOOGLE'

export interface SocialLoginRequest {
	provider: SocialProvider
	providerUserId: string
	email?: string
}

export interface SocialLoginResponse {
	userId: number
	accessToken: string
	refreshToken: string
	premium: boolean
	isNewUser: boolean
}

export interface SignupRequest {
	email: string
	password: string
	nickname?: string
}

export interface SignupResponse {
	email: string
	nickname: string
	userId: number
}
