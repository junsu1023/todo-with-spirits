import { storage } from '#imports'

// 웹에서 가져온 로그인 정보. refresh token은 쓰지 않으므로 저장하지 않는다 (docs/spec.md 7번 (B))
export interface ExtensionAuth {
  accessToken: string
  userId: number | null
  nickname: string | null
}

// chrome.storage.local에 저장 → 사이드패널, background, content script 모두 접근 가능
export const authStorage = storage.defineItem<ExtensionAuth | null>(
  'local:auth',
  { fallback: null },
)
