import { useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'
import { socialLoginApi } from '@/feature/auth/api/mutate'
import { useAuthStore } from '@/feature/auth/model/authStore'

export function KakaoCallbackPage() {
	const [searchParams] = useSearchParams()
	const navigate = useNavigate()
	const setSocialAuth = useAuthStore((state) => state.setSocialAuth)
	const called = useRef(false)

	useEffect(() => {
		// React StrictMode 이중 실행 방지
		if (called.current) return
		called.current = true

		const code = searchParams.get('code')
		const error = searchParams.get('error')

		if (error || !code) {
			toast.error('카카오 로그인에 실패했습니다')
			navigate('/login', { replace: true })
			return
		}

		async function handleCallback(authCode: string) {
			try {
				// Kakao JS SDK authorize()는 JavaScript App Key로 인가하므로
				// 토큰 교환도 동일 키(VITE_KAKAO_APP_KEY) 사용
				const tokenRes = await fetch('https://kauth.kakao.com/oauth/token', {
					method: 'POST',
					headers: {
						'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8',
					},
					body: new URLSearchParams({
						grant_type: 'authorization_code',
						client_id: import.meta.env.VITE_KAKAO_APP_KEY as string,
						redirect_uri: `${window.location.origin}/oauth/kakao`,
						code: authCode,
					}),
				})

				const tokenData = (await tokenRes.json()) as {
					access_token?: string
					error?: string
					error_description?: string
				}

				if (!tokenData.access_token) {
					throw new Error(tokenData.error_description ?? '카카오 토큰 발급 실패')
				}

				const accessToken = tokenData.access_token

				const userRes = await fetch('https://kapi.kakao.com/v2/user/me', {
					headers: { Authorization: `Bearer ${accessToken}` },
				})
				const userInfo = (await userRes.json()) as {
					id: number
					kakao_account?: { email?: string }
				}

				const res = await socialLoginApi(accessToken, {
					provider: 'KAKAO',
					providerUserId: String(userInfo.id),
					email: userInfo.kakao_account?.email,
				})

				if (res.result === 'success') {
					setSocialAuth(res.detail)
					navigate('/', { replace: true })
				} else {
					toast.error(res.detail.description[0]?.message ?? '로그인에 실패했습니다')
					navigate('/login', { replace: true })
				}
			} catch (err) {
				console.error('[Kakao] callback error', err)
				toast.error(err instanceof Error ? err.message : '카카오 로그인에 실패했습니다')
				navigate('/login', { replace: true })
			}
		}

		handleCallback(code)
	}, [])

	return (
		<div className="flex h-screen items-center justify-center bg-gray-50">
			<p className="text-sm text-gray-400">카카오 로그인 중...</p>
		</div>
	)
}
