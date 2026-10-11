import {
	Briefcase,
	CircleDashed,
	HeartPulse,
	type LucideIcon,
	Palette,
	Shapes,
	Sprout,
	Users,
	Wallet,
} from 'lucide-react'
import type { Category } from '@/entity/task/model/type'

// todo: 디자인 아이콘 확정 전까지 lucide 아이콘으로 임시 대체
export const CATEGORY_ICON: Record<Category, LucideIcon> = {
	FINANCE: Wallet,
	GROWTH: Sprout,
	HEALTH: HeartPulse,
	HOBBY: Palette,
	RELATIONSHIP: Users,
	WORK: Briefcase,
	ETC: Shapes,
	NONE: CircleDashed,
}

export interface RankItem {
	key: string
	icon: LucideIcon
	label: string
	value: string
}

interface RecordRankListProps {
	title: string
	items: RankItem[]
	emptyText?: string
}

export function RecordRankList({
	title,
	items,
	emptyText = '아직 기록이 없어요',
}: RecordRankListProps) {
	return (
		<div className="flex flex-col gap-2">
			<p className="text-sm font-medium text-gray-500">{title}</p>
			{items.length === 0 ? (
				<p className="rounded-xl bg-gray-50 px-4 py-3 text-sm text-gray-300">
					{emptyText}
				</p>
			) : (
				<ul className="flex flex-col gap-2">
					{items.map(({ key, icon: Icon, label, value }) => (
						<li
							key={key}
							className="flex items-center gap-3 rounded-xl bg-gray-50 px-4 py-3"
						>
							<Icon size={18} className="text-brand" />
							<span className="flex-1 text-sm text-gray-700">{label}</span>
							<span className="text-sm font-semibold text-gray-800">{value}</span>
						</li>
					))}
				</ul>
			)}
		</div>
	)
}
