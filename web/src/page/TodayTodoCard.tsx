import { useQuery } from '@tanstack/react-query'
import { Star } from 'lucide-react'
import { useState } from 'react'
import { getRoutineList, getTaskSchedule } from '@/entity/task/api/query'
import type { RepeatType } from '@/entity/task/model/type'
import { TaskInputDock } from '@/feature/task/ui/TaskInputDock'
import { Card } from '@/shared/ui/card'

type DisplayRepeatType = '매일' | '매주' | '매월'

const REPEAT_MAP: Record<RepeatType, DisplayRepeatType | null> = {
	DAILY: '매일',
	WEEKLY: '매주',
	MONTHLY: '매월',
	YEARLY: null,
}

type MainTab = 'todo' | 'completed'

function formatDateBadge(date: string, time?: string) {
	const d = new Date(date)
	const label = `${d.getMonth() + 1}.${d.getDate()}`
	return time ? `${label} ${time}` : label
}

function formatRepeat(repeatType: RepeatType | undefined) {
	if (!repeatType) return ''
	return REPEAT_MAP[repeatType] ?? ''
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
					className="size-[13px] stroke-white stroke-[3] fill-none"
					role="img"
					aria-label="완료"
				>
					<polyline points="20 6 9 17 4 12" />
				</svg>
			)}
		</button>
	)
}

interface TodayTodoCardProps {
	selectedDate: Date
}

function toDateString(date: Date) {
	const y = date.getFullYear()
	const m = String(date.getMonth() + 1).padStart(2, '0')
	const d = String(date.getDate()).padStart(2, '0')
	return `${y}-${m}-${d}`
}

export function TodayTodoCard({ selectedDate }: TodayTodoCardProps) {
	const dateStr = toDateString(selectedDate)

	const { data: todoList } = useQuery({
		queryKey: ['task', 'schedule', dateStr],
		queryFn: () => getTaskSchedule({ from: dateStr, to: dateStr }),
	})
	const { data: routineList } = useQuery({
		queryKey: ['task', 'routine', dateStr],
		queryFn: () => getRoutineList({ from: dateStr, to: dateStr }),
	})

	const [mainTab, setMainTab] = useState<MainTab>('todo')

	// toggle 오버레이 (API 아이템 XOR)
	const [completedTodoIds, setCompletedTodoIds] = useState<Set<number>>(
		new Set(),
	)
	const [starredTodoIds, setStarredTodoIds] = useState<Set<number>>(new Set())
	const [completedRoutineIds, setCompletedRoutineIds] = useState<Set<number>>(
		new Set(),
	)

	// API 결과 직접 파생
	const apiTodos =
		todoList?.result === 'success' ? todoList.detail.items : []
	const allRoutines =
		routineList?.result === 'success' ? routineList.detail.items : []

	// 오버레이 적용
	const todosWithOverlay = apiTodos.map((t) => ({
		...t,
		isCompleted: completedTodoIds.has(t.taskId)
			? !t.isCompleted
			: t.isCompleted,
		isImportant: starredTodoIds.has(t.taskId) ? !t.isImportant : t.isImportant,
	}))
	const routinesWithOverlay = allRoutines.map((r) => ({
		...r,
		isCompleted: completedRoutineIds.has(r.taskId)
			? !r.isCompleted
			: r.isCompleted,
	}))

	const displayedTodos =
		mainTab === 'todo'
			? todosWithOverlay
			: todosWithOverlay.filter((t) => t.isCompleted)
	const displayedRoutines =
		mainTab === 'todo'
			? routinesWithOverlay
			: routinesWithOverlay.filter((r) => r.isCompleted)

	const flipSet = (prev: Set<number>, id: number): Set<number> => {
		const next = new Set(prev)
		next.has(id) ? next.delete(id) : next.add(id)
		return next
	}

	const toggleTodo = (id: number) =>
		setCompletedTodoIds((prev) => flipSet(prev, id))

	const toggleStar = (id: number) =>
		setStarredTodoIds((prev) => flipSet(prev, id))

	const toggleRoutine = (id: number) =>
		setCompletedRoutineIds((prev) => flipSet(prev, id))

	return (
		<Card className="flex min-h-0 flex-1 flex-col gap-5 overflow-hidden p-6">
			{/* 헤더 */}
			<div className="flex items-center justify-between">
				<h2 className="text-xl font-bold">Plan</h2>
				<div className="flex rounded-full bg-gray-100 p-1">
					{(['todo', 'completed'] as MainTab[]).map((tab) => (
						<button
							key={tab}
							type="button"
							onClick={() => setMainTab(tab)}
							className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
								mainTab === tab
									? 'bg-white text-gray-900 shadow-sm'
									: 'text-gray-400'
							}`}
						>
							{tab === 'todo' ? 'To-do' : 'Completed'}
						</button>
					))}
				</div>
			</div>

			{/* To do + 루틴 2열 */}
			<div className="flex min-h-0 flex-1 gap-6 overflow-y-auto">
				{/* 할 일 */}
				<div className="flex flex-1 flex-col gap-4">
					<p className="font-semibold text-gray-700">To do</p>
					{displayedTodos.length === 0 && (
						<p className="text-sm text-gray-400">
							{mainTab === 'todo'
								? '아직 등록된 할 일이 없습니다'
								: '완료된 할 일이 없습니다'}
						</p>
					)}
					{displayedTodos.map((todo) => (
						<div key={todo.taskId} className="flex items-center gap-3">
							<CheckButton
								checked={todo.isCompleted}
								color="#48CAD9"
								onToggle={() => toggleTodo(todo.taskId)}
							/>
							<div className="flex flex-1 flex-col">
								<span
									className={`text-base ${
										todo.isCompleted
											? 'text-gray-400 line-through'
											: 'text-gray-800'
									}`}
								>
									{todo.title}
								</span>
								{todo.endDate && (
									<span className="text-xs text-[#B286FD]">
										{formatDateBadge(todo.endDate, todo.endTime?.slice(0, 5))}
									</span>
								)}
							</div>
							<button type="button" onClick={() => toggleStar(todo.taskId)}>
								<Star
									size={16}
									className={
										todo.isImportant
											? 'fill-[#B286FD] text-[#B286FD]'
											: 'text-gray-200'
									}
								/>
							</button>
						</div>
					))}
				</div>

				<div className="w-px bg-gray-100" />

				{/* 루틴 */}
				<div className="flex flex-1 flex-col gap-4">
					<p className="font-semibold text-gray-700">루틴</p>
					{displayedRoutines.length === 0 && (
						<p className="text-sm text-gray-400">
							{mainTab === 'todo'
								? '아직 등록된 루틴이 없습니다'
								: '완료한 루틴이 없습니다'}
						</p>
					)}
					{displayedRoutines.map((routine) => (
						<div key={routine.taskId} className="flex items-center gap-3">
							<CheckButton
								checked={routine.isCompleted}
								color="#B2F042"
								onToggle={() => toggleRoutine(routine.taskId)}
							/>
							<div className="flex flex-1 flex-col">
								<span
									className={`text-base ${
										routine.isCompleted
											? 'text-gray-400 line-through'
											: 'text-gray-800'
									}`}
								>
									{routine.title}
								</span>
								<span className="text-xs text-[#B2A042]">
									{formatRepeat(routine.repeatType)}
								</span>
							</div>
						</div>
					))}
				</div>
			</div>

			{/* 입력 도크 */}
			<TaskInputDock dateStr={dateStr} />
		</Card>
	)
}
