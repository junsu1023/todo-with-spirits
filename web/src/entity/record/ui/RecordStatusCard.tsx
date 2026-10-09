import { Card } from '@/shared/ui/card'
import { Skeleton } from '@/shared/ui/skeleton'

export function RecordErrorCard({ message }: { message: string }) {
	return (
		<Card className="items-center p-10 text-sm text-gray-400">{message}</Card>
	)
}

/** 주간/월간 공통 2열 스켈레톤 */
export function RecordReportSkeleton() {
	return (
		<div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
			{Array.from({ length: 2 }, (_, i) => (
				// biome-ignore lint/suspicious/noArrayIndexKey: 고정 길이 스켈레톤
				<Card key={i} className="flex flex-col gap-5 p-6">
					<Skeleton className="h-5 w-24" />
					<Skeleton className="h-8 w-40" />
					<Skeleton className="h-48 w-full rounded-xl" />
					<div className="flex gap-3">
						<Skeleton className="h-16 flex-1 rounded-xl" />
						<Skeleton className="h-16 flex-1 rounded-xl" />
					</div>
				</Card>
			))}
		</div>
	)
}
