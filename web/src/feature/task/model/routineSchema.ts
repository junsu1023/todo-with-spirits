import { z } from 'zod'

const DAY_OF_WEEK_VALUES = [
	'MONDAY',
	'TUESDAY',
	'WEDNESDAY',
	'THURSDAY',
	'FRIDAY',
	'SATURDAY',
	'SUNDAY',
] as const

export const routineSchema = z
	.object({
		title: z.string().min(1, '제목을 입력해주세요').max(255),
		repeatType: z.enum(['DAILY', 'WEEKLY', 'MONTHLY']),
		repeatDaysOfWeek: z.array(z.enum(DAY_OF_WEEK_VALUES)),
		repeatDaysOfMonth: z.array(z.number().int().min(1).max(31)),
		repeatEndDate: z.string().optional(),
		memo: z.string().max(2000).optional(),
	})
	.superRefine((data, ctx) => {
		if (data.repeatType === 'WEEKLY' && data.repeatDaysOfWeek.length === 0) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				path: ['repeatDaysOfWeek'],
				message: '요일을 선택해주세요',
			})
		}
		if (data.repeatType === 'MONTHLY' && data.repeatDaysOfMonth.length === 0) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				path: ['repeatDaysOfMonth'],
				message: '날짜를 선택해주세요',
			})
		}
	})

export type RoutineFormValues = z.infer<typeof routineSchema>
