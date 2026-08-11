import { cn } from '@/lib/utils'
import sampleSpiritImage from '@/shared/assets/sample-spirit.png'
import type { Spirit } from '../model/type'

export function SpiritCard({
	spirit,
	isRepresentative = false,
	isSelected,
	onClick,
}: {
	spirit: Spirit
	isRepresentative: boolean
	isSelected: boolean
	onClick: () => void
}) {
	const expPercent = Math.round((spirit.exp / 100) * 100)
	return (
		<button
			type="button"
			onClick={onClick}
			className={cn(
				'flex flex-col items-center gap-2 rounded-2xl border-2 p-4 text-left transition-all',
				isSelected
					? 'border-[#B286FD] bg-[#F3E8FF]'
					: 'border-transparent bg-gray-50 hover:border-gray-200 hover:bg-gray-100',
			)}
		>
			{/* 정령 이미지 */}
			<div className="relative">
				<img
					src={spirit.imageUrl || sampleSpiritImage}
					alt={spirit.spiritName}
					className="h-16 w-16 rounded-xl object-cover"
					onError={(e) => { e.currentTarget.src = sampleSpiritImage }}
				/>
				{isRepresentative && (
					<span className="absolute -top-1.5 -right-1.5 rounded-full bg-[#B286FD] px-1.5 py-0.5 text-[9px] font-bold text-white leading-tight">
						대표
					</span>
				)}
			</div>

			{/* 이름 + 레벨 */}
			<div className="flex items-center gap-1">
				<span className="text-sm font-semibold text-gray-800">
					{spirit.spiritName}
				</span>
				<span className="flex h-4 w-4 items-center justify-center rounded-full bg-[#B286FD] text-[10px] font-bold text-white">
					{spirit.stage}
				</span>
			</div>

			{/* EXP 바 */}
			<div className="w-full">
				<div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-200">
					<div
						className="h-full rounded-full bg-[#B286FD]"
						style={{ width: `${expPercent}%` }}
					/>
				</div>
				<span className="mt-0.5 block text-center text-[10px] text-gray-400">
					{expPercent}%
				</span>
			</div>
		</button>
	)
}
