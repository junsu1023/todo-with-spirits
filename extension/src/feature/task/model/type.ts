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

// web의 NotificationType과 같음. 백엔드 enum: NONE, TEN_MINUTES(10), THIRTY_MINUTES(30), ONE_HOUR(60)
export type NotificationType =
  | 'NONE'
  | 'TEN_MINUTES'
  | 'THIRTY_MINUTES'
  | 'ONE_HOUR'

// 백엔드 ScheduleUpdateRequest에 있는 필드만. 보내지 않은 알림은 지워지므로 기존 값을 같이 보낸다
export interface UpdateScheduleRequest {
  taskId: number
  title: string
  endDateTime: string
  isAllDay: boolean
  isImportant: boolean
  notificationType: NotificationType
}

// 백엔드 RoutineUpdateRequest에 있는 필드만. 보내지 않은 반복 종료일·알림은 지워지므로 기존 값을 같이 보낸다
export interface UpdateRoutineRequest {
  taskId: number
  title: string
  repeatType: RoutineRepeatType
  repeatEndDate?: string
  repeatDaysOfWeek?: DayOfWeek[]
  repeatDaysOfMonth?: number[]
  excludeHoliday: boolean
  notificationType: NotificationType
}
