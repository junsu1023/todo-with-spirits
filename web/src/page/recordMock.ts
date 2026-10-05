// todo: Record API 연동 전까지 사용하는 레이아웃 확인용 mock 데이터

export type RecordCategory = 'WORK' | 'RELATIONSHIP' | 'HOBBY' | 'HEALTH'

export interface CategoryCount {
	category: RecordCategory
	count: number
}

export interface BarDatum {
	label: string
	subLabel?: string
	value: number
	todo?: string
	routine?: string
}

export type WeekRecordStatus = 'success' | 'fail' | 'none'

export const DAILY_MOCK = {
	headline: '다 잘해 진짜!',
	message: '루미랑 남은 4개도 끝내볼까요?',
	goalDone: 6,
	goalTotal: 10,
	todo: { done: 2, total: 2 },
	routine: { done: 2, total: 2 },
	retryGoalCount: 5,
	rewards: [
		{ kind: '달성 미션', title: '오늘 플랜 5개 이상 완료', exp: 20 },
		{ kind: '연기 스트릭', title: '미뤘던 목표 2개 완료', exp: 20 },
		{ kind: '히든 미션', title: '정령의 호감도 +10 쌓기', exp: 100 },
		{ kind: '히든 미션', title: '정령의 호감도 +10 쌓기', exp: 100 },
		{ kind: '달성 미션', title: '루틴 3일 연속 완료', exp: 30 },
	],
}

export const WEEKLY_MOCK = {
	headline: '버티면 승리에요',
	message: '이번주도 너무 잘하고 있어요!',
	rangeLabel: '26년 6월 1일 ~ 26년 6월 6일',
	bars: [
		{ label: '30', subLabel: 'SUN', value: 99, todo: '3/3', routine: '2/2' },
		{ label: '31', subLabel: 'MON', value: 99, todo: '2/2', routine: '2/2' },
		{ label: '01', subLabel: 'TUE', value: 0, todo: '0/2', routine: '0/1' },
		{ label: '02', subLabel: 'WED', value: 99, todo: '1/1', routine: '2/2' },
		{ label: '03', subLabel: 'THU', value: 100, todo: '1/3', routine: '1/2' },
		{ label: '04', subLabel: 'FRI', value: 0 },
		{ label: '05', subLabel: 'SAT', value: 0 },
	] satisfies BarDatum[],
	completedCount: 999,
	totalCount: 1000,
	averageRate: 100,
	records: [
		'success',
		'success',
		'fail',
		'success',
		'success',
		'none',
		'none',
	] satisfies WeekRecordStatus[],
	ratio: { todo: 95, routine: 99, postpone: 99 },
	topCategories: [
		{ category: 'WORK', count: 15 },
		{ category: 'RELATIONSHIP', count: 8 },
		{ category: 'HOBBY', count: 5 },
	] satisfies CategoryCount[],
	missedCategory: { category: 'HEALTH', count: 15 } satisfies CategoryCount,
	tip: {
		title: '커리어의 미래를 향해 멋지게 질주한 한 주!',
		description:
			'다음 주는 미뤄진 건강도 챙겨서 일과 삶의 균형을 맞춰볼까요?',
	},
}

export const MONTHLY_MOCK = {
	headline: '26년 초여름의 6월',
	message: '이번 달도 바쁘겠지만 할 수 있어요!',
	year: 2026,
	month: 6,
	// 일자별 To do / 루틴 존재 여부
	dots: {
		1: ['todo', 'routine'],
		2: ['todo'],
		3: ['todo', 'routine'],
		4: ['todo', 'routine'],
		5: ['routine'],
		6: ['todo', 'routine'],
		7: ['todo', 'routine'],
		8: ['todo', 'routine'],
		9: ['todo', 'routine'],
		10: ['todo', 'routine'],
		11: ['todo', 'routine'],
		12: ['todo', 'routine'],
		13: ['todo', 'routine'],
		14: ['todo', 'routine'],
		15: ['todo', 'routine'],
		16: ['todo', 'routine'],
		17: ['todo', 'routine'],
		18: ['todo', 'routine'],
		19: ['todo', 'routine'],
		20: ['todo', 'routine'],
		21: ['todo', 'routine'],
		22: ['todo', 'routine'],
		23: ['todo', 'routine'],
		24: ['todo', 'routine'],
		25: ['todo', 'routine'],
		26: ['todo', 'routine'],
		27: ['todo', 'routine'],
		28: ['todo', 'routine'],
		29: ['todo', 'routine'],
		30: ['todo'],
	} as Record<number, ('todo' | 'routine')[]>,
	// 전부 달성해서 정령 도장이 찍힌 날
	perfectDays: [1, 3, 4],
	today: 6,
	completedCount: 999,
	totalCount: 1000,
	averageRate: 100,
	compareBars: [
		{ label: '1월', value: 99 },
		{ label: '2월', value: 99 },
		{ label: '3월', value: 0 },
		{ label: '4월', value: 99 },
		{ label: '5월', value: 99 },
		{ label: '6월', value: 100 },
		{ label: '7월', value: 0 },
	] satisfies BarDatum[],
	compareMessage: '이번 달은 지난 달 보다 달성률이 98% 높아요!',
	analysis: {
		title: '기분 좋은 몰입의 흔적',
		description:
			'커리어 계발부터 취미 리듬까지 지켜낸 계획적인 정령!\n핵심 루틴들을 확실히 잡아 지켜냈어요.',
		rate: 99,
		badge: '학업/커리어 분야\n전체 상위 4%',
	},
	topCategories: [
		{ category: 'WORK', count: 15 },
		{ category: 'RELATIONSHIP', count: 8 },
		{ category: 'HOBBY', count: 5 },
	] satisfies CategoryCount[],
	missedCategory: { category: 'HEALTH', count: 15 } satisfies CategoryCount,
}
