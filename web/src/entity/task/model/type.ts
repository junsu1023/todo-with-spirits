export type TaskType = 'SCHEDULE' | 'HABIT' | 'ROUTINE'

export type RepeatType = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'

export type Category =
	| 'FINANCE'
	| 'GROWTH'
	| 'HEALTH'
	| 'HOBBY'
	| 'RELATIONSHIP'
	| 'WORK'
	| 'ETC'
	| 'NONE'

export interface TaskItem {
	taskId: number
	taskType: TaskType
	title: string
	category: Category | null
	memo: string | null
	isAllDay: boolean
	isCompleted: boolean
	isImportant: boolean
	isPublic: boolean
	startDate?: string
	startTime?: string
	endDate?: string
	endTime?: string
	repeatType?: RepeatType
	repeatEndDate?: string
	growthType?: string | null
	growthValue?: number | null
	notificationAt?: string | null
	notificationMinutes?: number | null
	completedAt?: string | null
	createdAt?: string
	updatedAt?: string
}

export interface TaskScheduleDetail {
	totalCount: number
	items: TaskItem[]
}

export interface TaskScheduleParams {
	from?: string
	to?: string
}
