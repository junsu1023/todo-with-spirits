import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { sendEmailVerificationApi } from '@/feature/auth/api/mutate'
import { checkEmailApi } from '@/feature/auth/api/query'
import {
	type SignupFormValues,
	signupSchema,
} from '@/feature/auth/model/signupSchema'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'

interface Props {
	onVerificationSent: (email: string, password: string) => void
}

export function CredentialsStep({ onVerificationSent }: Props) {
	const navigate = useNavigate()

	const {
		register,
		handleSubmit,
		setError,
		getValues,
		trigger,
		formState: { errors },
	} = useForm<SignupFormValues>({ resolver: zodResolver(signupSchema) })

	const handleEmailBlur = async () => {
		const valid = await trigger('email')
		if (!valid) return
		try {
			const res = await checkEmailApi(getValues('email'))
			if (res.result !== 'success' || !res.detail.registered) return
			const provider = res.detail.provider
			setError('email', {
				message:
					provider === 'KAKAO'
						? '이미 카카오 계정으로 가입된 이메일입니다.'
						: provider === 'GOOGLE'
							? '이미 구글 계정으로 가입된 이메일입니다.'
							: '이미 사용 중인 이메일입니다.',
			})
		} catch {
			// blur 검증 실패 시 스킵
		}
	}

	const { mutate, isPending } = useMutation({
		mutationFn: async ({
			email,
			password,
		}: {
			email: string
			password: string
		}) => {
			const checkRes = await checkEmailApi(email)
			if (checkRes.result === 'success' && checkRes.detail.registered) {
				throw Object.assign(new Error('duplicate'), {
					provider: checkRes.detail.provider,
				})
			}
			const sendRes = await sendEmailVerificationApi(email)
			if (sendRes.result !== 'success') throw new Error('send_failed')
			return { email, password }
		},
		onSuccess: ({ email, password }) => onVerificationSent(email, password),
		onError: (err: Error & { provider?: string | null }) => {
			if (err.message === 'duplicate') {
				setError('email', {
					message:
						err.provider === 'KAKAO'
							? '이미 카카오 계정으로 가입된 이메일입니다.'
							: err.provider === 'GOOGLE'
								? '이미 구글 계정으로 가입된 이메일입니다.'
								: '이미 사용 중인 이메일입니다.',
				})
			} else {
				toast.error('인증 메일 발송에 실패했어요. 잠시 후 다시 시도해주세요.')
			}
		},
	})

	const onSubmit = ({ passwordConfirm: _, ...values }: SignupFormValues) =>
		mutate(values)

	return (
		<div className="flex flex-col gap-8">
			<div className="flex flex-col gap-1">
				<h1 className="text-2xl font-bold text-gray-900">회원가입</h1>
				<p className="text-sm text-gray-400">
					Todo with Spirits와 함께 시작해요
				</p>
			</div>

			<form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-5">
				<div className="flex flex-col gap-1.5">
					<label htmlFor="email" className="text-sm font-medium text-gray-700">
						이메일 <span className="text-red-400">*</span>
					</label>
					<Input
						id="email"
						type="email"
						placeholder="example@email.com"
						aria-invalid={!!errors.email}
						{...register('email', { onBlur: handleEmailBlur })}
					/>
					{errors.email && (
						<p className="text-xs text-red-400">{errors.email.message}</p>
					)}
				</div>

				<div className="flex flex-col gap-1.5">
					<label
						htmlFor="password"
						className="text-sm font-medium text-gray-700"
					>
						비밀번호 <span className="text-red-400">*</span>
					</label>
					<Input
						id="password"
						type="password"
						placeholder="영문 + 숫자 조합 8~20자"
						aria-invalid={!!errors.password}
						{...register('password')}
					/>
					{errors.password && (
						<p className="text-xs text-red-400">{errors.password.message}</p>
					)}
				</div>

				<div className="flex flex-col gap-1.5">
					<label
						htmlFor="passwordConfirm"
						className="text-sm font-medium text-gray-700"
					>
						비밀번호 확인 <span className="text-red-400">*</span>
					</label>
					<Input
						id="passwordConfirm"
						type="password"
						placeholder="비밀번호를 한 번 더 입력해주세요"
						aria-invalid={!!errors.passwordConfirm}
						{...register('passwordConfirm')}
					/>
					{errors.passwordConfirm && (
						<p className="text-xs text-red-400">
							{errors.passwordConfirm.message}
						</p>
					)}
				</div>

				<Button
					type="submit"
					size="lg"
					className="mt-2 w-full"
					disabled={isPending}
				>
					{isPending ? '발송 중...' : '인증 메일 받기'}
				</Button>
			</form>

			<p className="text-center text-xs text-gray-400">
				이미 계정이 있으신가요?{' '}
				<button
					type="button"
					onClick={() => navigate('/login')}
					className="font-medium text-brand underline-offset-2 hover:underline"
				>
					로그인하기
				</button>
			</p>
		</div>
	)
}
