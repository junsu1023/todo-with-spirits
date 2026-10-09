import { Star, X } from 'lucide-react'
import type { WeeklyDailyChart } from '../model/type'

type DayStatus = 'empty' | 'success' | 'failed'

// android와 동일: icon 필드 대신 완료/전체 수로 판단
// todo: 서버 icon(SUCCESS / FAILED / EMPTY) 기준으로 바꿀지 확인 필요
const toDayStatus = (chart: WeeklyDailyChart): DayStatus => {
	const total = chart.scheduleTotal + chart.routineTotal
	const completed = chart.scheduleCompleted + chart.routineCompleted
	if (total === 0) return 'empty'
	return completed === total ? 'success' : 'failed'
}

const STATUS_STYLE: Record<DayStatus, string> = {
	success: 'bg-brand-light',
	failed: 'border-2 border-gray-200 bg-white',
	empty: 'border-2 border-gray-200 bg-white',
}

export function WeeklyRecordRow({ charts }: { charts: WeeklyDailyChart[] }) {
	return (
		<div className="flex flex-col gap-3 rounded-xl bg-gray-50 px-4 py-4">
			<span className="text-xs text-gray-400">주간 기록</span>
			<div className="flex justify-between">
				{charts.map((chart, i) => {
					const status = toDayStatus(chart)
					return (
						<div key={chart.date} className="flex flex-col items-center gap-1">
							<div
								className={`flex size-10 items-center justify-center rounded-full ${STATUS_STYLE[status]}`}
							>
								{status === 'success' && (
									<Star size={18} className="fill-white text-white" />
								)}
								{status === 'failed' && <X size={18} className="text-gray-300" />}
							</div>
							<span className="text-[10px] text-gray-400">{i + 1}일차</span>
						</div>
					)
				})}
			</div>
		</div>
	)
}
