import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { getWeeklyRecord } from '../api/query'
import { getWeekRange, toDateString } from '../lib/date'
import { RecordErrorCard, RecordReportSkeleton } from './RecordStatusCard'
import { WeeklyAnalysisCard } from './WeeklyAnalysisCard'
import { WeeklyReportCard } from './WeeklyReportCard'

export function RecordWeeklyReport({ date }: { date: Date }) {
	const dateParam = toDateString(date)

	const { data, isPending } = useQuery({
		queryKey: ['record', 'weekly', dateParam],
		queryFn: () => getWeeklyRecord({ date: dateParam }),
		// 주 이동 시 이전 데이터를 유지해 스켈레톤 깜빡임 방지
		placeholderData: keepPreviousData,
	})

	if (isPending) return <RecordReportSkeleton />

	if (data?.result !== 'success') {
		return <RecordErrorCard message="주간 리포트를 불러오지 못했어요." />
	}

	return (
		<div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
			<WeeklyReportCard
				record={data.detail}
				// android와 동일: 주 시작일 기준 월
				month={getWeekRange(date).start.getMonth() + 1}
			/>
			<WeeklyAnalysisCard record={data.detail} />
		</div>
	)
}
