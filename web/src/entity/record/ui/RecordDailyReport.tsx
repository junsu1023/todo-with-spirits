import { useQuery } from '@tanstack/react-query'
import { Card } from '@/shared/ui/card'
import { getTodayRecord } from '../api/query'
import { DailyRecordSkeleton } from './DailyRecordSkeleton'
import { DailyReportCard } from './DailyReportCard'
import { TodayRewardCard } from './TodayRewardCard'

interface RecordDailyReportProps {
	spiritName?: string
	spiritImageUrl?: string
}

export function RecordDailyReport({
	spiritName,
	spiritImageUrl,
}: RecordDailyReportProps) {
	const { data, isPending } = useQuery({
		queryKey: ['record', 'today'],
		queryFn: getTodayRecord,
	})

	if (isPending) return <DailyRecordSkeleton />

	if (data?.result !== 'success') {
		return (
			<Card className="items-center p-10 text-sm text-gray-400">
				데일리 리포트를 불러오지 못했어요.
			</Card>
		)
	}

	const record = data.detail

	return (
		<div className="grid grid-cols-1 gap-6 lg:grid-cols-[3fr_2fr]">
			<DailyReportCard
				record={record}
				spiritName={spiritName}
				spiritImageUrl={spiritImageUrl}
			/>
			<TodayRewardCard rewards={record.todayRewards ?? []} />
		</div>
	)
}
