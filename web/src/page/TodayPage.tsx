import { useState } from 'react'
import { TodaySpiritCard } from '@/entity/spirit'
import { TodayAchievementCard } from './TodayAchievementCard'
import { TodayDateHeader } from './TodayDateHeader'
import { TodayTodoCard } from './TodayTodoCard'

function toDateString(date: Date) {
	const y = date.getFullYear()
	const m = String(date.getMonth() + 1).padStart(2, '0')
	const d = String(date.getDate()).padStart(2, '0')
	return `${y}-${m}-${d}`
}

export function TodayPage() {
	const [selectedDate, setSelectedDate] = useState(() => new Date())
	const dateStr = toDateString(selectedDate)

	return (
		<main className="flex h-screen flex-col overflow-hidden p-6">
			<div className="flex min-h-0 flex-1 gap-6">
				{/* 좌: 정령 + 달성률 */}
				<div className="flex w-[260px] shrink-0 flex-col gap-4">
					<TodaySpiritCard />
					<TodayAchievementCard percentage={100} />
				</div>

				{/* 우: 날짜 헤더 + 할 일 */}
				<div className="flex min-h-0 flex-1 flex-col gap-4">
					<TodayDateHeader
						selectedDate={selectedDate}
						onDateChange={setSelectedDate}
					/>
					<TodayTodoCard key={dateStr} selectedDate={selectedDate} />
				</div>
			</div>
		</main>
	)
}
