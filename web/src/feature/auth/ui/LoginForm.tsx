import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { loginApi } from '../api/mutate'
import { useAuthStore } from '../model/authStore'
import { type LoginFormValues, loginSchema } from '../model/loginSchema'

export function LoginForm() {
  const setAuth = useAuthStore((state) => state.setAuth)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  })

  const { mutate, isPending } = useMutation({
    mutationFn: loginApi,
    onSuccess: (res) => {
      if (res.result === 'success') {
        setAuth(res.detail)
      }
    },
  })

  const onSubmit = (values: LoginFormValues) => {
    mutate(values)
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="flex flex-col gap-5 w-full"
    >
      {/* 이메일 */}
      <div className="flex flex-col gap-1.5">
        <label htmlFor="email" className="text-sm font-medium text-gray-700">
          이메일
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

      {/* 비밀번호 */}
      <div className="flex flex-col gap-1.5">
        <label htmlFor="password" className="text-sm font-medium text-gray-700">
          비밀번호
        </label>
        <Input
          id="password"
          type="password"
          placeholder="영문 + 숫자 조합 8자 이상"
          aria-invalid={!!errors.password}
          {...register('password')}
        />
        {errors.password && (
          <p className="text-xs text-red-400">{errors.password.message}</p>
        )}
      </div>

      {/* 제출 */}
      <Button
        type="submit"
        size="lg"
        className="mt-2 w-full"
        disabled={isPending}
      >
        {isPending ? '로그인 중...' : '로그인'}
      </Button>
    </form>
  )
}
