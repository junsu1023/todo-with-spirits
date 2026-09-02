import { Accordion } from '@base-ui/react/accordion'
import { CalendarClock, Pencil, Trash2 } from 'lucide-react'
import type { ReactNode } from 'react'

interface TaskItemProps {
	value: string
	title: string
	isCompleted: boolean
	checkColor: string
	onToggle: () => void
	onPostpone: () => void
	onEdit: () => void
	onDelete: () => void
	subtitle?: ReactNode
	trailing?: ReactNode
}

function CheckButton({
	checked,
	color,
	onToggle,
}: {
	checked: boolean
	color: string
	onToggle: () => void
}) {
	return (
		<button
			type="button"
			onClick={onToggle}
			style={checked ? { backgroundColor: color } : undefined}
			className={`flex size-7 shrink-0 items-center justify-center rounded-full transition-colors ${
				checked ? '' : 'border-2 border-gray-300 bg-white'
			}`}
		>
			{checked && (
				<svg
					viewBox="0 0 24 24"
					className="size-[13px] fill-none stroke-white stroke-[3]"
					role="img"
					aria-label="완료"
				>
					<polyline points="20 6 9 17 4 12" />
				</svg>
			)}
		</button>
	)
}

export function TaskItem({
	value,
	title,
	isCompleted,
	checkColor,
	onToggle,
	onPostpone,
	onEdit,
	onDelete,
	subtitle,
	trailing,
}: TaskItemProps) {
	return (
		<Accordion.Item
			value={value}
			className="flex flex-col hover:bg-gray-50 cursor-pointer rounded-xl p-1.5 transition-colors data-[open]:bg-gray-50"
		>
			<div className="flex items-center gap-3">
				<CheckButton
					checked={isCompleted}
					color={checkColor}
					onToggle={onToggle}
				/>
				<Accordion.Header render={<div />} className="flex min-w-0 flex-1">
					<Accordion.Trigger className="flex w-full cursor-pointer flex-col text-left">
						<span
							className={`text-base ${
								isCompleted ? 'text-gray-400 line-through' : 'text-gray-800'
							}`}
						>
							{title}
						</span>
						{subtitle}
					</Accordion.Trigger>
				</Accordion.Header>
				{trailing}
			</div>
			<Accordion.Panel className="ml-10 mt-1.5 flex items-center gap-1">
				{/* todo: 핸들러 연결 필요 */}
				<button
					type="button"
					onClick={onPostpone}
					className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
				>
					<CalendarClock size={13} />
					미루기
				</button>
				<button
					type="button"
					onClick={onEdit}
					className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
				>
					<Pencil size={13} />
					수정
				</button>
				<button
					type="button"
					onClick={onDelete}
					className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs text-red-300 transition-colors hover:bg-red-50 hover:text-red-400"
				>
					<Trash2 size={13} />
					삭제
				</button>
			</Accordion.Panel>
		</Accordion.Item>
	)
}
