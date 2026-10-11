import { Lightbulb } from 'lucide-react'

export function RecordTip({
	title,
	description,
}: {
	title: string
	description: string
}) {
	return (
		<div className="flex items-center gap-3 rounded-xl border border-brand-light bg-brand-muted px-4 py-3">
			<Lightbulb size={18} className="shrink-0 text-brand" />
			<div className="flex flex-col gap-0.5">
				<span className="text-sm font-semibold text-brand">{title}</span>
				<span className="text-xs text-gray-500">{description}</span>
			</div>
		</div>
	)
}
