export function RecordStat({
	label,
	value,
	suffix,
}: {
	label: string
	value: string
	suffix?: string
}) {
	return (
		<div className="flex flex-1 flex-col gap-1 rounded-xl bg-gray-50 px-4 py-3">
			<span className="text-xs text-gray-400">{label}</span>
			<div className="flex items-baseline gap-1">
				<span className="text-2xl font-bold text-gray-800">{value}</span>
				{suffix && <span className="text-xs text-gray-400">{suffix}</span>}
			</div>
		</div>
	)
}
