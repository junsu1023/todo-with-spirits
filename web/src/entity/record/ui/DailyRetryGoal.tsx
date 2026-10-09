export function DailyRetryGoal({ count }: { count: number }) {
	return (
		<div className="flex flex-col gap-2">
			<div className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3">
				<span className="text-sm text-gray-600">내일 다시 도전할 목표</span>
				<span className="text-sm font-semibold text-gray-800">{count}개</span>
			</div>
			<span className="self-end rounded-full bg-brand-muted px-3 py-1 text-xs text-brand">
				잊지말고 도전해보세요!
			</span>
		</div>
	)
}
