import { createBrowserRouter } from 'react-router-dom'
import { LoginPage } from '@/page/LoginPage'
import { PlanPage } from '@/page/PlanPage'
import { TodayPage } from '@/page/TodayPage'
import { AppLayout } from '@/shared/layout/AppLayout'
import { ROUTES } from '@/shared/routes'
import { GuestOnlyRoute, ProtectedRoute, RootRedirect } from './route-guards'

export const router = createBrowserRouter([
  {
    path: ROUTES.ROOT,
    element: <RootRedirect />,
  },
  {
    path: ROUTES.LOGIN,
    element: (
      <GuestOnlyRoute>
        <LoginPage />
      </GuestOnlyRoute>
    ),
  },
  {
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        path: ROUTES.TODAY,
        element: <TodayPage />,
      },
      {
        path: ROUTES.PLAN,
        element: <PlanPage />,
      },
    ],
  },
])
