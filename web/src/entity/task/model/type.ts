export type TaskType = 'SCHEDULE' | 'HABIT' | 'ROUTINE'

export type RepeatType = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'

// 기록(Record) API에서 사용. 할 일 API에서는 카테고리가 제거됨
export type Category =
	| 'FINANCE'
	| 'GROWTH'
	| 'HEALTH'
	| 'HOBBY'
	| 'RELATIONSHIP'
	| 'WORK'
	| 'ETC'
	| 'NONE'

// 카테고리·공개 상태·메모·시작일시는 할 일 API 요청/응답에서 제거됨 (back DTO, android b143473 기준)
// todo: 백엔드에서 다시 제공하면 주석 해제
export interface TaskItem {
	taskId: number
	taskType: TaskType
	title: string
	// category: Category | null
	// memo: string | null
	isAllDay: boolean
	isCompleted: boolean
	isImportant: boolean
	// isPublic: boolean
	// startDate?: string
	// startTime?: string
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

export interface CalendarItem {
	taskId: number
	taskType: TaskType
	title: string
	// category: Category | null
	// memo: string | null
	isAllDay?: boolean
	isCompleted: boolean
	isImportant: boolean
	// isPublic: boolean
	occurrenceDate: string
	endDate?: string | null
	endTime?: string | null
	repeatType?: RepeatType
	repeatEndDate?: string | null
	repeatDaysOfWeek?: string[]
	repeatDaysOfMonth?: number[]
	excludeHoliday?: boolean
	growthType?: string | null
	growthValue?: number | null
	notificationAt?: string | null
	notificationMinutes?: number | null
	completedAt?: string | null
	createdAt?: string
	updatedAt?: string
}

export interface CalendarDetail {
	totalCount: number
	completedCount: number
	completedRoutineCount: number
	completedScheduleCount: number
	inCompleteCount: number
	routineCount: number
	scheduleCount: number
	items: CalendarItem[]
}
