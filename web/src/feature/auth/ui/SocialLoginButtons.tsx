import { useGoogleLogin } from '@react-oauth/google'
import { toast } from 'sonner'
import { socialLoginApi } from '../api/mutate'
import { useAuthStore } from '../model/authStore'

function initKakao(): boolean {
	if (!window.Kakao) {
		toast.error('카카오 SDK가 로드되지 않았습니다. 페이지를 새로고침해 주세요.')
		return false
	}
	if (!window.Kakao.isInitialized()) {
		window.Kakao.init(import.meta.env.VITE_KAKAO_APP_KEY as string)
	}
	return true
}

function KakaoIcon() {
	return (
		<svg
			width="18"
			height="18"
			viewBox="0 0 18 18"
			fill="none"
			aria-hidden="true"
		>
			<path
				fillRule="evenodd"
				clipRule="evenodd"
				d="M9 1C4.582 1 1 3.836 1 7.333c0 2.244 1.492 4.21 3.738 5.334l-.953 3.553c-.084.313.28.567.549.376L8.62 13.97c.124.01.25.014.38.014 4.418 0 8-2.836 8-6.333S13.418 1 9 1z"
				fill="#000000"
			/>
		</svg>
	)
}

function GoogleIcon() {
	return (
		<svg
			width="18"
			height="18"
			viewBox="0 0 18 18"
			fill="none"
			aria-hidden="true"
		>
			<path
				d="M17.64 9.205c0-.639-.057-1.252-.164-1.841H9v3.481h4.844a4.14 4.14 0 0 1-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615z"
				fill="#4285F4"
			/>
			<path
				d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 0 0 9 18z"
				fill="#34A853"
			/>
			<path
				d="M3.964 10.71A5.41 5.41 0 0 1 3.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 0 0 0 9c0 1.452.348 2.827.957 4.042l3.007-2.332z"
				fill="#FBBC05"
			/>
			<path
				d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 0 0 .957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z"
				fill="#EA4335"
			/>
		</svg>
	)
}

export function SocialLoginButtons() {
	const setSocialAuth = useAuthStore((state) => state.setSocialAuth)

	const handleSocialLoginSuccess = async (
		providerAccessToken: string,
		provider: 'KAKAO' | 'GOOGLE',
		providerUserId: string,
		email?: string,
	) => {
		const res = await socialLoginApi(providerAccessToken, {
			provider,
			providerUserId,
			email,
		})
		if (res.result === 'success') {
			setSocialAuth(res.detail)
		} else {
			toast.error(
				res.detail.description[0]?.message ?? '소셜 로그인에 실패했습니다',
			)
		}
	}

	const handleKakaoLogin = () => {
		try {
			if (!initKakao()) return
			// Kakao SDK v2는 Auth.login() 팝업 방식을 제거함 → authorize() 리다이렉트 방식 사용
			window.Kakao.Auth.authorize({
				redirectUri: `${window.location.origin}/oauth/kakao`,
			})
		} catch (err) {
			console.error('[Kakao] 초기화 오류', err)
			toast.error('카카오 로그인을 시작할 수 없습니다')
		}
	}

	const handleGoogleLogin = useGoogleLogin({
		onSuccess: async (tokenResponse) => {
			try {
				const userInfo = await fetch(
					'https://www.googleapis.com/oauth2/v2/userinfo',
					{
						headers: { Authorization: `Bearer ${tokenResponse.access_token}` },
					},
				).then((r) => r.json() as Promise<{ id: string; email?: string }>)

				await handleSocialLoginSuccess(
					tokenResponse.access_token,
					'GOOGLE',
					userInfo.id,
					userInfo.email,
				)
			} catch {
				toast.error('구글 로그인에 실패했습니다')
			}
		},
		onError: () => toast.error('구글 로그인에 실패했습니다'),
	})

	return (
		<div className="flex flex-col gap-3">
			<button
				type="button"
				onClick={handleKakaoLogin}
				className="flex w-full items-center justify-center gap-2.5 rounded-xl bg-[#FEE500] py-3 text-sm font-semibold text-[#191919] transition-opacity hover:opacity-90"
			>
				<KakaoIcon />
				카카오로 로그인하기
			</button>

			<button
				type="button"
				onClick={() => handleGoogleLogin()}
				className="flex w-full items-center justify-center gap-2.5 rounded-xl border border-gray-200 bg-white py-3 text-sm font-semibold text-gray-700 transition-colors hover:bg-gray-50"
			>
				<GoogleIcon />
				구글로 로그인하기
			</button>
		</div>
	)
}
