const pad = (n: number) => String(n).padStart(2, '0')

/** Date → 'YYYY-MM-DD' */
export const toDateString = (date: Date) =>
	`${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`

/** Date → '26년 6월 1일' */
export const formatShortDate = (date: Date) =>
	`${String(date.getFullYear()).slice(2)}년 ${date.getMonth() + 1}월 ${date.getDate()}일`

// todo: 주 시작 요일을 일요일로 가정 (API 문서의 dailyCharts가 SUN부터 시작), 서버 기준 확인 필요
export const getWeekRange = (date: Date) => {
	const start = new Date(date.getFullYear(), date.getMonth(), date.getDate() - date.getDay())
	const end = new Date(start.getFullYear(), start.getMonth(), start.getDate() + 6)
	return { start, end }
}

export const shiftWeek = (date: Date, amount: number) =>
	new Date(date.getFullYear(), date.getMonth(), date.getDate() + amount * 7)

export const shiftMonth = (date: Date, amount: number) =>
	new Date(date.getFullYear(), date.getMonth() + amount, 1)

/** 해당 월 1일의 'YYYY-MM-DD' (월간 API 조회용) */
export const toMonthParam = (date: Date) =>
	`${date.getFullYear()}-${pad(date.getMonth() + 1)}-01`
