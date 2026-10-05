import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useState } from 'react'
import { RecordDailyReport } from './RecordDailyReport'
import { RecordMonthlyReport } from './RecordMonthlyReport'
import { RecordWeeklyReport } from './RecordWeeklyReport'

type RecordTab = '일간' | '주간' | '월간'

const TABS: RecordTab[] = ['일간', '주간', '월간']

// todo: 기간 이동 실제 데이터 연동 (현재는 mock 라벨)
const PERIOD_LABEL: Record<RecordTab, string> = {
	일간: '2026. 06. 06 (토)',
	주간: '6월 1주 · 26년 6월 1일 ~ 26년 6월 6일',
	월간: '2026년 6월',
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
				<div className="flex items-center gap-2 pb-2">
					<button
						type="button"
						aria-label="이전"
						className="flex size-8 items-center justify-center rounded-full hover:bg-gray-100"
					>
						<ChevronLeft size={18} />
					</button>
					<span className="text-sm font-semibold text-gray-700">
						{PERIOD_LABEL[tab]}
					</span>
					<button
						type="button"
						aria-label="다음"
						className="flex size-8 items-center justify-center rounded-full hover:bg-gray-100"
					>
						<ChevronRight size={18} />
					</button>
				</div>
			</div>

			<div className="min-h-0 flex-1 ">
				{tab === '일간' && <RecordDailyReport />}
				{tab === '주간' && <RecordWeeklyReport />}
				{tab === '월간' && <RecordMonthlyReport />}
			</div>
		</main>
	)
}

export default RecordPage
