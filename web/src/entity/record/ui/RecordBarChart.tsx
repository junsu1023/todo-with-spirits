import { Crown } from 'lucide-react'
import type { ReactNode } from 'react'

export interface BarDatum {
	key: string
	label: string
	subLabel?: string
	/** null이면 아직 오지 않은 기간 → '?' 표시 */
	value: number | null
	tooltip?: ReactNode
}

const MIN_BAR_HEIGHT = 4

const findBestIndex = (data: BarDatum[]) => {
	const max = Math.max(0, ...data.map((d) => d.value ?? 0))
	return max > 0 ? data.findIndex((d) => d.value === max) : -1
}

export function RecordBarChart({
	data,
	height = 180,
}: {
	data: BarDatum[]
	height?: number
}) {
	const max = Math.max(1, ...data.map((d) => d.value ?? 0))
	const bestIndex = findBestIndex(data)

	return (
		<div className="flex items-end gap-2" style={{ height: height + 48 }}>
			{data.map((d, i) => {
				const isBest = i === bestIndex
				const barHeight = Math.max(((d.value ?? 0) / max) * height, MIN_BAR_HEIGHT)

				return (
					<div
						key={d.key}
						className="group relative flex flex-1 flex-col items-center gap-1"
					>
						{isBest ? (
							<Crown size={16} className="fill-[#FFD84D] text-[#F5B400]" />
						) : (
							<span className="h-4" />
						)}
						<span className="text-xs font-medium text-gray-500">
							{d.value ?? '?'}
						</span>
						<div
							className={`w-full max-w-12 rounded-t-md transition-colors ${
								isBest ? 'bg-brand-light' : 'bg-gray-200'
							} ${d.value === null ? 'opacity-40' : 'group-hover:bg-brand'}`}
							style={{ height: barHeight }}
						/>
						<span className="mt-1 text-xs text-gray-500">{d.label}</span>
						{d.subLabel && (
							<span className="text-[10px] text-gray-400">{d.subLabel}</span>
						)}

						{d.tooltip && (
							<div className="pointer-events-none absolute -top-2 left-1/2 z-10 hidden -translate-x-1/2 -translate-y-full whitespace-nowrap rounded-lg bg-white px-3 py-2 text-xs shadow-md ring-1 ring-gray-100 group-hover:block">
								{d.tooltip}
							</div>
						)}
					</div>
				)
			})}
		</div>
	)
}
