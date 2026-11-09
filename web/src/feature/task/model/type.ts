import type { RepeatType } from '@/entity/task/model/type'

export type NotificationType =
	| 'NONE'
	| 'TEN_MINUTES'
	| 'THIRTY_MINUTES'
	| 'ONE_HOUR'
// 백엔드 NotificationType에 없음 (NONE, TEN_MINUTES, THIRTY_MINUTES, ONE_HOUR)
// 	| 'ONE_DAY'

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
	// 카테고리·공개 상태·메모는 API에서 제거됨 (entity/task/model/type.ts 참고)
	// category: Category
	// memo: string | null
	isCompleted: boolean
	isImportant: boolean
	// isPublic: boolean
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

// 카테고리·공개 상태·메모·시작일시는 API에서 제거됨 (entity/task/model/type.ts 참고)
export interface CreateScheduleRequest {
	title: string
	isAllDay: boolean
	// startDatetime: string
	endDateTime: string
	isImportant?: boolean
	notificationType?: NotificationType
	// category?: Category
	// isPublic?: boolean
	// memo?: string
}

export interface UpdateScheduleRequest {
	taskId: number
	title: string
	endDateTime: string
	isAllDay: boolean
	notificationType?: NotificationType
	// category?: Category
	// isPublic?: boolean
	isImportant?: boolean
	// memo?: string
}

export interface UpdateRoutineRequest {
	taskId: number
	title: string
	repeatType: Exclude<RepeatType, 'YEARLY'>
	// category?: Category
	repeatEndDate?: string
	repeatDaysOfWeek?: DayOfWeek[]
	repeatDaysOfMonth?: number[]
	notificationType?: NotificationType
	// isPublic?: boolean
	excludeHoliday?: boolean
	// memo?: string
}

export interface CreateRoutineRequest {
	title: string
	repeatType: Exclude<RepeatType, 'YEARLY'>
	repeatEndDate?: string
	repeatDaysOfWeek?: DayOfWeek[]
	repeatDaysOfMonth?: number[]
	// 백엔드 필드명은 notificationType (기존 notification은 오타)
	notificationType?: NotificationType
	// isPublic?: boolean
	// memo?: string
}

export interface PostponeTaskRequest {
	taskId: number
	originalDate?: string
	newDate?: string | null
	newTime?: string | null
}

export interface PostponeTaskResponse {
	taskId: number
	originalDate: string
	postponedDate: string
	postponedTime: string
	postponeCount: number
	maxPostponeCount: number
	remainingPostponeCount: number
}

export interface DeleteTaskRequest {
	taskIds: number[]
}

export interface DeleteTaskResponse {
	deletedCount: number
}

// 완료 시 지급된 재화. 이미 지급된 적 있으면 currencyEarned = 0
export interface TaskCompletionResponse {
	currencyEarned: number
	balance: number
}

// ─── Plan 화면 표시용 ─────────────────────────────────────────────────────────

export type ItemType = 'todo' | 'routine'

export interface PlanItem {
	id: number
	type: ItemType
	title: string
	starred: boolean
	completed: boolean
	dday: number
	dateLabel: string
	date?: string
	time?: string
	// 카테고리·공개 상태·메모는 API에서 제거됨
	// category?: string
	// isPublic?: boolean
	repeatType?: 'DAILY' | 'WEEKLY' | 'MONTHLY'
	repeatDaysOfWeek?: string[]
	repeatDaysOfMonth?: number[]
	excludeHolidays?: boolean
	// memo?: string
	// tags: string[]
}
