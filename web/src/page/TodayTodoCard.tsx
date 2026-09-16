//todo: 컴포넌트 분리 필요
import { Accordion } from '@base-ui/react/accordion'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Star } from 'lucide-react'
import { useState } from 'react'
import { getTaskCalendar } from '@/entity/task/api/query'
import type { CalendarItem, RepeatType } from '@/entity/task/model/type'
import {
	completeTask,
	deleteTasks,
	uncompleteTask,
	updateSchedule,
} from '@/feature/task/api/mutate'
import { TaskEditForm } from '@/feature/task/ui/TaskEditForm'
import { TaskInputDock } from '@/feature/task/ui/TaskInputDock'
import { TaskItem } from '@/feature/task/ui/TaskItem'
import { Card } from '@/shared/ui/card'
import { Dialog, DialogPopup } from '@/shared/ui/dialog'

type DisplayRepeatType = '매일' | '매주' | '매월'

const REPEAT_MAP: Record<RepeatType, DisplayRepeatType | null> = {
	DAILY: '매일',
	WEEKLY: '매주',
	MONTHLY: '매월',
	YEARLY: null,
}

type MainTab = 'todo' | 'completed'

function formatRepeat(repeatType: RepeatType | undefined) {
	if (!repeatType) return ''
	return REPEAT_MAP[repeatType] ?? ''
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

	const { data: dayData } = useQuery({
		queryKey: ['task', 'calendar', dateStr, dateStr],
		queryFn: () => getTaskCalendar({ from: dateStr, to: dateStr }),
	})

	const queryClient = useQueryClient()
	const [mainTab, setMainTab] = useState<MainTab>('todo')

	const allItems = dayData?.result === 'success' ? dayData.detail.items : []
	const apiTodos = allItems.filter((i) => i.taskType !== 'ROUTINE')
	const apiRoutines = allItems.filter((i) => i.taskType === 'ROUTINE')

	const displayedTodos =
		mainTab === 'todo' ? apiTodos : apiTodos.filter((t) => t.isCompleted)
	const displayedRoutines =
		mainTab === 'todo' ? apiRoutines : apiRoutines.filter((r) => r.isCompleted)

	const invalidate = () =>
		queryClient.invalidateQueries({ queryKey: ['task', 'calendar'] })

	const { mutate: completeItem } = useMutation({
		mutationFn: completeTask,
		onSuccess: (res) => {
			if (res.result === 'success') invalidate()
		},
	})
	const { mutate: uncompleteItem } = useMutation({
		mutationFn: uncompleteTask,
		onSuccess: (res) => {
			if (res.result === 'success') invalidate()
		},
	})

	const { mutate: deleteTask } = useMutation({
		mutationFn: deleteTasks,
		onSuccess: (res) => {
			if (res.result === 'success') invalidate()
		},
	})

	const { mutate: toggleImportant } = useMutation({
		mutationFn: updateSchedule,
		onSuccess: (res) => {
			if (res.result === 'success') invalidate()
		},
	})

	const handleToggleImportant = (todo: CalendarItem) => {
		const date = todo.endDate ?? dateStr
		const endDateTime = todo.isAllDay
			? `${date}T23:59:59`
			: `${date}T${todo.endTime ?? '23:59:59'}`
		toggleImportant({
			taskId: todo.taskId,
			title: todo.title,
			endDateTime,
			isAllDay: todo.isAllDay ?? true,
			isImportant: !todo.isImportant,
		})
	}

	const [editingItem, setEditingItem] = useState<CalendarItem | null>(null)

	const openEdit = (item: CalendarItem) => setEditingItem(item)

	return (
		<>
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
							<div className="flex flex-col items-center gap-1 py-6 text-gray-300">
								<span className="text-2xl font-bold">미정</span>
								<span className="text-xs">
									{mainTab === 'todo'
										? '이 날의 할 일이 없어요'
										: '완료된 할 일이 없어요'}
								</span>
							</div>
						)}
						<Accordion.Root multiple className="flex flex-col gap-3">
							{displayedTodos.map((todo) => (
								<TaskItem
									key={todo.taskId}
									value={String(todo.taskId)}
									title={todo.title}
									isCompleted={todo.isCompleted}
									checkColor="#48CAD9"
									onToggle={() =>
										todo.isCompleted
											? uncompleteItem({ taskId: todo.taskId, date: dateStr })
											: completeItem({ taskId: todo.taskId, date: dateStr })
									}
									onPostpone={() => {}}
									onEdit={() => openEdit(todo)}
									onDelete={() => deleteTask({ taskIds: [todo.taskId] })}
									subtitle={
										!todo.isAllDay && todo.endTime ? (
											<span className="text-xs text-[#B286FD]">
												{todo.endTime.slice(0, 5)}
											</span>
										) : undefined
									}
									trailing={
										<button
											type="button"
											onClick={(e) => {
												e.stopPropagation()
												handleToggleImportant(todo)
											}}
											className="shrink-0"
										>
											<Star
												size={16}
												className={
													todo.isImportant
														? 'fill-[#B286FD] text-[#B286FD]'
														: 'text-gray-200 hover:text-gray-300'
												}
											/>
										</button>
									}
								/>
							))}
						</Accordion.Root>
					</div>

					<div className="w-px bg-gray-100" />

					{/* 루틴 */}
					<div className="flex flex-1 flex-col gap-4">
						<p className="font-semibold text-gray-700">루틴</p>
						{displayedRoutines.length === 0 && (
							<div className="flex flex-col items-center gap-1 py-6 text-gray-300">
								<span className="text-2xl font-bold">미정</span>
								<span className="text-xs">
									{mainTab === 'todo'
										? '이 날의 루틴이 없어요'
										: '완료한 루틴이 없어요'}
								</span>
							</div>
						)}
						<Accordion.Root multiple className="flex flex-col gap-3">
							{displayedRoutines.map((routine) => (
								<TaskItem
									key={routine.taskId}
									value={String(routine.taskId)}
									title={routine.title}
									isCompleted={routine.isCompleted}
									checkColor="#B2F042"
									onToggle={() =>
										routine.isCompleted
											? uncompleteItem({
													taskId: routine.taskId,
													date: dateStr,
												})
											: completeItem({ taskId: routine.taskId, date: dateStr })
									}
									onPostpone={() => {}}
									onEdit={() => openEdit(routine)}
									onDelete={() => deleteTask({ taskIds: [routine.taskId] })}
									subtitle={
										<span className="text-xs text-[#B2A042]">
											{formatRepeat(routine.repeatType)}
										</span>
									}
								/>
							))}
						</Accordion.Root>
					</div>
				</div>

				{/* 입력 도크 */}
				<TaskInputDock dateStr={dateStr} />
			</Card>

			{/* 수정 모달 */}
			<Dialog
				open={editingItem !== null}
				onOpenChange={(open) => {
					if (!open) setEditingItem(null)
				}}
			>
				<DialogPopup className="max-w-md">
					{editingItem && (
						<TaskEditForm
							item={editingItem}
							onSuccess={() => setEditingItem(null)}
							onCancel={() => setEditingItem(null)}
						/>
					)}
				</DialogPopup>
			</Dialog>
		</>
	)
}
