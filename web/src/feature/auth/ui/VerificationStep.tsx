import { useMutation } from '@tanstack/react-query'
import { useCallback, useEffect, useRef, useState } from 'react'
import { toast } from 'sonner'
import {
	loginApi,
	sendEmailVerificationApi,
	signupApi,
	verifyEmailCodeApi,
} from '@/feature/auth/api/mutate'
import type { loginResponse } from '@/feature/auth/model/type'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'

const TIMER_SECONDS = 5 * 60

interface Props {
	email: string
	password: string
	onSuccess: (auth: loginResponse) => void
	onBack: () => void
	onTimeout: () => void
}

export function VerificationStep({
	email,
	password,
	onSuccess,
	onBack,
	onTimeout,
}: Props) {
	const [code, setCode] = useState('')
	const [codeError, setCodeError] = useState('')
	const [remaining, setRemaining] = useState(TIMER_SECONDS)

	const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)
	const onTimeoutRef = useRef(onTimeout)
	useEffect(() => {
		onTimeoutRef.current = onTimeout
	}, [onTimeout])

	const startTimer = useCallback(() => {
		if (timerRef.current) clearInterval(timerRef.current)
		setRemaining(TIMER_SECONDS)
		timerRef.current = setInterval(() => {
			setRemaining((prev) => {
				if (prev <= 1) {
					if (timerRef.current) clearInterval(timerRef.current)
					timerRef.current = null
					toast.error('인증 시간이 초과됐어요. 다시 시도해주세요.')
					onTimeoutRef.current()
					return 0
				}
				return prev - 1
			})
		}, 1000)
	}, [])

	useEffect(() => {
		startTimer()
		return () => {
			if (timerRef.current) clearInterval(timerRef.current)
		}
	}, [startTimer])

	const { mutate: verify, isPending } = useMutation({
		mutationFn: async () => {
			const codeNum = parseInt(code, 10)
			if (Number.isNaN(codeNum)) throw new Error('invalid_code')

			const verifyRes = await verifyEmailCodeApi({ email, code: codeNum })
			if (verifyRes.result !== 'success') throw new Error('wrong_code')

			const signupRes = await signupApi({ email, password })
			if (signupRes.result !== 'success') throw new Error('signup_failed')

			const loginRes = await loginApi({ email, password })
			if (loginRes.result !== 'success') throw new Error('login_failed')

			return loginRes.detail
		},
		onSuccess: (auth) => {
			if (timerRef.current) {
				clearInterval(timerRef.current)
				timerRef.current = null
			}
			onSuccess(auth)
		},
		onError: (err: Error) => {
			if (err.message === 'invalid_code' || err.message === 'wrong_code') {
				setCodeError('인증번호가 일치하지 않습니다.')
			} else {
				toast.error('인증에 실패했어요. 잠시 후 다시 시도해주세요.')
			}
		},
	})

	const handleResend = async () => {
		try {
			const res = await sendEmailVerificationApi(email)
			if (res.result === 'success') {
				setCode('')
				setCodeError('')
				startTimer()
				toast.success('인증 메일을 다시 보냈어요.')
			}
		} catch {
			toast.error('재발송에 실패했어요.')
		}
	}

	const minutes = Math.floor(remaining / 60)
	const seconds = remaining % 60

	return (
		<div className="flex flex-col gap-8">
			<div className="flex flex-col gap-1">
				<h1 className="text-2xl font-bold text-gray-900">이메일 인증</h1>
				<p className="text-sm text-gray-400">
					<span className="font-medium text-gray-700">{email}</span>으로 인증
					코드를 보냈어요.
				</p>
			</div>

			<div className="flex flex-col gap-5">
				<div className="flex flex-col gap-1.5">
					<label htmlFor="code" className="text-sm font-medium text-gray-700">
						인증번호
					</label>
					<div className="relative">
						<Input
							id="code"
							type="text"
							inputMode="numeric"
							placeholder="인증번호 6자리"
							value={code}
							onChange={(e) => {
								setCode(e.target.value)
								setCodeError('')
							}}
							aria-invalid={!!codeError}
							className="pr-20"
						/>
						<span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm font-light text-brand tabular-nums">
							{String(minutes).padStart(2, '0')}:
							{String(seconds).padStart(2, '0')}
						</span>
					</div>
					{codeError && <p className="text-xs text-red-400">{codeError}</p>}
				</div>

				<Button
					size="lg"
					className="w-full"
					disabled={!code || isPending}
					onClick={() => verify()}
				>
					{isPending ? '확인 중...' : '확인'}
				</Button>

				<div className="flex flex-col items-center gap-1 text-xs text-gray-400">
					<p>인증 메일을 받지 못하셨나요?</p>
					<button
						type="button"
						className="font-medium text-brand underline-offset-2 hover:underline"
						onClick={handleResend}
					>
						인증 메일 다시 보내기
					</button>
				</div>

				<button
					type="button"
					className="text-xs text-gray-400 underline-offset-2 hover:underline"
					onClick={() => {
						if (timerRef.current) clearInterval(timerRef.current)
						onBack()
					}}
				>
					← 이메일 변경
				</button>
			</div>
		</div>
	)
}
