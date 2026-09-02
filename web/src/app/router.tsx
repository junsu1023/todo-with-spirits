import { createBrowserRouter } from 'react-router-dom'
import ForestPage from '@/page/ForestPage'
import { JoinPage } from '@/page/JoinPage'
import { KakaoCallbackPage } from '@/page/KakaoCallbackPage'
import { LoginPage } from '@/page/LoginPage'
import { MePage } from '@/page/MePage'
import { PlanPage } from '@/page/PlanPage'
import RecordPage from '@/page/RecordPage'
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
		path: '/join',
		element: (
			<GuestOnlyRoute>
				<JoinPage />
			</GuestOnlyRoute>
		),
	},
	{
		path: '/oauth/kakao',
		element: <KakaoCallbackPage />,
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
			{
				path: ROUTES.MYPAGE,
				element: <MePage />,
			},
			{
				path: ROUTES.FOREST,
				element: <ForestPage />,
			},
			{
				path: ROUTES.RECORD,
				element: <RecordPage />,
			},
		],
	},
])
