import { Star, X } from 'lucide-react'
import { WEEKLY_MOCK, type WeekRecordStatus } from './recordMock'
import {
	CategoryRanking,
	MissedCategory,
	RecordBarChart,
	RecordCard,
	RecordHeadline,
	RecordStat,
	RecordTip,
} from './RecordShared'

function WeekRecord({ records }: { records: WeekRecordStatus[] }) {
	return (
		<div className="flex flex-col gap-3 rounded-xl bg-gray-50 px-4 py-4">
			<span className="text-xs text-gray-400">주간 기록</span>
			<div className="flex justify-between">
				{records.map((status, i) => (
					<div
						// biome-ignore lint/suspicious/noArrayIndexKey: 요일 순서 고정
						key={i}
						className="flex flex-col items-center gap-1"
					>
						<div
							className={`flex size-10 items-center justify-center rounded-full ${
								status === 'success'
									? 'bg-brand-light'
									: 'border-2 border-gray-200 bg-white'
							}`}
						>
							{status === 'success' && (
								<Star size={18} className="fill-white text-white" />
							)}
							{status === 'fail' && <X size={18} className="text-gray-300" />}
						</div>
						<span className="text-[10px] text-gray-400">{i + 1}일차</span>
					</div>
				))}
			</div>
		</div>
	)
}

function RatioBar({
	todo,
	routine,
	postpone,
}: {
	todo: number
	routine: number
	postpone: number
}) {
	const segments = [
		{ label: 'To do', value: todo, className: 'bg-sky' },
		{ label: '루틴', value: routine, className: 'bg-lime' },
		{ label: '미루기', value: postpone, className: 'bg-gray-200' },
	]

	return (
		<div className="flex flex-col gap-2">
			<div className="flex h-7 gap-0.5 overflow-hidden rounded-lg">
				{segments.map((s) => (
					<div
						key={s.label}
						className={`flex flex-1 items-center justify-center text-xs font-semibold text-gray-700 ${s.className}`}
					>
						{s.value}%
					</div>
				))}
			</div>
			<div className="flex justify-center gap-4">
				{segments.map((s) => (
					<span
						key={s.label}
						className="flex items-center gap-1 text-xs text-gray-500"
					>
						<span className={`size-2 rounded-full ${s.className}`} />
						{s.label}
					</span>
				))}
			</div>
		</div>
	)
}

export function RecordWeeklyReport() {
	const w = WEEKLY_MOCK

	return (
		<div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
			<RecordCard title="주간 리포트">
				<RecordHeadline title={w.headline} message={w.message} />
				<RecordBarChart data={w.bars} />
				<div className="flex gap-3">
					<RecordStat
						label="이번 주 달성 플랜"
						value={`${w.completedCount}개`}
						suffix={`/ ${w.totalCount}`}
					/>
					<RecordStat label="평균 달성률" value={`${w.averageRate}%`} />
				</div>
				<WeekRecord records={w.records} />
			</RecordCard>

			<RecordCard title="주간 분석">
				<RatioBar {...w.ratio} />
				<CategoryRanking title="주간 실천 Top 3" items={w.topCategories} />
				<MissedCategory item={w.missedCategory} />
				<RecordTip {...w.tip} />
			</RecordCard>
		</div>
	)
}
