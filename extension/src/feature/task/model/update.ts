import type { CalendarItem } from '@/entity/task/model/type'
import type { DayOfWeek, NotificationType, UpdateScheduleRequest } from './type'

const NOTIFICATION_BY_MINUTES: Record<number, NotificationType> = {
  10: 'TEN_MINUTES',
  30: 'THIRTY_MINUTES',
  60: 'ONE_HOUR',
}

// 조회 응답의 notificationMinutes → 수정 요청의 notificationType (기존 알림을 지우지 않기 위함)
export function toNotificationType(minutes?: number | null): NotificationType {
  return (minutes && NOTIFICATION_BY_MINUTES[minutes]) || 'NONE'
}

export function getScheduleDate(item: CalendarItem) {
  return item.endDate ?? item.occurrenceDate
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
  overrides: Partial<Omit<UpdateScheduleRequest, 'taskId'>> = {},
): UpdateScheduleRequest {
  const time = getScheduleTime(item)
  return {
    taskId: item.taskId,
    title: item.title,
    endDateTime: toEndDateTime(getScheduleDate(item), time),
    isAllDay: time === '',
    isImportant: item.isImportant,
    notificationType: toNotificationType(item.notificationMinutes),
    ...overrides,
  }
}

// 조회 응답의 repeatDaysOfWeek는 string[]이라 요청 타입으로 맞춘다
export function toDaysOfWeek(days?: string[]) {
  return (days ?? []) as DayOfWeek[]
}
