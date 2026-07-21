import type { ReactElement, ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { ROUTES } from '@/shared/routes'

// TODO: auth store 연결 후 실제 인증 상태로 교체
// const status = useAuthStore((state) => state.status)

export function RootRedirect(): ReactElement {
  // TODO: 인증 상태에 따라 분기
  // if (status === 'authenticated') return <Navigate to={ROUTES.TODAY} replace />
  return <Navigate to={ROUTES.LOGIN} replace />
}

interface GuestOnlyRouteProps {
  children: ReactNode
}

export function GuestOnlyRoute({
  children,
}: GuestOnlyRouteProps): ReactElement {
  // TODO: 로그인 상태면 TODAY로 리다이렉트
  // if (status === 'authenticated') return <Navigate to={ROUTES.TODAY} replace />
  return <>{children}</>
}

interface ProtectedRouteProps {
  children: ReactNode
  redirectTo?: string
}

export function ProtectedRoute({
  children,
  redirectTo: _redirectTo = ROUTES.LOGIN,
}: ProtectedRouteProps): ReactElement {
  // TODO: 미인증 상태면 로그인으로 리다이렉트
  // if (status === 'unauthenticated') return <Navigate to={_redirectTo} replace />
  return <>{children}</>
}
