import { useQuery } from '@tanstack/react-query'
import { ChevronDown, ChevronUp } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getTaskCalendar } from '@/entity/task'
import { ROUTES } from '@/shared/routes'

const WEEKDAYS = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']
const WEEKDAYS_KO = [
	'일요일',
	'월요일',
	'화요일',
	'수요일',
	'목요일',
	'금요일',
	'토요일',
]

function getWeekDates(base: Date): Date[] {
	const sunday = new Date(base)
	sunday.setDate(base.getDate() - base.getDay())
	return Array.from({ length: 7 }, (_, i) => {
		const d = new Date(sunday)
		d.setDate(sunday.getDate() + i)
		return d
	})
}

function toDateString(date: Date) {
	const y = date.getFullYear()
	const m = String(date.getMonth() + 1).padStart(2, '0')
	const d = String(date.getDate()).padStart(2, '0')
	return `${y}-${m}-${d}`
}

function toKey(date: Date) {
	return `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`
}

function formatHeader(date: Date) {
	const y = date.getFullYear()
	const m = String(date.getMonth() + 1).padStart(2, '0')
	const d = String(date.getDate()).padStart(2, '0')
	return `${y}. ${m}. ${d} (${WEEKDAYS_KO[date.getDay()]})`
}

interface TodayDateHeaderProps {
	selectedDate: Date
	onDateChange: (date: Date) => void
}

export function TodayDateHeader({
	selectedDate,
	onDateChange,
}: TodayDateHeaderProps) {
	const [isExpanded, setIsExpanded] = useState(true)

	const today = new Date()
	const todayKey = toKey(today)
	const selectedKey = toKey(selectedDate)
	const weekDates = getWeekDates(selectedDate)

	const from = toDateString(weekDates[0])
	const to = toDateString(weekDates[6])

	const { data: calendarData } = useQuery({
		queryKey: ['task', 'calendar', from, to],
		queryFn: () => getTaskCalendar({ from, to }),
		enabled: isExpanded,
	})

	const items =
		calendarData?.result === 'success' ? calendarData.detail.items : []

	const dotMap = items.reduce<
		Record<string, { hasTodo: boolean; hasRoutine: boolean }>
	>((acc, item) => {
		const dateStr = item.occurrenceDate
		if (!dateStr) return acc
		if (!acc[dateStr]) acc[dateStr] = { hasTodo: false, hasRoutine: false }
		if (item.taskType === 'ROUTINE') {
			acc[dateStr].hasRoutine = true
		} else {
			acc[dateStr].hasTodo = true
		}
		return acc
	}, {})

	const navigate = useNavigate()

	return (
		<div className="flex flex-col gap-3">
			{/* 헤더 행 */}
			<div className="flex items-center justify-between">
				<button
					type="button"
					onClick={() => setIsExpanded((e) => !e)}
					className="flex items-center gap-2 hover:opacity-70"
				>
					<span className="text-xl font-bold">
						{formatHeader(selectedDate)}
					</span>
					{isExpanded ? (
						<ChevronUp size={20} className="text-gray-500" />
					) : (
						<ChevronDown size={20} className="text-gray-500" />
					)}
				</button>
				<button
					type="button"
					className="text-sm text-gray-400 hover:text-[#B286FD] transition-colors"
					onClick={() => navigate(ROUTES.PLAN)}
				>
					플랜 전체 보기
				</button>
			</div>

			{/* 주간 캘린더 */}
			{isExpanded && (
				<div className="flex justify-between px-2">
					{weekDates.map((date) => {
						const key = toKey(date)
						const dateStr = toDateString(date)
						const isSelected = key === selectedKey
						const isToday = key === todayKey
						const dayDots = dotMap[dateStr]

						return (
							<button
								key={key}
								type="button"
								onClick={() => onDateChange(new Date(date))}
								className="flex flex-col items-center gap-1.5"
							>
								<span className="text-xs font-medium text-gray-400">
									{WEEKDAYS[date.getDay()]}
								</span>
								<div
									className={`flex h-10 w-9 items-center justify-center rounded-lg text-sm font-semibold transition-colors ${
										isSelected
											? 'bg-[#B286FD] text-white'
											: isToday
												? 'border-2 border-[#B286FD] text-[#B286FD]'
												: 'text-gray-700 hover:bg-gray-100'
									}`}
								>
									{date.getDate()}
								</div>
								<div className="flex gap-0.5">
									{dayDots?.hasTodo && (
										<div
											className="h-1.5 w-1.5 rounded-full"
											style={{ backgroundColor: 'var(--sky)' }}
										/>
									)}
									{dayDots?.hasRoutine && (
										<div
											className="h-1.5 w-1.5 rounded-full"
											style={{ backgroundColor: 'var(--lime)' }}
										/>
									)}
								</div>
							</button>
						)
					})}
				</div>
			)}
		</div>
	)
}
