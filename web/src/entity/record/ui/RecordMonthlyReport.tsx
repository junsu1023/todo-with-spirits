import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { getMonthlyRecord } from '../api/query'
import { toMonthParam } from '../lib/date'
import { MonthlyAnalysisCard } from './MonthlyAnalysisCard'
import { MonthlyCompareCard } from './MonthlyCompareCard'
import { MonthlyReportCard } from './MonthlyReportCard'
import { RecordErrorCard, RecordReportSkeleton } from './RecordStatusCard'

interface RecordMonthlyReportProps {
	date: Date
	spiritImageUrl?: string
}

export function RecordMonthlyReport({ date, spiritImageUrl }: RecordMonthlyReportProps) {
	const dateParam = toMonthParam(date)

	const { data, isPending } = useQuery({
		queryKey: ['record', 'monthly', dateParam],
		queryFn: () => getMonthlyRecord({ date: dateParam }),
		// 월 이동 시 이전 데이터를 유지해 스켈레톤 깜빡임 방지
		placeholderData: keepPreviousData,
	})

	if (isPending) return <RecordReportSkeleton />

	if (data?.result !== 'success') {
		return <RecordErrorCard message="월간 리포트를 불러오지 못했어요." />
	}

	const record = data.detail

	return (
		<div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
			<MonthlyReportCard record={record} spiritImageUrl={spiritImageUrl} />
			<div className="flex flex-col gap-6">
				<MonthlyCompareCard
					comparisons={record.monthlyComparisons ?? []}
					month={record.month}
				/>
				<MonthlyAnalysisCard record={record} spiritImageUrl={spiritImageUrl} />
			</div>
		</div>
	)
}
