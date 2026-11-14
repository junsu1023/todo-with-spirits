import {
  authStorage,
  type ExtensionAuth,
} from '@/feature/auth/model/authStorage'

// 웹의 zustand persist 키 (web/src/feature/auth/model/authStore.ts)
const WEB_AUTH_KEY = 'auth'
const POLL_INTERVAL_MS = 1000

interface WebPersistedAuth {
  state?: {
    accessToken?: string | null
    userId?: number | null
    nickname?: string | null
  }
}

/**
 * 웹(vercel)에서 실행되어 localStorage의 로그인 정보를 익스텐션 저장소로 복사한다.
 * 웹 코드는 수정하지 않는다. 같은 탭 안의 localStorage 변경은 storage 이벤트가 오지 않아서 주기적으로 확인한다.
 */
export default defineContentScript({
  matches: ['https://todo-with-spirits.vercel.app/*'],
  main(ctx) {
    let lastRaw: string | null | undefined

    const sync = async () => {
      const raw = localStorage.getItem(WEB_AUTH_KEY)
      if (raw === lastRaw) return
      lastRaw = raw

      const next = parseWebAuth(raw)
      const current = await authStorage.getValue()
      if (current?.accessToken === next?.accessToken) return

      // 웹이 로그아웃 상태면 익스텐션도 로그아웃
      await authStorage.setValue(next)
    }

    sync()
    ctx.setInterval(sync, POLL_INTERVAL_MS)
  },
})

function parseWebAuth(raw: string | null): ExtensionAuth | null {
  if (!raw) return null
  try {
    const { state } = JSON.parse(raw) as WebPersistedAuth
    if (!state?.accessToken) return null
    return {
      accessToken: state.accessToken,
      userId: state.userId ?? null,
      nickname: state.nickname ?? null,
    }
  } catch {
    return null
  }
}
