import { toPercent, toRate } from '../lib/progress'
import type { RecordTypeProgress } from '../model/type'
import { RecordRing } from './RecordRing'

interface DailyTypeProgressProps {
	label: string
	color: string
	progress: RecordTypeProgress
}

export function DailyTypeProgress({
	label,
	color,
	progress: { completed, total },
}: DailyTypeProgressProps) {
	return (
		<div className="flex flex-1 flex-col items-center gap-3 rounded-xl bg-gray-50 p-5">
			<span className="text-sm font-medium text-gray-600">{label}</span>
			<RecordRing percentage={toPercent(toRate(completed, total))} color={color} />
			<span className="text-xs text-gray-400">
				{completed} / {total}
			</span>
		</div>
	)
}
