import { Info, Monitor, User } from 'lucide-react'
import { ROUTES } from '@/shared/routes'

export const SETTING_ITEMS = [
	{
		icon: User,
		label: '계정 관리',
		navLabel: '계정',
		href: ROUTES.MYPAGE_SETTING_ACCOUNT,
		description: '프로필 정보, 회원 탈퇴',
	},
	{
		icon: Monitor,
		label: '디스플레이 설정',
		navLabel: '디스플레이',
		href: ROUTES.MYPAGE_SETTING_DISPLAY,
		description: '다크모드, 플랜 표시, 테마, 언어',
	},
	{
		icon: Info,
		label: '고객 지원',
		href: ROUTES.MYPAGE_SUPPORT,
		description: '공지사항, 이용안내, FAQ, 약관 및 정책',
	},
] as const
