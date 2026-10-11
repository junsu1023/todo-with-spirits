import { toDateString } from '../lib/date'
import { WEEKLY_HEADLINE_MOCK } from '../model/mock'
import type { WeeklyDailyChart, WeeklyRecordDetail } from '../model/type'
import { type BarDatum, RecordBarChart } from './RecordBarChart'
import { RecordCard, RecordHeadline } from './RecordCard'
import { RecordStat } from './RecordStat'
import { WeeklyRecordRow } from './WeeklyRecordRow'

// android와 동일: 오늘 이후 날짜는 값 없음('?'), 막대 값은 완료한 플랜 수
const toBarDatum = (chart: WeeklyDailyChart, today: string): BarDatum => ({
	key: chart.date,
	label: chart.date.slice(8),
	subLabel: chart.dayOfWeek,
	value:
		chart.date > today ? null : chart.scheduleCompleted + chart.routineCompleted,
	tooltip: (
		<>
			<p className="text-[#48CAD9]">
				To do {chart.scheduleCompleted} / {chart.scheduleTotal}
			</p>
			<p className="text-[#8FC21F]">
				루틴 {chart.routineCompleted} / {chart.routineTotal}
			</p>
		</>
	),
})

interface WeeklyReportCardProps {
	record: WeeklyRecordDetail
	month: number
}

export function WeeklyReportCard({ record, month }: WeeklyReportCardProps) {
	const charts = record.dailyCharts ?? []
	const today = toDateString(new Date())

	return (
		<RecordCard title={`${month}월 ${record.week}주 리포트`}>
			{/* todo: 헤드라인은 API에 없어 임시 문구 사용 중 (android도 하드코딩) */}
			<RecordHeadline title={WEEKLY_HEADLINE_MOCK} message={record.message} />
			<RecordBarChart data={charts.map((chart) => toBarDatum(chart, today))} />
			<div className="flex gap-3">
				<RecordStat
					label="이번 주 달성 플랜"
					value={`${record.completedTaskCount}개`}
					suffix={`/ ${record.totalTaskCount}`}
				/>
				{/* todo: averageCompletionRate를 0~100 값으로 가정 (android는 값 그대로 % 표시) */}
				<RecordStat
					label="평균 달성률"
					value={`${Math.round(record.averageCompletionRate)}%`}
				/>
			</div>
			<WeeklyRecordRow charts={charts} />
		</RecordCard>
	)
}
