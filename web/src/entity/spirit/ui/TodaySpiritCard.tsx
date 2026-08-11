import { useQuery } from '@tanstack/react-query'
import { Flame } from 'lucide-react'
import { getCurrentSpirit } from '@/entity/spirit'
import sampleSpiritImage from '@/shared/assets/sample-spirit.png'
import { Card } from '@/shared/ui/card'
import { Skeleton } from '@/shared/ui/skeleton'

function TodaySpiritCardSkeleton() {
	return (
		<Card className="flex flex-col items-center gap-4 p-6">
			<Skeleton className="h-[120px] w-[120px] rounded-xl" />
			<div className="flex w-full flex-col gap-3">
				<div className="flex flex-col gap-1.5">
					<Skeleton className="h-5 w-4/5 rounded" />
					<Skeleton className="h-5 w-3/5 rounded" />
				</div>
				<div className="flex flex-col gap-2">
					<Skeleton className="h-4 w-24 rounded" />
					<Skeleton className="h-1 w-full rounded-full" />
					<Skeleton className="h-8 w-full rounded-md" />
				</div>
			</div>
		</Card>
	)
}

export function TodaySpiritCard() {
	const { data, isPending } = useQuery({
		queryKey: ['spirit', 'current'],
		queryFn: getCurrentSpirit,
	})

	if (isPending) return <TodaySpiritCardSkeleton />

	const spirit = data?.result === 'success' ? data.detail : null

	const todayPoints = spirit
		? (spirit.focusExp ?? 0) +
			(spirit.vitalityExp ?? 0) +
			(spirit.consistencyExp ?? 0) +
			(spirit.creativityExp ?? 0)
		: 0

	return (
		<Card className="flex flex-col items-center gap-4 p-6">
			<img
				src={spirit?.imageUrl || sampleSpiritImage}
				alt={spirit?.spiritName ?? '정령'}
				className="h-[120px] w-[120px] rounded-xl object-cover"
				onError={(e) => {
					e.currentTarget.src = sampleSpiritImage
				}}
			/>
			<div className="flex w-full flex-col gap-3">
				{/* 응원 메시지 */}
				<span className="text-lg font-bold leading-snug text-[#B286FD]">
					벌써 절반 왔어요!
					<br />
					{spirit?.spiritName ?? '—'}랑 조금 더 힘내봐요
				</span>

				<div className="flex flex-col gap-2">
					{/* 이름 + 레벨 + EXP 수치 */}
					<div className="flex items-center justify-between">
						<div className="flex items-center gap-1.5">
							<span className="text-sm font-semibold text-[#8F8170]">
								{spirit?.spiritName ?? '—'}
							</span>
							<span className="flex h-4 w-4 items-center justify-center rounded-full bg-[#B286FD] text-[10px] font-bold text-white">
								{spirit?.stage ?? 1}
							</span>
						</div>
						<span className="text-xs text-gray-400">
							{spirit?.exp ?? 0} / 100
						</span>
					</div>

					{/* EXP 바 */}
					<div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-200">
						<div
							className="h-full rounded-full bg-[#B286FD] transition-all"
							style={{ width: `${spirit?.exp ?? 0}%` }}
						/>
					</div>

					{/* 오늘의 성장 포인트 */}
					<div className="flex items-center gap-1.5 rounded-md bg-[#F3E8FF] px-3 py-2">
						<Flame size={14} className="text-[#B286FD]" />
						<span className="text-sm font-medium text-[#6B42E2]">
							오늘의 성장 포인트
						</span>
						<span className="ml-auto text-sm font-bold text-[#6B42E2]">
							+{todayPoints}
						</span>
					</div>
				</div>
			</div>
		</Card>
	)
}
