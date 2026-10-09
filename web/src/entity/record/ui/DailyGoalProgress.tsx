import sampleSpiritImage from '@/shared/assets/sample-spirit.png'
import { toPercent } from '../lib/progress'

interface DailyGoalProgressProps {
	/** 0~1 비율 */
	completionRate: number
	completedCount: number
	totalCount: number
	/** 대표 정령 이미지 (없으면 기본 이미지) */
	spiritImageUrl?: string
}

export function DailyGoalProgress({
	completionRate,
	completedCount,
	totalCount,
	spiritImageUrl,
}: DailyGoalProgressProps) {
	const percentage = toPercent(completionRate)

	return (
		<div className="flex flex-col gap-2 pt-16">
			<div className="relative h-3 rounded-full bg-gray-100">
				<div
					className="h-full rounded-full bg-brand-light transition-[width]"
					style={{ width: `${percentage}%` }}
				/>
				<div
					className="absolute bottom-0 flex -translate-x-1/2 flex-col items-center"
					style={{ left: `${percentage}%` }}
				>
					<span className="mb-1 rounded-full bg-white px-2 py-0.5 text-xs font-semibold text-brand ring-1 ring-brand-light">
						{percentage}%
					</span>
					<img
						src={spiritImageUrl || sampleSpiritImage}
						alt=""
						className="size-10 object-contain"
						onError={(e) => {
							e.currentTarget.src = sampleSpiritImage
						}}
					/>
				</div>
			</div>
			<span className="self-end text-xs text-gray-400">
				오늘 목표 {completedCount} / {totalCount}개 달성
			</span>
		</div>
	)
}
