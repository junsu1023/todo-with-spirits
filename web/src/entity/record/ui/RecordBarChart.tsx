import { Crown } from 'lucide-react'
import type { BarDatum } from '../model/mock'

export function RecordBarChart({
	data,
	height = 180,
}: {
	data: BarDatum[]
	height?: number
}) {
	const max = Math.max(...data.map((d) => d.value), 1)
	const bestIndex = data.findIndex((d) => d.value === max)

	return (
		<div className="flex items-end gap-3" style={{ height: height + 48 }}>
			{data.map((d, i) => {
				const isBest = i === bestIndex && d.value > 0
				const barHeight = Math.max((d.value / max) * height, 4)
				const hasTooltip = d.todo || d.routine

				return (
					<div
						key={`${d.label}-${d.subLabel ?? ''}`}
						className="group relative flex flex-1 flex-col items-center gap-1"
					>
						{isBest ? (
							<Crown size={16} className="fill-[#FFD84D] text-[#F5B400]" />
						) : (
							<span className="h-4" />
						)}
						<span className="text-xs font-medium text-gray-500">
							{d.value > 0 ? d.value : ''}
						</span>
						<div
							className={`w-full max-w-12 rounded-t-md transition-colors ${
								isBest ? 'bg-brand-light' : 'bg-gray-200'
							} group-hover:bg-brand`}
							style={{ height: barHeight }}
						/>
						<span className="mt-1 text-xs text-gray-500">{d.label}</span>
						{d.subLabel && (
							<span className="text-[10px] text-gray-400">{d.subLabel}</span>
						)}

						{hasTooltip && (
							<div className="pointer-events-none absolute -top-2 left-1/2 z-10 hidden -translate-x-1/2 -translate-y-full whitespace-nowrap rounded-lg bg-white px-3 py-2 text-xs shadow-md ring-1 ring-gray-100 group-hover:block">
								<p className="text-[#48CAD9]">To do {d.todo ?? '-'}</p>
								<p className="text-[#8FC21F]">루틴 {d.routine ?? '-'}</p>
							</div>
						)}
					</div>
				)
			})}
		</div>
	)
}
