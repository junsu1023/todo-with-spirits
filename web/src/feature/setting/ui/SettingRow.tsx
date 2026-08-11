import { ChevronRight, type LucideIcon } from 'lucide-react'

export function SettingRow({
	icon: Icon,
	label,
	description,
}: {
	icon: LucideIcon
	label: string
	description: string
}) {
	return (
		<button
			type="button"
			onClick={() => {}}
			className="cursor-pointer flex w-full items-center gap-4 rounded-xl bg-white px-5 py-4 text-left ring-1 ring-gray-100 transition-colors hover:bg-gray-50"
		>
			<div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gray-100">
				<Icon size={16} className="text-gray-500" />
			</div>
			<div className="flex flex-1 flex-col gap-0.5">
				<span className="text-sm font-medium text-gray-800">{label}</span>
				<span className="text-xs text-gray-400">{description}</span>
			</div>
			<ChevronRight size={16} className="shrink-0 text-gray-300" />
		</button>
	)
}
