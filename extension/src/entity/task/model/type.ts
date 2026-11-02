// web/src/entity/task/model/type.ts에서 MVP에 필요한 부분만 복사

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

export interface TaskScheduleParams {
  from?: string
  to?: string
}

export interface CalendarItem {
  taskId: number
  taskType: TaskType
  title: string
  category: Category | null
  memo: string | null
  isAllDay?: boolean
  isCompleted: boolean
  isImportant: boolean
  isPublic: boolean
  occurrenceDate: string
  endDate?: string | null
  endTime?: string | null
  repeatType?: RepeatType
  repeatEndDate?: string | null
  repeatDaysOfWeek?: string[]
  repeatDaysOfMonth?: number[]
  excludeHoliday?: boolean
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
