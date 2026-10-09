import { toPercent } from '../lib/progress'
import type { WeeklyTypeAnalysis } from '../model/type'

// todo: ratio를 0~1 비율로 가정 (android 표시 기준), 서버 스펙 확인 필요
export function WeeklyTypeRatioBar({ analysis }: { analysis: WeeklyTypeAnalysis }) {
	const segments = [
		{ label: 'To do', ratio: analysis.scheduleRatio, color: 'bg-sky' },
		{ label: '루틴', ratio: analysis.routineRatio, color: 'bg-lime' },
		{ label: '미루기', ratio: analysis.delayedRatio, color: 'bg-gray-200' },
	]
	const isEmpty = segments.every((s) => s.ratio <= 0)

	return (
		<div className="flex flex-col gap-2">
			<div className="flex h-7 gap-0.5 overflow-hidden rounded-lg bg-gray-100">
				{isEmpty ? (
					<span className="flex flex-1 items-center justify-center text-xs text-gray-400">
						아직 기록이 없어요
					</span>
				) : (
					segments
						.filter((s) => s.ratio > 0)
						.map((s) => (
							<div
								key={s.label}
								className={`flex min-w-10 items-center justify-center text-xs font-semibold text-gray-700 ${s.color}`}
								style={{ flexGrow: s.ratio }}
							>
								{toPercent(s.ratio)}%
							</div>
						))
				)}
			</div>
			<div className="flex justify-center gap-4">
				{segments.map((s) => (
					<span key={s.label} className="flex items-center gap-1 text-xs text-gray-500">
						<span className={`size-2 rounded-full ${s.color}`} />
						{s.label}
					</span>
				))}
			</div>
		</div>
	)
}
