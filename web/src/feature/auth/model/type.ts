export interface loginResponse {
	accessToken: string
	email: string
	nickname: string
	refreshToken: string
	tokenType: string
	userId: number
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
