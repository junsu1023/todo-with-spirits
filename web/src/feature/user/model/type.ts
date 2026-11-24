// 서버 EmailVerificationStatus. NEEDS_REVERIFICATION은 이메일을 바꾼 뒤 다시 인증해야 하는 상태 (User.updateEmail)
// PENDING은 서버에 없는 값이라 NEEDS_REVERIFICATION으로 바꿈
export type EmailVerificationStatus =
	| 'VERIFIED'
	| 'UNVERIFIED'
	| 'NEEDS_REVERIFICATION'
export type PlanType = 'MONTHLY' | 'YEARLY'

export interface SubscriptionDetail {
	planType: PlanType
	startedAt: string
	expiresAt: string
	autoRenew: boolean
	premium: boolean
}
export type LoginType = 'LOCAL' | 'SOCIAL'
export type Provider = 'KAKAO' | 'GOOGLE'
export type Role = 'USER' | 'ADMIN'

export interface UpdateUserRequest {
	nickname?: string
	representativeSpiritId?: number
}

export interface UserProfile {
	userId: number
	nickname: string
	email: string | null
	emailVerificationStatus: EmailVerificationStatus
	loginType: LoginType
	provider: Provider | null
	premium: boolean
	role: Role
	representativeSpiritId: number
	createdAt: string
}
