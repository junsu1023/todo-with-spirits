import { useQuery } from '@tanstack/react-query'
import { type ReactNode, useState } from 'react'
import {
	formatShortDate,
	getWeekRange,
	RecordDailyReport,
	RecordMonthlyReport,
	RecordPeriodNav,
	RecordWeeklyReport,
	shiftMonth,
	shiftWeek,
} from '@/entity/record'
import { getCurrentSpirit } from '@/entity/spirit'

type RecordTab = '일간' | '주간' | '월간'

const TABS: RecordTab[] = ['일간', '주간', '월간']

const WEEKDAYS_KO = ['일', '월', '화', '수', '목', '금', '토']

const formatDayLabel = (date: Date) =>
	`${date.getFullYear()}. ${String(date.getMonth() + 1).padStart(2, '0')}. ${String(date.getDate()).padStart(2, '0')} (${WEEKDAYS_KO[date.getDay()]})`

const formatWeekLabel = (date: Date) => {
	const { start, end } = getWeekRange(date)
	return `${formatShortDate(start)} ~ ${formatShortDate(end)}`
}

const formatMonthLabel = (date: Date) =>
	`${date.getFullYear()}년 ${date.getMonth() + 1}월`

export function RecordPage() {
	const [tab, setTab] = useState<RecordTab>('일간')
	const [weekDate, setWeekDate] = useState(() => new Date())
	const [monthDate, setMonthDate] = useState(() => new Date())

	// record API에 정령 정보가 없어 대표 정령 조회 사용 (TodaySpiritCard와 캐시 공유)
	const { data: spiritData } = useQuery({
		queryKey: ['spirit', 'current'],
		queryFn: getCurrentSpirit,
	})
	const spirit = spiritData?.result === 'success' ? spiritData.detail : null

	const periodNav = {
		일간: (
			<RecordPeriodNav
				label={formatDayLabel(new Date())}
				// todo: today API에 date 파라미터가 없어 날짜 이동 불가 → API 지원 시 주석 해제
				// onPrev={() => setDate(addDays(date, -1))}
				// onNext={() => setDate(addDays(date, 1))}
			/>
		),
		주간: (
			<RecordPeriodNav
				label={formatWeekLabel(weekDate)}
				onPrev={() => setWeekDate((d) => shiftWeek(d, -1))}
				onNext={() => setWeekDate((d) => shiftWeek(d, 1))}
			/>
		),
		월간: (
			<RecordPeriodNav
				label={formatMonthLabel(monthDate)}
				onPrev={() => setMonthDate((d) => shiftMonth(d, -1))}
				onNext={() => setMonthDate((d) => shiftMonth(d, 1))}
			/>
		),
	} satisfies Record<RecordTab, ReactNode>

	const report = {
		일간: (
			<RecordDailyReport
				spiritName={spirit?.spiritName}
				spiritImageUrl={spirit?.imageUrl}
			/>
		),
		주간: <RecordWeeklyReport date={weekDate} />,
		월간: <RecordMonthlyReport date={monthDate} spiritImageUrl={spirit?.imageUrl} />,
	} satisfies Record<RecordTab, ReactNode>

	return (
		<main className="flex h-screen flex-col gap-5 overflow-y-auto p-4 md:p-6">
			{/* 헤더: 탭 + 기간 이동 */}
			<div className="flex flex-col-reverse gap-2 border-b border-gray-100 sm:flex-row sm:items-end sm:justify-between">
				<div className="flex">
					{TABS.map((t) => (
						<button
							key={t}
							type="button"
							onClick={() => setTab(t)}
							className={`px-5 pb-2.5 text-base font-medium transition-colors ${
								tab === t
									? 'border-b-2 border-[#B286FD] text-[#B286FD]'
									: 'text-gray-400 hover:text-gray-600'
							}`}
						>
							{t}
						</button>
					))}
				</div>
				{periodNav[tab]}
			</div>

			<div className="min-h-0 flex-1">{report[tab]}</div>
		</main>
	)
}

export default RecordPage
