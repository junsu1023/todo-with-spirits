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

// 할 일·루틴 API가 하나로 합쳐져 아래 TaskRequest, TaskResponse로 바뀌었다 (서버 dto/task)
// export interface RoutineDetail {
// 	taskId: number
// 	taskType: 'ROUTINE'
// 	title: string
// 	category: Category
// 	memo: string | null
// 	isCompleted: boolean
// 	isImportant: boolean
// 	isPublic: boolean
// 	excludeHoliday: boolean
// 	repeatType: RepeatType
// 	repeatEndDate: string | null
// 	repeatDaysOfWeek: DayOfWeek[]
// 	repeatDaysOfMonth: number[]
// 	growthType: string | null
// 	growthValue: number
// 	notificationAt: string | null
// 	notificationMinutes: number | null
// 	completedAt: string | null
// 	createdAt: string
// 	updatedAt: string
// }

// export interface CreateScheduleRequest {
// 	title: string
// 	isAllDay: boolean
// 	startDatetime: string
// 	endDateTime: string
// 	isImportant?: boolean
// 	notificationType?: NotificationType
// 	category?: Category
// 	isPublic?: boolean
// 	memo?: string
// }

// export interface UpdateScheduleRequest {
// 	taskId: number
// 	title: string
// 	endDateTime: string
// 	isAllDay: boolean
// 	notificationType?: NotificationType
// 	category?: Category
// 	isPublic?: boolean
// 	isImportant?: boolean
// 	memo?: string
// }

// export interface UpdateRoutineRequest {
// 	taskId: number
// 	title: string
// 	repeatType: Exclude<RepeatType, 'YEARLY'>
// 	category?: Category
// 	repeatEndDate?: string
// 	repeatDaysOfWeek?: DayOfWeek[]
// 	repeatDaysOfMonth?: number[]
// 	notificationType?: NotificationType
// 	isPublic?: boolean
// 	excludeHoliday?: boolean
// 	memo?: string
// }

// export interface CreateRoutineRequest {
// 	title: string
// 	repeatType: Exclude<RepeatType, 'YEARLY'>
// 	repeatEndDate?: string
// 	repeatDaysOfWeek?: DayOfWeek[]
// 	repeatDaysOfMonth?: number[]
// 	notification?: NotificationType
// 	isPublic?: boolean
// 	memo?: string
// }

// 서버 RepeatType 중 루틴으로 고를 수 있는 값 (서버 ALLOWED_REPEAT에서 NONE 제외)
export type RoutineRepeatType = Exclude<RepeatType, 'NONE' | 'YEARLY'>

/**
 * 할 일·루틴 생성/수정 공통 요청 (POST api/task, PATCH api/task/{taskId}). 서버 TaskRequest.
 * repeatType이 없거나 NONE이면 할 일, DAILY/WEEKLY/MONTHLY면 루틴.
 * 수정 시 endDateTime만 비우면 기존 날짜·시각이 유지되고,
 * 나머지 값은 보내지 않으면 기본값(false, NONE, null)으로 지워진다 → model/update.ts로 기존 값을 채워 보낸다.
 */
export interface TaskRequest {
	title: string
	// 할 일: 마감 일시 / 루틴: 반복 시작일 + 시각. 생성 시 없으면 오늘 하루 종일
	endDateTime?: string
	isAllDay?: boolean
	isImportant?: boolean
	notificationType?: NotificationType
	repeatType?: 'NONE' | RoutineRepeatType
	repeatEndDate?: string
	// WEEKLY 필수
	repeatDaysOfWeek?: DayOfWeek[]
	// MONTHLY 필수 (1 ~ 31)
	repeatDaysOfMonth?: number[]
	// 루틴일 때만 적용
	excludeHoliday?: boolean
	// todo: 아래는 서버 TaskRequest에 없는 값 (보내도 무시됨). 정리 여부 확인 후 처리
	startDatetime?: string
	category?: Category
	isPublic?: boolean
	memo?: string
}

export interface UpdateTaskRequest extends TaskRequest {
	taskId: number
}

// 생성/수정 응답 (서버 TaskResponse)
export interface TaskResponse {
	taskId: number
	title: string
	// 할 일: 해당 날짜 / 루틴: 반복 시작일
	startDate: string
	endTime: string | null
	isAllDay: boolean
	isImportant: boolean
	repeatType: RepeatType
	repeatEndDate: string | null
	repeatDaysOfWeek: DayOfWeek[]
	repeatDaysOfMonth: number[]
	excludeHoliday: boolean
	notificationMinutes: number | null
	notificationAt: string | null
	// 게임(정령 성장) 값. 기획 확정 전이라 받기만 하고 화면에서는 쓰지 않는다
	growthValue: number
	isCompleted: boolean
	completedAt: string | null
	createdAt: string
	updatedAt: string
}

// 완료 응답. 이미 지급된 적 있으면 currencyEarned = 0
// 게임(재화) 값이라 기획 확정 전까지 받기만 하고 화면에서는 쓰지 않는다
export interface TaskCompletionResponse {
	currencyEarned: number
	balance: number
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
	// null이면 무제한 (서버 TaskPostponeResponse)
	maxPostponeCount: number | null
	remainingPostponeCount: number | null
}

export interface DeleteTaskRequest {
	taskIds: number[]
}

export interface DeleteTaskResponse {
	deletedCount: number
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
	category?: string
	isPublic?: boolean
	repeatType?: 'DAILY' | 'WEEKLY' | 'MONTHLY'
	repeatDaysOfWeek?: string[]
	repeatDaysOfMonth?: number[]
	excludeHolidays?: boolean
	memo?: string
	tags: string[]
}
