import { ChevronLeft, ChevronRight } from 'lucide-react'

interface RecordPeriodNavProps {
	label: string
	onPrev?: () => void
	onNext?: () => void
}

/** 핸들러가 없으면 화살표 없이 기간 라벨만 표시 */
export function RecordPeriodNav({ label, onPrev, onNext }: RecordPeriodNavProps) {
	return (
		<div className="flex items-center gap-2 pb-2">
			{onPrev && (
				<button
					type="button"
					aria-label="이전"
					onClick={onPrev}
					className="flex size-8 items-center justify-center rounded-full hover:bg-gray-100"
				>
					<ChevronLeft size={18} />
				</button>
			)}
			<span className="text-sm font-semibold text-gray-700">{label}</span>
			{onNext && (
				<button
					type="button"
					aria-label="다음"
					onClick={onNext}
					className="flex size-8 items-center justify-center rounded-full hover:bg-gray-100"
				>
					<ChevronRight size={18} />
				</button>
			)}
		</div>
	)
}
