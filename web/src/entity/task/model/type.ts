// 서버 응답에서 taskType이 빠졌다 (할 일·루틴 API 통합). 할 일/루틴 구분은 repeatType으로 한다 (./repeat.ts)
// todo: 기록(record) 타입이 아직 사용 중이라 남겨 둠. 기록 API 정리 때 함께 정리
export type TaskType = 'SCHEDULE' | 'HABIT' | 'ROUTINE'

// 서버 enum. NONE이면 할 일(단발성), 그 외는 루틴(반복). YEARLY는 서버가 생성·수정을 막는다
export type RepeatType = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'

export type Category =
	| 'FINANCE'
	| 'GROWTH'
	| 'HEALTH'
	| 'HOBBY'
	| 'RELATIONSHIP'
	| 'WORK'
	| 'ETC'
	| 'NONE'

// GET api/task/schedule, api/task/routine 응답. 서버에서 API가 없어져 함께 주석 처리
// export interface TaskItem {
// 	taskId: number
// 	taskType: TaskType
// 	title: string
// 	category: Category | null
// 	memo: string | null
// 	isAllDay: boolean
// 	isCompleted: boolean
// 	isImportant: boolean
// 	isPublic: boolean
// 	startDate?: string
// 	startTime?: string
// 	endDate?: string
// 	endTime?: string
// 	repeatType?: RepeatType
// 	repeatEndDate?: string
// 	growthType?: string | null
// 	growthValue?: number | null
// 	notificationAt?: string | null
// 	notificationMinutes?: number | null
// 	completedAt?: string | null
// 	createdAt?: string
// 	updatedAt?: string
// }

// export interface TaskScheduleDetail {
// 	totalCount: number
// 	items: TaskItem[]
// }

// 서버 GET api/task는 from, to 필수
export interface TaskScheduleParams {
	from: string
	to: string
}

// 서버 TaskOccurrenceResponse
export interface CalendarItem {
	taskId: number
	// taskType: TaskType
	title: string
	category: Category | null
	memo: string | null
	isAllDay?: boolean
	isCompleted: boolean
	isImportant: boolean
	isPublic: boolean
	occurrenceDate: string
	// 할 일 날짜는 occurrenceDate를 쓴다
	// endDate?: string | null
	endTime?: string | null
	repeatType: RepeatType
	repeatEndDate?: string | null
	repeatDaysOfWeek?: string[]
	repeatDaysOfMonth?: number[]
	excludeHoliday?: boolean
	// growthType?: string | null
	// 게임(정령 성장) 값. 기획 확정 전이라 받기만 하고 화면에서는 쓰지 않는다
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
	// completedRoutineCount: number
	// completedScheduleCount: number
	inCompleteCount: number
	// routineCount: number
	// scheduleCount: number
	items: CalendarItem[]
}
