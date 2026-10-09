import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import {
	RecordDailyReport,
	RecordMonthlyReport,
	RecordPeriodNav,
	RecordWeeklyReport,
} from '@/entity/record'
import { getCurrentSpirit } from '@/entity/spirit'

type RecordTab = '일간' | '주간' | '월간'

const TABS: RecordTab[] = ['일간', '주간', '월간']

const WEEKDAYS_KO = ['일', '월', '화', '수', '목', '금', '토']

const formatDayLabel = (date: Date) =>
	`${date.getFullYear()}. ${String(date.getMonth() + 1).padStart(2, '0')}. ${String(date.getDate()).padStart(2, '0')} (${WEEKDAYS_KO[date.getDay()]})`

const noop = () => {}

function PeriodNavByTab({ tab }: { tab: RecordTab }) {
	switch (tab) {
		case '일간':
			return (
				<RecordPeriodNav
					label={formatDayLabel(new Date())}
					// todo: today API에 date 파라미터가 없어 날짜 이동 불가 → API 지원 시 주석 해제
					// onPrev={() => setDate(addDays(date, -1))}
					// onNext={() => setDate(addDays(date, 1))}
				/>
			)
		case '주간':
			// todo: 주간 API 연동 시 실제 기간/이동 처리 (현재 mock 라벨)
			return (
				<RecordPeriodNav
					label="6월 1주 · 26년 6월 1일 ~ 26년 6월 6일"
					onPrev={noop}
					onNext={noop}
				/>
			)
		case '월간':
			// todo: 월간 API 연동 시 실제 기간/이동 처리 (현재 mock 라벨)
			return <RecordPeriodNav label="2026년 6월" onPrev={noop} onNext={noop} />
	}
}

function ReportByTab({ tab }: { tab: RecordTab }) {
	// record API에 정령 정보가 없어 대표 정령 조회 사용 (TodaySpiritCard와 캐시 공유)
	const { data: spiritData } = useQuery({
		queryKey: ['spirit', 'current'],
		queryFn: getCurrentSpirit,
	})
	const spirit = spiritData?.result === 'success' ? spiritData.detail : null

	switch (tab) {
		case '일간':
			return (
				<RecordDailyReport
					spiritName={spirit?.spiritName}
					spiritImageUrl={spirit?.imageUrl}
				/>
			)
		case '주간':
			return <RecordWeeklyReport />
		case '월간':
			return <RecordMonthlyReport />
	}
}

export function RecordPage() {
	const [tab, setTab] = useState<RecordTab>('일간')

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
				<PeriodNavByTab tab={tab} />
			</div>

			<div className="min-h-0 flex-1">
				<ReportByTab tab={tab} />
			</div>
		</main>
	)
}

export default RecordPage
