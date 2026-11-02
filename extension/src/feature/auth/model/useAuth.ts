import { useEffect, useState } from 'react'
import { authStorage, type ExtensionAuth } from './authStorage'

// undefined = 아직 저장소를 읽는 중
export function useAuth() {
  const [auth, setAuth] = useState<ExtensionAuth | null | undefined>()

  useEffect(() => {
    authStorage.getValue().then(setAuth)
    // content script가 웹 로그인 정보를 넣거나, 401로 지워지면 바로 반영
    return authStorage.watch(setAuth)
  }, [])

  return auth
}
