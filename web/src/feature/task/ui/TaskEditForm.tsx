import { useMutation, useQueryClient } from '@tanstack/react-query'
import { X } from 'lucide-react'
import { useState } from 'react'
import type { CalendarItem, Category } from '@/entity/task/model/type'
import { updateRoutine, updateSchedule } from '@/feature/task/api/mutate'
import type { DayOfWeek } from '@/feature/task/model/type'
import { DropdownSelect } from '@/shared/ui/dropdown-select'

// ─── Constants ────────────────────────────────────────────────────────────────

const CATEGORY_OPTIONS: { label: string; value: Category }[] = [
	{ label: '없음', value: 'NONE' },
	{ label: '업무/커리어', value: 'WORK' },
	{ label: '인간관계/약속', value: 'RELATIONSHIP' },
	{ label: '자기계발', value: 'GROWTH' },
	{ label: '건강', value: 'HEALTH' },
	{ label: '취미', value: 'HOBBY' },
	{ label: '금융', value: 'FINANCE' },
	{ label: '기타', value: 'ETC' },
]

const CATEGORY_LABELS = CATEGORY_OPTIONS.map((o) => o.label)
const labelToCategory = (label: string): Category =>
	CATEGORY_OPTIONS.find((o) => o.label === label)?.value ?? 'NONE'
const categoryToLabel = (cat: Category): string =>
	CATEGORY_OPTIONS.find((o) => o.value === cat)?.label ?? '없음'

const PUBLIC_OPTIONS = ['비공개', '공개']

const DAY_OPTIONS: { label: string; value: DayOfWeek }[] = [
	{ label: '일', value: 'SUNDAY' },
	{ label: '월', value: 'MONDAY' },
	{ label: '화', value: 'TUESDAY' },
	{ label: '수', value: 'WEDNESDAY' },
	{ label: '목', value: 'THURSDAY' },
	{ label: '금', value: 'FRIDAY' },
	{ label: '토', value: 'SATURDAY' },
]

// ─── Sub-components ───────────────────────────────────────────────────────────

function Toggle({
	checked,
	onChange,
}: {
	checked: boolean
	onChange: () => void
}) {
	return (
		<button
			type="button"
			role="switch"
			aria-checked={checked}
			onClick={onChange}
			className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ${
				checked ? 'bg-[#B286FD]' : 'bg-gray-200'
			}`}
		>
			<span
				aria-hidden="true"
				className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out ${
					checked ? 'translate-x-5' : 'translate-x-0'
				}`}
			/>
		</button>
	)
}

function Checkbox({
	checked,
	onChange,
}: {
	checked: boolean
	onChange: () => void
}) {
	return (
		<button
			type="button"
			onClick={onChange}
			className={`flex h-5 w-5 items-center justify-center rounded-md transition-colors ${
				checked ? 'bg-[#B286FD]' : 'border-2 border-gray-300 bg-white'
			}`}
		>
			{checked && (
				<svg
					viewBox="0 0 10 8"
					fill="none"
					className="h-3 w-3"
					aria-hidden="true"
				>
					<title>checked</title>
					<path
						d="M1 4L3.5 6.5L9 1"
						stroke="white"
						strokeWidth="1.5"
						strokeLinecap="round"
						strokeLinejoin="round"
					/>
				</svg>
			)}
		</button>
	)
}

function FormRow({
	label,
	last = false,
	children,
}: {
	label: string
	last?: boolean
	children: React.ReactNode
}) {
	return (
		<div
			className={`flex items-center justify-between py-3 ${last ? '' : 'border-b border-gray-100'}`}
		>
			<span className="text-sm text-gray-500">{label}</span>
			{children}
		</div>
	)
}

// ─── TaskEditForm ─────────────────────────────────────────────────────────────

interface TaskEditFormProps {
	item: CalendarItem
	dateStr: string
	onSuccess: () => void
	onCancel: () => void
}

function todayStr() {
	const d = new Date()
	return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export function TaskEditForm({
	item,
	dateStr,
	onSuccess,
	onCancel,
}: TaskEditFormProps) {
	const queryClient = useQueryClient()

	const isRoutine = item.taskType === 'ROUTINE'

	// common
	const [title, setTitle] = useState(item.title)
	const [category, setCategory] = useState<Category>(item.category ?? 'NONE')
	const [isPublic, setIsPublic] = useState(item.isPublic ? '공개' : '비공개')
	const [memo, setMemo] = useState(item.memo ?? '')

	// schedule only
	const [isImportant, setIsImportant] = useState(item.isImportant)
	const [date, setDate] = useState(item.endDate ?? todayStr())
	const [timeEnabled, setTimeEnabled] = useState(item.isAllDay === false)
	const [time, setTime] = useState(item.endTime?.slice(0, 5) ?? '00:00')

	// routine only
	const [repeatType, setRepeatType] = useState<'DAILY' | 'WEEKLY' | 'MONTHLY'>(
		item.repeatType === 'DAILY' ||
			item.repeatType === 'WEEKLY' ||
			item.repeatType === 'MONTHLY'
			? item.repeatType
			: 'DAILY',
	)
	const [repeatDaysOfWeek, setRepeatDaysOfWeek] = useState<DayOfWeek[]>([])
	const [repeatDaysOfMonth, setRepeatDaysOfMonth] = useState<number[]>([])
	const [excludeHolidays, setExcludeHolidays] = useState(false)

	const invalidate = () => {
		queryClient.invalidateQueries({
			queryKey: ['task', 'calendar'],
		})
	}

	const { mutate: saveSchedule, isPending: isSavingSchedule } = useMutation({
		mutationFn: updateSchedule,
		onSuccess: (res) => {
			if (res.result === 'success') {
				invalidate()
				onSuccess()
			}
		},
	})

	const { mutate: saveRoutine, isPending: isSavingRoutine } = useMutation({
		mutationFn: updateRoutine,
		onSuccess: (res) => {
			if (res.result === 'success') {
				invalidate()
				onSuccess()
			}
		},
	})

	const isPending = isSavingSchedule || isSavingRoutine

	const toggleDay = (day: DayOfWeek) =>
		setRepeatDaysOfWeek((prev) =>
			prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day],
		)

	const toggleDate = (d: number) =>
		setRepeatDaysOfMonth((prev) =>
			prev.includes(d) ? prev.filter((n) => n !== d) : [...prev, d],
		)

	const handleSave = () => {
		if (!title.trim()) return

		if (isRoutine) {
			if (repeatType === 'WEEKLY' && repeatDaysOfWeek.length === 0) return
			if (repeatType === 'MONTHLY' && repeatDaysOfMonth.length === 0) return
			saveRoutine({
				taskId: item.taskId,
				title: title.trim(),
				repeatType,
				...(repeatType === 'WEEKLY' && { repeatDaysOfWeek }),
				...(repeatType === 'MONTHLY' && { repeatDaysOfMonth }),
				excludeHoliday: excludeHolidays,
				category,
				isPublic: isPublic === '공개',
				memo: memo.trim() || undefined,
			})
		} else {
			const endDateTime = timeEnabled
				? `${date}T${time}:00`
				: `${date}T23:59:59`
			saveSchedule({
				taskId: item.taskId,
				title: title.trim(),
				endDateTime,
				isAllDay: !timeEnabled,
				isImportant,
				category,
				isPublic: isPublic === '공개',
				memo: memo.trim() || undefined,
			})
		}
	}

	return (
		<div className="flex flex-col gap-4">
			{/* 헤더 */}
			<div className="flex items-center justify-between">
				<span className="font-semibold text-gray-800">
					{isRoutine ? '루틴 수정' : '할 일 수정'}
				</span>
				<button
					type="button"
					onClick={onCancel}
					className="flex h-7 w-7 items-center justify-center rounded-full text-gray-400 hover:bg-gray-100"
				>
					<X size={16} />
				</button>
			</div>

			{/* 제목 */}
			<input
				type="text"
				value={title}
				onChange={(e) => setTitle(e.target.value)}
				placeholder="제목을 입력하세요"
				className="border-b border-gray-200 pb-2 text-base font-medium text-gray-800 outline-none placeholder:text-gray-300 focus:border-[#B286FD]"
				onKeyDown={(e) => {
					if (e.key === 'Enter') handleSave()
				}}
				autoFocus
			/>

			{/* 필드 */}
			<div className="flex flex-col rounded-xl bg-gray-50 px-4">
				{isRoutine ? (
					<>
						{/* 반복 */}
						<FormRow label="반복">
							<div className="flex gap-1.5">
								{(['DAILY', 'WEEKLY', 'MONTHLY'] as const).map((t) => (
									<button
										key={t}
										type="button"
										onClick={() => setRepeatType(t)}
										className={`rounded-full px-3 py-1 text-sm font-medium transition-colors ${
											repeatType === t
												? 'bg-[#B286FD] text-white'
												: 'border border-gray-200 text-gray-500 hover:border-[#B286FD] hover:text-[#B286FD]'
										}`}
									>
										{t === 'DAILY' ? '매일' : t === 'WEEKLY' ? '매주' : '매월'}
									</button>
								))}
							</div>
						</FormRow>

						{/* 요일 선택 (매주) */}
						{repeatType === 'WEEKLY' && (
							<div className="flex flex-wrap gap-2 border-b border-gray-100 py-3">
								{DAY_OPTIONS.map((day) => (
									<button
										key={day.value}
										type="button"
										onClick={() => toggleDay(day.value)}
										className={`flex h-8 w-8 items-center justify-center rounded-full text-sm font-medium transition-colors ${
											repeatDaysOfWeek.includes(day.value)
												? 'bg-[#B286FD] text-white'
												: 'border border-gray-200 text-gray-500 hover:border-[#B286FD] hover:text-[#B286FD]'
										}`}
									>
										{day.label}
									</button>
								))}
							</div>
						)}

						{/* 날짜 선택 (매월) */}
						{repeatType === 'MONTHLY' && (
							<div className="grid grid-cols-7 gap-1 border-b border-gray-100 py-3">
								{Array.from({ length: 31 }, (_, i) => i + 1).map((d) => (
									<button
										key={d}
										type="button"
										onClick={() => toggleDate(d)}
										className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-medium transition-colors ${
											repeatDaysOfMonth.includes(d)
												? 'bg-[#B286FD] text-white'
												: 'border border-gray-100 text-gray-500 hover:border-[#B286FD] hover:text-[#B286FD]'
										}`}
									>
										{d}
									</button>
								))}
							</div>
						)}

						{/* 공휴일 제외 */}
						<div className="flex items-center gap-2 border-b border-gray-100 py-3">
							<Checkbox
								checked={excludeHolidays}
								onChange={() => setExcludeHolidays((v) => !v)}
							/>
							<span className="text-sm text-gray-500">공휴일 제외</span>
						</div>

						<FormRow label="카테고리">
							<DropdownSelect
								options={CATEGORY_LABELS}
								value={categoryToLabel(category)}
								onChange={(label) => setCategory(labelToCategory(label))}
							/>
						</FormRow>

						<FormRow label="공개 상태" last>
							<DropdownSelect
								options={PUBLIC_OPTIONS}
								value={isPublic}
								onChange={setIsPublic}
							/>
						</FormRow>
					</>
				) : (
					<>
						<FormRow label="중요">
							<Checkbox
								checked={isImportant}
								onChange={() => setIsImportant((v) => !v)}
							/>
						</FormRow>

						<FormRow label="날짜">
							<input
								type="date"
								value={date}
								onChange={(e) => setDate(e.target.value)}
								className="rounded-lg border border-gray-200 bg-white px-2 py-1 text-sm text-gray-700 outline-none focus:border-[#B286FD]"
							/>
						</FormRow>

						<FormRow label="시간">
							<div className="flex items-center gap-3">
								{timeEnabled && (
									<input
										type="time"
										value={time}
										onChange={(e) => setTime(e.target.value)}
										className="rounded-lg border border-gray-200 bg-white px-2 py-1 text-sm text-gray-700 outline-none focus:border-[#B286FD]"
									/>
								)}
								<Toggle
									checked={timeEnabled}
									onChange={() => setTimeEnabled((v) => !v)}
								/>
							</div>
						</FormRow>

						<FormRow label="카테고리">
							<DropdownSelect
								options={CATEGORY_LABELS}
								value={categoryToLabel(category)}
								onChange={(label) => setCategory(labelToCategory(label))}
							/>
						</FormRow>

						<FormRow label="공개 상태" last>
							<DropdownSelect
								options={PUBLIC_OPTIONS}
								value={isPublic}
								onChange={setIsPublic}
							/>
						</FormRow>
					</>
				)}
			</div>

			{/* 메모 */}
			<textarea
				value={memo}
				onChange={(e) => setMemo(e.target.value)}
				placeholder="메모"
				rows={3}
				className="resize-none rounded-xl bg-gray-50 p-3 text-sm text-gray-700 outline-none placeholder:text-gray-300 focus:ring-1 focus:ring-[#B286FD]"
			/>

			{/* 버튼 */}
			<div className="flex gap-2">
				<button
					type="button"
					onClick={onCancel}
					className="flex-1 rounded-xl border border-gray-200 py-3 text-sm font-medium text-gray-600 transition-colors hover:bg-gray-50"
				>
					취소
				</button>
				<button
					type="button"
					onClick={handleSave}
					disabled={isPending || !title.trim()}
					className="flex-1 rounded-xl bg-[#B286FD] py-3 text-sm font-semibold text-white transition-opacity disabled:opacity-60"
				>
					{isPending ? '저장 중...' : '수정하기'}
				</button>
			</div>
		</div>
	)
}
