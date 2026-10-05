import {
	Briefcase,
	Crown,
	HeartPulse,
	Lightbulb,
	Palette,
	Share,
	Users,
} from 'lucide-react'
import type { ReactNode } from 'react'
import { Card } from '@/shared/ui/card'
import type { BarDatum, CategoryCount, RecordCategory } from './recordMock'

// ─── Card ────────────────────────────────────────────────────────────────────

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

// ─── Stat ────────────────────────────────────────────────────────────────────

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

// ─── Bar chart ───────────────────────────────────────────────────────────────

export function RecordBarChart({
	data,
	height = 180,
}: {
	data: BarDatum[]
	height?: number
}) {
	const max = Math.max(...data.map((d) => d.value), 1)
	const bestIndex = data.findIndex((d) => d.value === max)

	return (
		<div className="flex items-end gap-3" style={{ height: height + 48 }}>
			{data.map((d, i) => {
				const isBest = i === bestIndex && d.value > 0
				const barHeight = Math.max((d.value / max) * height, 4)
				const hasTooltip = d.todo || d.routine

				return (
					<div
						key={`${d.label}-${d.subLabel ?? ''}`}
						className="group relative flex flex-1 flex-col items-center gap-1"
					>
						{isBest ? (
							<Crown size={16} className="fill-[#FFD84D] text-[#F5B400]" />
						) : (
							<span className="h-4" />
						)}
						<span className="text-xs font-medium text-gray-500">
							{d.value > 0 ? d.value : ''}
						</span>
						<div
							className={`w-full max-w-12 rounded-t-md transition-colors ${
								isBest ? 'bg-brand-light' : 'bg-gray-200'
							} group-hover:bg-brand`}
							style={{ height: barHeight }}
						/>
						<span className="mt-1 text-xs text-gray-500">{d.label}</span>
						{d.subLabel && (
							<span className="text-[10px] text-gray-400">{d.subLabel}</span>
						)}

						{hasTooltip && (
							<div className="pointer-events-none absolute -top-2 left-1/2 z-10 hidden -translate-x-1/2 -translate-y-full whitespace-nowrap rounded-lg bg-white px-3 py-2 text-xs shadow-md ring-1 ring-gray-100 group-hover:block">
								<p className="text-[#48CAD9]">To do {d.todo ?? '-'}</p>
								<p className="text-[#8FC21F]">루틴 {d.routine ?? '-'}</p>
							</div>
						)}
					</div>
				)
			})}
		</div>
	)
}

// ─── Category ────────────────────────────────────────────────────────────────

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

// ─── Ring ────────────────────────────────────────────────────────────────────

export function RecordRing({
	percentage,
	color,
	size = 120,
}: {
	percentage: number
	color: string
	size?: number
}) {
	const stroke = 10
	const radius = (size - stroke) / 2
	const circumference = 2 * Math.PI * radius
	const offset = circumference - (circumference * percentage) / 100
	const c = size / 2

	return (
		<div className="relative" style={{ width: size, height: size }}>
			<svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
				<title>{`달성률 ${percentage}%`}</title>
				<circle
					cx={c}
					cy={c}
					r={radius}
					fill="none"
					stroke="#F3F4F6"
					strokeWidth={stroke}
				/>
				<circle
					cx={c}
					cy={c}
					r={radius}
					fill="none"
					stroke={color}
					strokeWidth={stroke}
					strokeDasharray={circumference}
					strokeDashoffset={offset}
					strokeLinecap="round"
					transform={`rotate(-90 ${c} ${c})`}
				/>
			</svg>
			<span className="absolute inset-0 flex items-center justify-center text-xl font-bold text-gray-700">
				{percentage}%
			</span>
		</div>
	)
}
