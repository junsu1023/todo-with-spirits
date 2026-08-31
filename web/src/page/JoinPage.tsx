import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { signupApi } from '@/feature/auth/api/mutate'
import {
	type SignupFormValues,
	signupSchema,
} from '@/feature/auth/model/signupSchema'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'

export function JoinPage() {
	const navigate = useNavigate()

	const {
		register,
		handleSubmit,
		setError,
		formState: { errors },
	} = useForm<SignupFormValues>({
		resolver: zodResolver(signupSchema),
	})

	const { mutate, isPending } = useMutation({
		mutationFn: signupApi,
		onSuccess: (res) => {
			if (res.result === 'success') {
				toast.success('회원가입이 완료됐어요. 로그인해주세요.')
				navigate('/login', { replace: true })
				return
			}

			const { errorCode, description } = res.detail

			if (
				errorCode === 'INVALID_PARAMETER' ||
				errorCode === 'DUPLICATE_EMAIL'
			) {
				for (const err of description) {
					if (err.field === 'email') {
						setError('email', {
							message:
								errorCode === 'DUPLICATE_EMAIL'
									? '이미 사용 중인 이메일입니다.'
									: err.message,
						})
					} else if (err.field === 'password') {
						setError('password', { message: err.message })
					}
				}
				return
			}

			toast.error('회원가입에 실패했어요. 잠시 후 다시 시도해주세요.')
		},
		onError: () => {
			toast.error('네트워크 오류가 발생했어요. 잠시 후 다시 시도해주세요.')
		},
	})

	const onSubmit = (values: SignupFormValues) => {
		mutate(values)
	}

	return (
		<main className="flex h-screen items-center justify-center bg-gray-50">
			<div className="flex w-full max-w-sm flex-col gap-8 rounded-2xl bg-white p-8 shadow-sm ring-1 ring-gray-100">
				<div className="flex flex-col gap-1">
					<h1 className="text-2xl font-bold text-gray-900">회원가입</h1>
					<p className="text-sm text-gray-400">
						Todo with Spirits와 함께 시작해요
					</p>
				</div>

				<form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-5">
					<div className="flex flex-col gap-1.5">
						<label
							htmlFor="email"
							className="text-sm font-medium text-gray-700"
						>
							이메일 <span className="text-red-400">*</span>
						</label>
						<Input
							id="email"
							type="email"
							placeholder="example@email.com"
							aria-invalid={!!errors.email}
							{...register('email')}
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
							htmlFor="nickname"
							className="text-sm font-medium text-gray-700"
						>
							닉네임{' '}
							<span className="text-xs font-normal text-gray-400">(선택)</span>
						</label>
						<Input
							id="nickname"
							type="text"
							placeholder="미입력 시 자동 생성됩니다"
							aria-invalid={!!errors.nickname}
							{...register('nickname')}
						/>
						{errors.nickname && (
							<p className="text-xs text-red-400">{errors.nickname.message}</p>
						)}
					</div>

					<Button
						type="submit"
						size="lg"
						className="mt-2 w-full"
						disabled={isPending}
					>
						{isPending ? '가입 중...' : '회원가입'}
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
		</main>
	)
}
