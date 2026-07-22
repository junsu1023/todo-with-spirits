import { z } from 'zod'

export const scheduleSchema = z.object({
	title: z.string().min(1, '제목을 입력해주세요').max(255),
	endDate: z.string(),
	isAllDay: z.boolean(),
	endTime: z.string().optional(),
	isImportant: z.boolean(),
})

export type ScheduleFormValues = z.infer<typeof scheduleSchema>
