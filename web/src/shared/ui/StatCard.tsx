import { Card } from './card'

export function StatCard({
	label,
	value,
	unit,
}: {
	label: string
	value: number
	unit: string
}) {
	return (
		<Card className="flex w-full flex-col items-center justify-center gap-2 p-4">
			<span className="text-sm text-gray-400">{label}</span>
			<div className="flex items-end gap-1">
				<span className="text-5xl font-bold text-[#B286FD]">{value}</span>
				<span className="mb-1 text-lg font-medium text-gray-500">{unit}</span>
			</div>
		</Card>
	)
}
