import { LoginForm } from '@/feature/auth/ui/LoginForm'

export function LoginPage() {
  return (
    <main className="flex h-screen items-center justify-center bg-gray-50">
      <div className="flex w-full max-w-sm flex-col gap-8 rounded-2xl bg-white p-8 shadow-sm ring-1 ring-gray-100">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl font-bold text-gray-900">로그인</h1>
          <p className="text-sm text-gray-400">
            Todo with Spirits에 오신 걸 환영해요
          </p>
        </div>
        <LoginForm />
      </div>
    </main>
  )
}
