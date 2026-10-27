import type { RepeatType } from '@/entity/task/model/type'

// web/src/feature/task/model/type.ts에서 MVP에 필요한 부분만 복사

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY'

export type RoutineRepeatType = Exclude<RepeatType, 'YEARLY'>

export interface CreateScheduleRequest {
  title: string
  isAllDay: boolean
  startDatetime: string
  endDateTime: string
}

export interface CreateRoutineRequest {
  title: string
  repeatType: RoutineRepeatType
  repeatDaysOfWeek?: DayOfWeek[]
  repeatDaysOfMonth?: number[]
}
