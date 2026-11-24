// 서버 EmailVerificationStatus. NEEDS_REVERIFICATION은 이메일을 바꾼 뒤 다시 인증해야 하는 상태 (User.updateEmail)
// PENDING은 서버에 없는 값이라 NEEDS_REVERIFICATION으로 바꿈
export type EmailVerificationStatus =
	| 'VERIFIED'
	| 'UNVERIFIED'
	| 'NEEDS_REVERIFICATION'
// 서버 PlanType. 구독한 적 없으면 FREE (서버가 조회 시 FREE 구독을 만들어 돌려준다)
export type PlanType = 'FREE' | 'MONTHLY' | 'YEARLY'

// 서버 SubscriptionResponse
export interface SubscriptionDetail {
	planType: PlanType
	// FREE면 null
	startedAt: string | null
	// FREE이거나 만료일 없는 구독이면 null
	expiresAt: string | null
	autoRenew: boolean
	// 유료 구독이 활성 상태인지 (만료일 반영). 유료 플랜이어도 만료되면 false
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
