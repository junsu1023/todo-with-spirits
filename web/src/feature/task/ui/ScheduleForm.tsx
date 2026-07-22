import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Clock, Star, X } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { createSchedule } from '@/feature/task/api/mutate'
import { Popover, PopoverContent, PopoverTrigger } from '@/shared/ui/popover'
import {
	type ScheduleFormValues,
	scheduleSchema,
} from '../model/scheduleSchema'

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

function formatDateBadge(date: string, time?: string) {
	const d = new Date(date)
	const label = `${d.getMonth() + 1}.${d.getDate()}`
	return time ? `${label} ${time}` : label
}

interface ScheduleFormProps {
	dateStr: string
}

export function ScheduleForm({ dateStr }: ScheduleFormProps) {
	const queryClient = useQueryClient()

	const { register, handleSubmit, setValue, watch, reset } =
		useForm<ScheduleFormValues>({
			resolver: zodResolver(scheduleSchema),
			defaultValues: {
				title: '',
				endDate: '',
				isAllDay: true,
				endTime: '',
				isImportant: false,
			},
		})

	const { mutate, isPending } = useMutation({
		mutationFn: createSchedule,
		onSuccess: (res) => {
			if (res.result !== 'success') return
			queryClient.invalidateQueries({ queryKey: ['task', 'schedule', dateStr] })
			reset()
		},
	})

	const endDate = watch('endDate')
	const isAllDay = watch('isAllDay')
	const isImportant = watch('isImportant')
	const endTime = watch('endTime')

	const hasDate = endDate !== ''

	const onSubmit = (values: ScheduleFormValues) => {
		const resolvedDate = values.endDate || dateStr
		const endDateTime = values.isAllDay
			? `${resolvedDate}T23:59:59`
			: `${resolvedDate}T${values.endTime ?? '00:00:00'}`

		mutate({
			title: values.title,
			isAllDay: values.isAllDay,
			startDatetime: `${resolvedDate}T00:00:00`,
			endDateTime,
			isImportant: values.isImportant,
		})
	}

	return (
		<form
			onSubmit={handleSubmit(onSubmit)}
			className="flex items-center gap-2 rounded-xl bg-white px-4 py-3"
		>
			<input
				type="text"
				placeholder="새로운 할 일을 입력하세요..."
				{...register('title')}
				className="flex-1 bg-transparent text-sm text-gray-700 outline-none placeholder:text-gray-300"
			/>

			{/* 날짜 뱃지 */}
			{hasDate && (
				<span className="flex shrink-0 items-center gap-1 rounded-full bg-[#F4ECFF] px-2.5 py-1 text-xs text-[#B286FD]">
					{formatDateBadge(endDate, !isAllDay ? endTime : undefined)}
					<button
						type="button"
						onClick={() => {
							setValue('endDate', '')
							setValue('endTime', '')
							setValue('isAllDay', true)
						}}
						className="hover:opacity-60"
					>
						<X size={10} />
					</button>
				</span>
			)}

			{/* 날짜/시간 Popover */}
			<Popover>
				<PopoverTrigger
					className="shrink-0 hover:opacity-70"
					aria-label="날짜 및 시간 설정"
				>
					<Clock
						size={17}
						className={hasDate ? 'text-[#B286FD]' : 'text-gray-300'}
					/>
				</PopoverTrigger>
				<PopoverContent side="top" align="end" className="w-64 p-4">
					<div className="flex flex-col gap-4">
						<div className="flex items-center justify-between gap-3">
							<span className="shrink-0 text-sm text-gray-500">날짜</span>
							<input
								type="date"
								value={endDate}
								onChange={(e) => setValue('endDate', e.target.value)}
								className="flex-1 rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-700 outline-none focus:border-[#B286FD]"
							/>
						</div>
						<div className="flex items-center justify-between">
							<span className="text-sm text-gray-500">시간</span>
							<Toggle
								checked={!isAllDay}
								onChange={() => setValue('isAllDay', !isAllDay)}
							/>
						</div>
						{!isAllDay && (
							<input
								type="time"
								value={endTime ?? ''}
								onChange={(e) => setValue('endTime', e.target.value)}
								className="w-full rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-700 outline-none focus:border-[#B286FD]"
							/>
						)}
					</div>
				</PopoverContent>
			</Popover>

			{/* 중요 표시 */}
			<button
				type="button"
				onClick={() => setValue('isImportant', !isImportant)}
				className="shrink-0"
			>
				<Star
					size={18}
					className={
						isImportant ? 'fill-[#B286FD] text-[#B286FD]' : 'text-gray-300'
					}
				/>
			</button>

			<button
				type="submit"
				disabled={isPending}
				className="shrink-0 rounded-lg bg-[#B286FD] px-4 py-1.5 text-sm font-medium text-white disabled:opacity-60"
			>
				추가
			</button>
		</form>
	)
}
