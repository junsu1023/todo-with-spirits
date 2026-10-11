import type { MonthlyRecordDetail } from '../model/type'
import { MonthlyCalendar } from './MonthlyCalendar'
import { RecordCard, RecordHeadline } from './RecordCard'
import { RecordStat } from './RecordStat'

interface MonthlyReportCardProps {
	record: MonthlyRecordDetail
	spiritImageUrl?: string
}

export function MonthlyReportCard({ record, spiritImageUrl }: MonthlyReportCardProps) {
	const { year, month } = record

	return (
		<RecordCard title={`${month}월 리포트`}>
			<RecordHeadline
				// todo: 디자인의 '26년 초여름의 6월' 같은 타이틀은 API에 없음 → 년/월로 임시 표시
				title={`${String(year).slice(2)}년 ${month}월`}
				message={record.message}
			/>
			<MonthlyCalendar
				year={year}
				month={month}
				heatmaps={record.dailyHeatmaps ?? []}
				spiritImageUrl={spiritImageUrl}
			/>
			<div className="flex gap-3">
				<RecordStat
					label="이번 달 달성"
					value={`${record.completedTaskCount}개`}
					suffix={`/ ${record.totalTaskCount}`}
				/>
				{/* todo: averageCompletionRate를 0~100 값으로 가정 (android는 반올림해서 % 표시) */}
				<RecordStat
					label="평균 달성률"
					value={`${Math.round(record.averageCompletionRate)}%`}
				/>
			</div>
		</RecordCard>
	)
}
