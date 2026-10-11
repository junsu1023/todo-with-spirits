import { toPercent } from '../lib/progress'
import type { MonthlyComparison } from '../model/type'
import { type BarDatum, RecordBarChart } from './RecordBarChart'
import { RecordCard } from './RecordCard'

const MONTHS = Array.from({ length: 12 }, (_, i) => i + 1)

// todo: completedRate를 0~1 비율로 가정 (android 표시 기준)
const findRate = (comparisons: MonthlyComparison[], month: number) =>
	comparisons.find((c) => c.month === month)?.completedRate

// android와 동일: 조회 월 이후는 값 없음('?')
const toBarDatum = (
	comparisons: MonthlyComparison[],
	month: number,
	currentMonth: number,
): BarDatum => {
	const rate = findRate(comparisons, month)
	return {
		key: String(month),
		label: `${month}월`,
		value: month > currentMonth || rate === undefined ? null : toPercent(rate),
	}
}

// todo: 1월은 전년도 12월 값이 응답에 없어 0으로 비교됨 (android 동일)
const getCompareMessage = (comparisons: MonthlyComparison[], month: number) => {
	const current = findRate(comparisons, month) ?? 0
	const previous = findRate(comparisons, month - 1) ?? 0
	const diff = toPercent(Math.abs(current - previous))

	if (current === previous) return '이번 달은 지난 달과 달성률이 같아요.'
	return current > previous
		? `이번 달은 지난 달 보다 달성률이 ${diff}% 높아요!`
		: `이번 달은 지난 달 보다 달성률이 ${diff}% 낮아요. 다시 힘내봐요!`
}

interface MonthlyCompareCardProps {
	comparisons: MonthlyComparison[]
	month: number
}

export function MonthlyCompareCard({ comparisons, month }: MonthlyCompareCardProps) {
	return (
		<RecordCard title="월간 비교">
			<RecordBarChart
				data={MONTHS.map((m) => toBarDatum(comparisons, m, month))}
				height={140}
			/>
			<p className="rounded-xl border border-brand-light bg-brand-muted px-4 py-2.5 text-center text-sm text-brand">
				{getCompareMessage(comparisons, month)}
			</p>
		</RecordCard>
	)
}
