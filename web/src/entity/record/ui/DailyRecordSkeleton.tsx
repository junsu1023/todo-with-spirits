import { Card } from '@/shared/ui/card'
import { Skeleton } from '@/shared/ui/skeleton'

export function DailyRecordSkeleton() {
	return (
		<div className="grid grid-cols-1 gap-6 lg:grid-cols-[3fr_2fr]">
			<Card className="flex flex-col gap-5 p-6">
				<Skeleton className="h-5 w-24" />
				<Skeleton className="h-8 w-40" />
				<Skeleton className="h-3 w-full rounded-full" />
				<div className="flex gap-4">
					<Skeleton className="h-48 flex-1 rounded-xl" />
					<Skeleton className="h-48 flex-1 rounded-xl" />
				</div>
				<Skeleton className="h-12 w-full rounded-xl" />
			</Card>
			<Card className="flex flex-col gap-2 p-6">
				<Skeleton className="mb-3 h-5 w-24" />
				{Array.from({ length: 4 }, (_, i) => (
					// biome-ignore lint/suspicious/noArrayIndexKey: 고정 길이 스켈레톤
					<Skeleton key={i} className="h-16 w-full rounded-xl" />
				))}
			</Card>
		</div>
	)
}
