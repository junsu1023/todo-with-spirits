export {}

interface KakaoAuthToken {
	access_token: string
	token_type: string
	refresh_token: string
	expires_in: number
	scope: string
}

interface KakaoUserResponse {
	id: number
	kakao_account?: {
		email?: string
		email_needs_agreement?: boolean
	}
}

interface KakaoStatic {
	init(appKey: string): void
	isInitialized(): boolean
	Auth: {
		authorize(settings: { redirectUri: string; scope?: string }): void
		getAccessToken(): string | null
	}
	API: {
		request(settings: {
			url: string
			success: (response: KakaoUserResponse) => void
			fail?: (err: unknown) => void
		}): void
	}
}

declare global {
	interface Window {
		Kakao: KakaoStatic
	}
}
