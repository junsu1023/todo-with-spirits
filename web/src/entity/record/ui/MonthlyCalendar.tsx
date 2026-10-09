import sampleSpiritImage from '@/shared/assets/sample-spirit.png'
import { toDateString } from '../lib/date'
import type { MonthlyDailyHeatmap } from '../model/type'

const WEEKDAYS = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']

interface DayCell {
	day: number
	date: string
	hasSchedule: boolean
	hasRoutine: boolean
	/** android와 동일: 그날 일정/루틴을 모두 완료했을 때만 정령 도장 */
	hasStamp: boolean
}

const toDayCell = (
	year: number,
	month: number,
	day: number,
	heatmap?: MonthlyDailyHeatmap,
): DayCell => {
	const scheduleTotal = heatmap?.scheduleTotalCount ?? 0
	const routineTotal = heatmap?.routineTotalCount ?? 0
	const total = scheduleTotal + routineTotal
	const completed =
		(heatmap?.scheduleCompletedCount ?? 0) + (heatmap?.routineCompletedCount ?? 0)

	return {
		day,
		date: toDateString(new Date(year, month - 1, day)),
		hasSchedule: scheduleTotal > 0,
		hasRoutine: routineTotal > 0,
		hasStamp: total > 0 && completed === total,
	}
}

interface MonthlyCalendarProps {
	year: number
	month: number
	heatmaps: MonthlyDailyHeatmap[]
	spiritImageUrl?: string
}

export function MonthlyCalendar({
	year,
	month,
	heatmaps,
	spiritImageUrl,
}: MonthlyCalendarProps) {
	const heatmapByDate = new Map(heatmaps.map((h) => [h.date, h]))
	const firstWeekday = new Date(year, month - 1, 1).getDay()
	const daysInMonth = new Date(year, month, 0).getDate()
	const today = toDateString(new Date())

	const cells = Array.from({ length: daysInMonth }, (_, i) => {
		const date = toDateString(new Date(year, month - 1, i + 1))
		return toDayCell(year, month, i + 1, heatmapByDate.get(date))
	})

	return (
		<div className="grid grid-cols-7 gap-y-2">
			{WEEKDAYS.map((d) => (
				<span key={d} className="text-center text-xs font-medium text-gray-400">
					{d}
				</span>
			))}
			{Array.from({ length: firstWeekday }, (_, i) => (
				// biome-ignore lint/suspicious/noArrayIndexKey: 앞쪽 빈 칸
				<span key={`empty-${i}`} />
			))}
			{cells.map((cell) => (
				<div key={cell.date} className="flex flex-col items-center gap-1">
					<div className="flex h-1.5 gap-0.5">
						{cell.hasSchedule && <span className="size-1.5 rounded-full bg-sky" />}
						{cell.hasRoutine && <span className="size-1.5 rounded-full bg-lime" />}
					</div>
					{cell.hasStamp ? (
						<img
							src={spiritImageUrl || sampleSpiritImage}
							alt={`${cell.day}일 전체 달성`}
							className="size-9 rounded-full bg-lime/30 object-contain"
							onError={(e) => {
								e.currentTarget.src = sampleSpiritImage
							}}
						/>
					) : (
						<span
							className={`flex size-9 items-center justify-center rounded-lg text-sm ${
								cell.date === today
									? 'font-semibold text-brand ring-2 ring-brand'
									: 'text-gray-600'
							}`}
						>
							{cell.day}
						</span>
					)}
				</div>
			))}
		</div>
	)
}
