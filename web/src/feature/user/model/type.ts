export type EmailVerificationStatus = 'VERIFIED' | 'UNVERIFIED' | 'PENDING'
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
