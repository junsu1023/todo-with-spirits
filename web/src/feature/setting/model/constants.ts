import { Info, User } from 'lucide-react'

export const SETTING_ITEMS = [
	{
		icon: User,
		label: '계정 설정',
		description: '프로필 정보, 회원 탈퇴',
	},
	//todo: 상황에 맞게 활성화 예정
	// {
	// 	icon: Bell,
	// 	label: '알림 설정',
	// 	description: '리마인드, 정기알림, 야간푸시, 이벤트 수신',
	// },
	// {
	// 	icon: Monitor,
	// 	label: '디스플레이 설정',
	// 	description: '다크모드, 플랜 표시, 테마, 언어',
	// },
	// {
	// 	icon: Cloud,
	// 	label: '데이터 설정',
	// 	description: '백업, 동기화, 데이터 내보내기',
	// },
	{
		icon: Info,
		label: '고객 지원',
		description: '공지사항, 이용안내, FAQ, 약관 및 정책',
	},
]
