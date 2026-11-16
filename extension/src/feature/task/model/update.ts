import type { CalendarItem } from '@/entity/task/model/type'
import type {
  DayOfWeek,
  NotificationType,
  RoutineRepeatType,
  UpdateTaskRequest,
} from './type'

const NOTIFICATION_BY_MINUTES: Record<number, NotificationType> = {
  10: 'TEN_MINUTES',
  30: 'THIRTY_MINUTES',
  60: 'ONE_HOUR',
}

// 조회 응답의 repeatType이 NONE이 아니면 루틴
export function isRoutine(item: CalendarItem) {
  return item.repeatType !== 'NONE'
}

// 조회 응답의 notificationMinutes → 수정 요청의 notificationType (기존 알림을 지우지 않기 위함)
export function toNotificationType(minutes?: number | null): NotificationType {
  return (minutes && NOTIFICATION_BY_MINUTES[minutes]) || 'NONE'
}

// 할 일은 날짜가 하나라 occurrenceDate가 곧 할 일 날짜
export function getScheduleDate(item: CalendarItem) {
  return item.occurrenceDate
}

// 시간이 없으면(하루 종일) 빈 문자열
export function getScheduleTime(item: CalendarItem) {
  return !item.isAllDay && item.endTime ? item.endTime.slice(0, 5) : ''
}

// web ScheduleForm과 같은 형식. time이 비어 있으면 하루 종일
export function toEndDateTime(date: string, time: string) {
  return time ? `${date}T${time}:00` : `${date}T23:59:59`
}

// 기존 할 일 값을 그대로 담은 수정 요청. 일부만 바꿀 때 overrides로 덮어쓴다
export function toScheduleUpdate(
  item: CalendarItem,
  overrides: Partial<Omit<UpdateTaskRequest, 'taskId'>> = {},
): UpdateTaskRequest {
  const time = getScheduleTime(item)
  return {
    taskId: item.taskId,
    title: item.title,
    endDateTime: toEndDateTime(getScheduleDate(item), time),
    isAllDay: time === '',
    isImportant: item.isImportant,
    notificationType: toNotificationType(item.notificationMinutes),
    repeatType: 'NONE',
    ...overrides,
  }
}

/**
 * 기존 루틴 값을 그대로 담은 수정 요청. 일부만 바꿀 때 overrides로 덮어쓴다.
 * endDateTime은 보내지 않는다 → 백엔드가 기존 반복 시작일·시각을 유지한다
 * (occurrenceDate는 오늘 회차라 시작일로 보내면 시작일이 바뀜)
 */
export function toRoutineUpdate(
  item: CalendarItem,
  overrides: Partial<Omit<UpdateTaskRequest, 'taskId'>> = {},
): UpdateTaskRequest {
  return {
    taskId: item.taskId,
    title: item.title,
    isImportant: item.isImportant,
    notificationType: toNotificationType(item.notificationMinutes),
    repeatType: toRoutineRepeatType(item),
    repeatEndDate: item.repeatEndDate ?? undefined,
    repeatDaysOfWeek: toDaysOfWeek(item.repeatDaysOfWeek),
    repeatDaysOfMonth: item.repeatDaysOfMonth ?? [],
    excludeHoliday: item.excludeHoliday ?? false,
    ...overrides,
  }
}

export function toRoutineRepeatType(item: CalendarItem): RoutineRepeatType {
  return item.repeatType === 'WEEKLY' || item.repeatType === 'MONTHLY'
    ? item.repeatType
    : 'DAILY'
}

// 조회 응답의 repeatDaysOfWeek는 string[]이라 요청 타입으로 맞춘다
export function toDaysOfWeek(days?: string[]) {
  return (days ?? []) as DayOfWeek[]
}
