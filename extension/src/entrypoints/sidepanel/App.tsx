import { authStorage } from '@/feature/auth/model/authStorage'
import { useAuth } from '@/feature/auth/model/useAuth'
import { LoginScreen } from '@/feature/auth/ui/LoginScreen'
import { TodayPage } from '@/page/TodayPage'

export function App() {
  const auth = useAuth()
  const today = new Date().toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  })

  // 저장소 읽는 중
  if (auth === undefined) return null

  return (
    <main className="flex h-screen flex-col gap-4 p-4">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-lg font-bold">Todo with Spirits</h1>
          <p className="text-sm text-neutral-500">{today}</p>
        </div>
        {auth && (
          <button
            type="button"
            // 익스텐션에서만 로그아웃 (웹 로그인 상태는 그대로)
            onClick={() => authStorage.setValue(null)}
            className="text-xs text-neutral-400 hover:text-neutral-600"
          >
            로그아웃
          </button>
        )}
      </header>

      {auth ? (
        <>
          {auth.nickname && (
            <p className="text-sm">{auth.nickname}님, 안녕하세요 🌱</p>
          )}
          <TodayPage />
        </>
      ) : (
        <LoginScreen />
      )}
    </main>
  )
}
