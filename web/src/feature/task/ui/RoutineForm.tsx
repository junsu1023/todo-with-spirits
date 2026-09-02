import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { createRoutine } from '@/feature/task/api/mutate'
import { type RoutineFormValues, routineSchema } from '../model/routineSchema'
import type { DayOfWeek } from '../model/type'

const DAYS: { label: string; value: DayOfWeek }[] = [
	{ label: '일', value: 'SUNDAY' },
	{ label: '월', value: 'MONDAY' },
	{ label: '화', value: 'TUESDAY' },
	{ label: '수', value: 'WEDNESDAY' },
	{ label: '목', value: 'THURSDAY' },
	{ label: '금', value: 'FRIDAY' },
	{ label: '토', value: 'SATURDAY' },
]

export function RoutineForm() {
	const queryClient = useQueryClient()

	const {
		register,
		handleSubmit,
		setValue,
		watch,
		reset,
		formState: { errors },
	} = useForm<RoutineFormValues>({
		resolver: zodResolver(routineSchema),
		defaultValues: {
			title: '',
			repeatType: 'DAILY',
			repeatDaysOfWeek: [],
			repeatDaysOfMonth: [],
		},
	})

	const { mutate, isPending } = useMutation({
		mutationFn: createRoutine,
		onSuccess: (res) => {
			if (res.result !== 'success') return
			queryClient.invalidateQueries({ queryKey: ['task', 'calendar'] })
			reset()
		},
	})

	const repeatType = watch('repeatType')
	const repeatDaysOfWeek = watch('repeatDaysOfWeek')
	const repeatDaysOfMonth = watch('repeatDaysOfMonth')

	const toggleDay = (day: DayOfWeek) => {
		const next = repeatDaysOfWeek.includes(day)
			? repeatDaysOfWeek.filter((d) => d !== day)
			: [...repeatDaysOfWeek, day]
		setValue('repeatDaysOfWeek', next, { shouldValidate: true })
	}

	const toggleDate = (date: number) => {
		const next = repeatDaysOfMonth.includes(date)
			? repeatDaysOfMonth.filter((d) => d !== date)
			: [...repeatDaysOfMonth, date]
		setValue('repeatDaysOfMonth', next, { shouldValidate: true })
	}

	const onSubmit = (values: RoutineFormValues) => {
		mutate({
			title: values.title,
			repeatType: values.repeatType,
			...(values.repeatDaysOfWeek.length > 0 && {
				repeatDaysOfWeek: values.repeatDaysOfWeek,
			}),
			...(values.repeatDaysOfMonth.length > 0 && {
				repeatDaysOfMonth: values.repeatDaysOfMonth,
			}),
			...(values.repeatEndDate && { repeatEndDate: values.repeatEndDate }),
			...(values.memo && { memo: values.memo }),
		})
	}

	return (
		<form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
			{/* 제목 입력 */}
			<div className="flex items-center gap-2 rounded-xl bg-white px-4 py-3">
				<input
					type="text"
					placeholder="루틴 이름을 입력하세요..."
					{...register('title')}
					className="flex-1 bg-transparent text-sm text-gray-700 outline-none placeholder:text-gray-300"
				/>
				<button
					type="submit"
					disabled={isPending}
					className="shrink-0 rounded-lg bg-[#B286FD] px-4 py-1.5 text-sm font-medium text-white disabled:opacity-60"
				>
					추가
				</button>
			</div>
			{errors.title && (
				<p className="px-1 text-xs text-red-400">{errors.title.message}</p>
			)}

			{/* 반복 설정 */}
			<div className="flex flex-col gap-3 rounded-xl bg-white px-4 py-3">
				<div className="flex items-center justify-between">
					<span className="text-sm text-gray-500">반복</span>
					<div className="flex gap-1.5">
						{(['DAILY', 'WEEKLY', 'MONTHLY'] as const).map((type) => (
							<button
								key={type}
								type="button"
								onClick={() =>
									setValue('repeatType', type, { shouldValidate: true })
								}
								className={`rounded-full px-3 py-1 text-sm font-medium transition-colors ${
									repeatType === type
										? 'bg-[#B286FD] text-white'
										: 'border border-gray-200 text-gray-500 hover:border-[#B286FD] hover:text-[#B286FD]'
								}`}
							>
								{type === 'DAILY'
									? '매일'
									: type === 'WEEKLY'
										? '매주'
										: '매월'}
							</button>
						))}
					</div>
				</div>

				{repeatType === 'WEEKLY' && (
					<>
						<div className="flex gap-2">
							{DAYS.map((day) => (
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
						{errors.repeatDaysOfWeek && (
							<p className="text-xs text-red-400">요일을 선택해주세요</p>
						)}
					</>
				)}

				{repeatType === 'MONTHLY' && (
					<>
						<div className="grid grid-cols-7 gap-1">
							{Array.from({ length: 31 }, (_, i) => i + 1).map((date) => (
								<button
									key={date}
									type="button"
									onClick={() => toggleDate(date)}
									className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-medium transition-colors ${
										repeatDaysOfMonth.includes(date)
											? 'bg-[#B286FD] text-white'
											: 'border border-gray-100 text-gray-500 hover:border-[#B286FD] hover:text-[#B286FD]'
									}`}
								>
									{date}
								</button>
							))}
						</div>
						{errors.repeatDaysOfMonth && (
							<p className="text-xs text-red-400">날짜를 선택해주세요</p>
						)}
					</>
				)}
			</div>
		</form>
	)
}
