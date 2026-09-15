import { createBrowserRouter } from 'react-router-dom'
import { AccountPage } from '@/page/AccountPage'
import { CustomerSupportPage } from '@/page/CustomerSupportPage'
import { DisplayPage } from '@/page/DisplayPage'
import ForestPage from '@/page/ForestPage'
import { JoinPage } from '@/page/JoinPage'
import { KakaoCallbackPage } from '@/page/KakaoCallbackPage'
import { LoginPage } from '@/page/LoginPage'
import { MePage } from '@/page/MePage'
import { PlanPage } from '@/page/PlanPage'
import RecordPage from '@/page/RecordPage'
import { SettingPage } from '@/page/SettingPage'
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
				path: ROUTES.MYPAGE_SETTING,
				element: <SettingPage />,
				children: [
					{
						path: ROUTES.MYPAGE_SETTING_ACCOUNT,
						element: <AccountPage />,
					},
					{
						path: ROUTES.MYPAGE_SETTING_DISPLAY,
						element: <DisplayPage />,
					},
					{
						path: ROUTES.MYPAGE_SUPPORT,
						element: <CustomerSupportPage />,
					},
				],
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
