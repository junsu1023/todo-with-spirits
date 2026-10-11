import { useQuery } from '@tanstack/react-query'
import { getTodayRecord } from '../api/query'
import { DailyRecordSkeleton } from './DailyRecordSkeleton'
import { DailyReportCard } from './DailyReportCard'
import { RecordErrorCard } from './RecordStatusCard'
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
		return <RecordErrorCard message="데일리 리포트를 불러오지 못했어요." />
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
