import type { ReactElement, ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@/feature/auth/model/authStore'
import { ROUTES } from '@/shared/routes'

function useIsAuthenticated() {
  const accessToken = useAuthStore((state) => state.accessToken)
  const refreshToken = useAuthStore((state) => state.refreshToken)
  return accessToken !== null && refreshToken !== null
}

export function RootRedirect(): ReactElement {
  const isAuthenticated = useIsAuthenticated()
  return isAuthenticated ? (
    <Navigate to={ROUTES.TODAY} replace />
  ) : (
    <Navigate to={ROUTES.LOGIN} replace />
  )
}

interface GuestOnlyRouteProps {
  children: ReactNode
}

export function GuestOnlyRoute({
  children,
}: GuestOnlyRouteProps): ReactElement {
  const isAuthenticated = useIsAuthenticated()
  if (isAuthenticated) return <Navigate to={ROUTES.TODAY} replace />
  return <>{children}</>
}

interface ProtectedRouteProps {
  children: ReactNode
  redirectTo?: string
}

export function ProtectedRoute({
  children,
  redirectTo = ROUTES.LOGIN,
}: ProtectedRouteProps): ReactElement {
  const isAuthenticated = useIsAuthenticated()
  if (!isAuthenticated) return <Navigate to={redirectTo} replace />
  return <>{children}</>
}
