import type { Category, RepeatType } from '@/entity/task/model/type'

export type NotificationType =
	| 'NONE'
	| 'TEN_MINUTES'
	| 'THIRTY_MINUTES'
	| 'ONE_HOUR'
	| 'ONE_DAY'

export type DayOfWeek =
	| 'MONDAY'
	| 'TUESDAY'
	| 'WEDNESDAY'
	| 'THURSDAY'
	| 'FRIDAY'
	| 'SATURDAY'
	| 'SUNDAY'

export interface RoutineDetail {
	taskId: number
	taskType: 'ROUTINE'
	title: string
	category: Category
	memo: string | null
	isCompleted: boolean
	isImportant: boolean
	isPublic: boolean
	excludeHoliday: boolean
	repeatType: RepeatType
	repeatEndDate: string | null
	repeatDaysOfWeek: DayOfWeek[]
	repeatDaysOfMonth: number[]
	growthType: string | null
	growthValue: number
	notificationAt: string | null
	notificationMinutes: number | null
	completedAt: string | null
	createdAt: string
	updatedAt: string
}

export interface CreateScheduleRequest {
	title: string
	isAllDay: boolean
	startDatetime: string
	endDateTime: string
	isImportant?: boolean
	notificationType?: NotificationType
	category?: Category
	isPublic?: boolean
	memo?: string
}

export interface UpdateScheduleRequest {
	taskId: number
	title: string
	endDateTime: string
	isAllDay: boolean
	notificationType?: NotificationType
	category?: Category
	isPublic?: boolean
	isImportant?: boolean
	memo?: string
}

export interface UpdateRoutineRequest {
	taskId: number
	title: string
	repeatType: Exclude<RepeatType, 'YEARLY'>
	category?: Category
	repeatEndDate?: string
	repeatDaysOfWeek?: DayOfWeek[]
	repeatDaysOfMonth?: number[]
	notificationType?: NotificationType
	isPublic?: boolean
	excludeHoliday?: boolean
	memo?: string
}

export interface CreateRoutineRequest {
	title: string
	repeatType: Exclude<RepeatType, 'YEARLY'>
	repeatEndDate?: string
	repeatDaysOfWeek?: DayOfWeek[]
	repeatDaysOfMonth?: number[]
	notification?: NotificationType
	isPublic?: boolean
	memo?: string
}

export interface DeleteTaskRequest {
	taskIds: number[]
}

export interface DeleteTaskResponse {
	deletedCount: number
}
