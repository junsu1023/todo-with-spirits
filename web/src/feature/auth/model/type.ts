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

// 서버는 소셜 로그인도 일반 로그인과 같은 LoginResponse를 준다 (android SocialLoginResponse와 같음)
// 소셜 계정은 이메일이 없을 수 있다
export interface SocialLoginResponse {
	tokenType: string
	accessToken: string
	refreshToken: string
	userId: number
	email: string | null
	nickname: string
	// 서버 응답에 없는 값
	// premium: boolean
	// isNewUser: boolean
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

export interface CheckEmailResponse {
	provider: 'KAKAO' | 'GOOGLE' | null
	registered: boolean
}
