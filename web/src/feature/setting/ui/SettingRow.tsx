import { ChevronRight, type LucideIcon } from 'lucide-react'
import { Link } from 'react-router-dom'

const BASE_CLASS =
	'cursor-pointer flex w-full items-center gap-4 rounded-xl bg-white px-5 py-4 text-left ring-1 ring-gray-100 transition-colors hover:bg-gray-50'

export function SettingRow({
	icon: Icon,
	label,
	description,
	href,
}: {
	icon: LucideIcon
	label: string
	description: string
	href?: string
}) {
	const inner = (
		<>
			<div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gray-100">
				<Icon size={16} className="text-gray-500" />
			</div>
			<div className="flex flex-1 flex-col gap-0.5">
				<span className="text-sm font-medium text-gray-800">{label}</span>
				<span className="text-xs text-gray-400">{description}</span>
			</div>
			<ChevronRight size={16} className="shrink-0 text-gray-300" />
		</>
	)

	if (href) {
		return (
			<Link to={href} className={BASE_CLASS}>
				{inner}
			</Link>
		)
	}

	return (
		<button type="button" className={BASE_CLASS}>
			{inner}
		</button>
	)
}
