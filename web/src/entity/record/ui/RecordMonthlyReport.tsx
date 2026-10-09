import { Sparkles } from 'lucide-react'
import sampleSpiritImage from '@/shared/assets/sample-spirit.png'
import { MONTHLY_MOCK } from '../model/mock'
import { CategoryRanking, MissedCategory } from './CategoryRanking'
import { RecordBarChart } from './RecordBarChart'
import { RecordCard, RecordHeadline } from './RecordCard'
import { RecordStat } from './RecordStat'

const WEEKDAYS = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']

function MonthCalendar() {
	const { year, month, dots, perfectDays, today } = MONTHLY_MOCK
	const firstWeekday = new Date(year, month - 1, 1).getDay()
	const daysInMonth = new Date(year, month, 0).getDate()

	const cells: (number | null)[] = [
		...Array.from({ length: firstWeekday }, () => null),
		...Array.from({ length: daysInMonth }, (_, i) => i + 1),
	]

	return (
		<div className="grid grid-cols-7 gap-y-2">
			{WEEKDAYS.map((d) => (
				<span key={d} className="text-center text-xs font-medium text-gray-400">
					{d}
				</span>
			))}
			{cells.map((day, i) => {
				if (day === null) {
					// biome-ignore lint/suspicious/noArrayIndexKey: 빈 칸
					return <span key={`empty-${i}`} />
				}
				const isPerfect = perfectDays.includes(day)
				const isToday = day === today
				const dayDots = dots[day] ?? []

				return (
					<div key={day} className="flex flex-col items-center gap-1">
						<div className="flex h-1.5 gap-0.5">
							{dayDots.map((type) => (
								<span
									key={type}
									className={`size-1.5 rounded-full ${type === 'todo' ? 'bg-sky' : 'bg-lime'}`}
								/>
							))}
						</div>
						{isPerfect ? (
							<img
								src={sampleSpiritImage}
								alt={`${day}일 전체 달성`}
								className="size-9 rounded-full bg-lime/30 object-contain"
							/>
						) : (
							<span
								className={`flex size-9 items-center justify-center rounded-lg text-sm ${
									isToday
										? 'font-semibold text-brand ring-2 ring-brand'
										: 'text-gray-600'
								}`}
							>
								{day}
							</span>
						)}
					</div>
				)
			})}
		</div>
	)
}

function MonthlyAnalysisHero() {
	const { analysis } = MONTHLY_MOCK

	return (
		<div className="flex flex-col items-center gap-6 rounded-xl bg-brand-muted p-5 sm:flex-row">
			<div className="relative shrink-0">
				<img src={sampleSpiritImage} alt="" className="size-32 object-contain" />
				<Sparkles
					size={20}
					className="absolute -top-1 left-0 fill-[#FFE766] text-[#F5D000]"
				/>
				<span className="absolute -right-6 -top-2 flex size-16 flex-col items-center justify-center rounded-full bg-lime text-center leading-tight">
					<span className="text-[10px] text-gray-600">달성률</span>
					<span className="text-base font-bold text-gray-800">
						{analysis.rate}%
					</span>
				</span>
			</div>
			<div className="flex flex-col gap-2">
				<span className="w-fit whitespace-pre-line rounded-full bg-white px-3 py-1 text-[11px] text-brand">
					{analysis.badge.replace('\n', ' · ')}
				</span>
				<span className="text-lg font-bold text-gray-800">{analysis.title}</span>
				<span className="whitespace-pre-line text-xs text-gray-500">
					{analysis.description}
				</span>
			</div>
		</div>
	)
}

export function RecordMonthlyReport() {
	const m = MONTHLY_MOCK

	return (
		<div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
			<RecordCard title={`${m.month}월 리포트`}>
				<RecordHeadline title={m.headline} message={m.message} />
				<MonthCalendar />
				<div className="flex gap-3">
					<RecordStat
						label="이번 달 달성"
						value={`${m.completedCount}개`}
						suffix={`/ ${m.totalCount}`}
					/>
					<RecordStat label="평균 달성률" value={`${m.averageRate}%`} />
				</div>
			</RecordCard>

			<div className="flex flex-col gap-6">
				<RecordCard title="월간 비교">
					<RecordBarChart data={m.compareBars} height={140} />
					<p className="rounded-xl border border-brand-light bg-brand-muted px-4 py-2.5 text-center text-sm text-brand">
						{m.compareMessage}
					</p>
				</RecordCard>

				<RecordCard title="월간 분석">
					<MonthlyAnalysisHero />
					<div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
						<CategoryRanking title="월간 실천 Top 3" items={m.topCategories} />
						<MissedCategory item={m.missedCategory} />
					</div>
				</RecordCard>
			</div>
		</div>
	)
}
