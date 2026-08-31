import { z } from 'zod'

export const scheduleSchema = z.object({
	title: z.string().min(1, '제목을 입력해주세요').max(255),
	endDate: z.string(),
	isAllDay: z.boolean(),
	endTime: z.string().optional(),
	isImportant: z.boolean(),
})

export type ScheduleFormValues = z.infer<typeof scheduleSchema>

export const updateScheduleSchema = z.object({
	title: z.string().min(1, '제목을 입력해주세요').max(255),
	endDate: z.string().min(1, '날짜를 입력해주세요'),
	isAllDay: z.boolean(),
	endTime: z.string().optional(),
	isImportant: z.boolean().optional(),
	category: z.string().optional(),
	isPublic: z.boolean().optional(),
	memo: z.string().max(2000).optional(),
})

export type UpdateScheduleFormValues = z.infer<typeof updateScheduleSchema>
