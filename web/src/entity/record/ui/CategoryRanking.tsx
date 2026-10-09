import {
	Briefcase,
	HeartPulse,
	Palette,
	Users,
} from 'lucide-react'
import type { CategoryCount, RecordCategory } from '../model/mock'

const CATEGORY_META: Record<
	RecordCategory,
	{ label: string; icon: typeof Briefcase }
> = {
	WORK: { label: '학업/커리어', icon: Briefcase },
	RELATIONSHIP: { label: '인간관계/약속', icon: Users },
	HOBBY: { label: '취미', icon: Palette },
	HEALTH: { label: '건강', icon: HeartPulse },
}

function CategoryRow({ category, count }: CategoryCount) {
	const { label, icon: Icon } = CATEGORY_META[category]
	return (
		<li className="flex items-center gap-3 rounded-xl bg-gray-50 px-4 py-3">
			<Icon size={18} className="text-brand" />
			<span className="flex-1 text-sm text-gray-700">{label}</span>
			<span className="text-sm font-semibold text-gray-800">{count}회</span>
		</li>
	)
}

export function CategoryRanking({
	title,
	items,
}: {
	title: string
	items: CategoryCount[]
}) {
	return (
		<div className="flex flex-col gap-2">
			<p className="text-sm font-medium text-gray-500">{title}</p>
			<ul className="flex flex-col gap-2">
				{items.map((item) => (
					<CategoryRow key={item.category} {...item} />
				))}
			</ul>
		</div>
	)
}

export function MissedCategory({ item }: { item: CategoryCount }) {
	return (
		<div className="flex flex-col gap-2">
			<p className="text-sm font-medium text-gray-500">자주 놓친 분야</p>
			<ul>
				<CategoryRow {...item} />
			</ul>
		</div>
	)
}
