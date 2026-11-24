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
	// 정령 단계·경험치(stage, exp)가 정령도감 응답에서 제거됨
	// todo: 호감도(affection) 등 보여줄 값이 정해지면 다시 연결
	// const expPercent = Math.round((spirit.exp / 100) * 100)
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
				{/* 서버 응답이 imageUrl → image.url로 바뀜 (웹용 에셋이 없으면 null → 기본 이미지) */}
				<img
					src={spirit.image?.url || sampleSpiritImage}
					alt={spirit.spiritName ?? spirit.speciesName}
					className="h-16 w-16 rounded-xl object-cover"
					onError={(e) => { e.currentTarget.src = sampleSpiritImage }}
				/>
				{isRepresentative && (
					<span className="absolute -top-1.5 -right-1.5 rounded-full bg-[#B286FD] px-1.5 py-0.5 text-[9px] font-bold text-white leading-tight">
						대표
					</span>
				)}
			</div>

			{/* 이름 (레벨은 정령 단계가 서버 응답에서 제거됨) */}
			<div className="flex items-center gap-1">
				<span className="text-sm font-semibold text-gray-800">
					{spirit.spiritName ?? spirit.speciesName}
				</span>
				{/*
				<span className="flex h-4 w-4 items-center justify-center rounded-full bg-[#B286FD] text-[10px] font-bold text-white">
					{spirit.stage}
				</span>
				*/}
			</div>

			{/* EXP 바: 정령도감 응답에 경험치가 없음 (위 expPercent 참고)
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
			*/}
		</button>
	)
}
