/** 오늘 기준 D-day 값을 계산. 과거면 음수, 오늘이면 0, 미래면 양수 */
export function calcDday(dateStr: string): number {
	const today = new Date()
	today.setHours(0, 0, 0, 0)
	const target = new Date(dateStr)
	target.setHours(0, 0, 0, 0)
	return Math.ceil((target.getTime() - today.getTime()) / 86400000)
}

/** dday 숫자 → 표시 문자열. 지남 / D-DAY / D-N */
export function formatDday(dday: number): string {
	if (dday < 0) return '지남'
	if (dday === 0) return 'D-DAY'
	return `D-${dday}`
}

/** 'YYYY-MM-DD' 문자열에 n일을 더한 'YYYY-MM-DD' 반환 */
export function addDays(dateStr: string, days: number): string {
	const [y, m, d] = dateStr.split('-').map(Number)
	const date = new Date(y, m - 1, d + days)
	const yyyy = date.getFullYear()
	const mm = String(date.getMonth() + 1).padStart(2, '0')
	const dd = String(date.getDate()).padStart(2, '0')
	return `${yyyy}-${mm}-${dd}`
}
