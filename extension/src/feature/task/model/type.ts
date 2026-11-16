import type { RepeatType } from '@/entity/task/model/type'

// 백엔드 dto/task (TaskRequest, TaskResponse, TaskCompleteResponse) 기준

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY'

// 루틴으로 고를 수 있는 반복. 백엔드 ALLOWED_REPEAT에서 NONE(할 일)을 뺀 값
export type RoutineRepeatType = Exclude<RepeatType, 'NONE' | 'YEARLY'>

// 백엔드 enum: NONE, TEN_MINUTES(10), THIRTY_MINUTES(30), ONE_HOUR(60)
export type NotificationType =
  | 'NONE'
  | 'TEN_MINUTES'
  | 'THIRTY_MINUTES'
  | 'ONE_HOUR'

/**
 * 할 일·루틴 생성/수정 공통 요청 (POST api/task, PATCH api/task/{taskId}).
 * repeatType이 없거나 NONE이면 할 일, DAILY/WEEKLY/MONTHLY면 루틴.
 * 수정 시 endDateTime만 비우면 기존 날짜·시각이 유지되고,
 * 나머지 값은 보내지 않으면 기본값(false, NONE, null)으로 지워진다.
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
}

export interface UpdateTaskRequest extends TaskRequest {
  taskId: number
}

// 생성/수정 응답
export interface TaskResponse {
  taskId: number
  title: string
  // 할 일: 해당 날짜 / 루틴: 반복 시작일
  startDate: string
  endTime?: string | null
  isAllDay: boolean
  isImportant: boolean
  repeatType: RepeatType
  repeatEndDate?: string | null
  repeatDaysOfWeek: string[]
  repeatDaysOfMonth: number[]
  excludeHoliday: boolean
  notificationMinutes?: number | null
  notificationAt?: string | null
  growthValue?: number
  growthType?: string
  isCompleted?: boolean
  completedAt?: string | null
  createdAt?: string
  updatedAt?: string
}

// 완료 시 지급된 재화. 이미 지급된 적 있으면 currencyEarned = 0
export interface TaskCompletionResponse {
  currencyEarned: number
  balance: number
}
