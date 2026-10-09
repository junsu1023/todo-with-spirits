import { Share } from 'lucide-react'
import type { ReactNode } from 'react'
import { Card } from '@/shared/ui/card'

export function RecordCard({
	title,
	children,
	className = '',
}: {
	title: string
	children: ReactNode
	className?: string
}) {
	return (
		<Card className={`flex flex-col gap-5 p-6 ${className}`}>
			<div className="flex items-center justify-between">
				<h3 className="text-base font-semibold text-gray-700">{title}</h3>
				{/* todo: 공유하기 기능 미정 */}
				<button
					type="button"
					aria-label="공유하기"
					className="flex size-8 items-center justify-center rounded-full text-gray-400 hover:bg-gray-100 hover:text-gray-600"
				>
					<Share size={16} />
				</button>
			</div>
			{children}
		</Card>
	)
}

export function RecordHeadline({
	title,
	message,
}: {
	title: string
	message: string
}) {
	return (
		<div className="flex flex-col gap-1">
			<span className="text-2xl font-bold text-brand">{title}</span>
			<span className="text-sm text-gray-400">{message}</span>
		</div>
	)
}
