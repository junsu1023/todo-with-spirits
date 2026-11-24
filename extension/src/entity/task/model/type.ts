// web/src/entity/task/model/type.ts에서 MVP에 필요한 부분만 복사
// 필드는 백엔드 TaskOccurrenceResponse 기준 (2026-11-16 할 일 API 통합으로 taskType·endDate·루틴/일정별 개수가 빠짐)

// 백엔드 enum. NONE이면 할 일(단발성), 그 외는 루틴(반복). YEARLY는 백엔드가 생성·수정을 막는다
export type RepeatType = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'

export interface TaskScheduleParams {
  from: string
  to: string
}

export interface CalendarItem {
  taskId: number
  title: string
  occurrenceDate: string
  endTime?: string | null
  isAllDay?: boolean
  isImportant: boolean
  repeatType: RepeatType
  repeatEndDate?: string | null
  repeatDaysOfWeek?: string[]
  repeatDaysOfMonth?: number[]
  excludeHoliday?: boolean
  notificationMinutes?: number | null
  notificationAt?: string | null
  // 게임(정령 성장) 값. 기획 확정 전이라 받기만 하고 화면에서는 쓰지 않는다
  growthValue?: number
  isCompleted: boolean
  completedAt?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface CalendarDetail {
  totalCount: number
  completedCount: number
  inCompleteCount: number
  items: CalendarItem[]
}
