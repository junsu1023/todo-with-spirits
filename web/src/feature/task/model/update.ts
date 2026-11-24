import type { CalendarItem } from '@/entity/task/model/type'
import type {
  DayOfWeek,
  NotificationType,
  RoutineRepeatType,
  UpdateTaskRequest,
} from './type'

/**
 * 수정 요청(PATCH api/task/{taskId})을 조회 응답의 기존 값으로 채운다.
 * 서버는 endDateTime 외에 보내지 않은 값을 지우므로(중요 → false, 알림 → 없음, 반복 → NONE)
 * 화면에서 고치지 않는 값도 그대로 돌려보낸다. 바꿀 값만 overrides로 덮어쓴다.
 */

type Overrides = Partial<Omit<UpdateTaskRequest, 'taskId'>>

const NOTIFICATION_BY_MINUTES: Record<number, NotificationType> = {
  10: 'TEN_MINUTES',
  30: 'THIRTY_MINUTES',
  60: 'ONE_HOUR',
}

/**
 * 조회 응답의 notificationMinutes → 수정 요청의 notificationType.
 * 웹은 아직 알림 기능을 적용하지 않았지만, android 등에서 설정한 알림이 웹 수정으로 사라지지 않도록 그대로 돌려보낸다.
 * todo: 웹에 알림 설정 UI가 생기면 사용자가 고른 값을 보내도록 바꾼다
 */
export function toNotificationType(minutes?: number | null): NotificationType {
  return (minutes && NOTIFICATION_BY_MINUTES[minutes]) || 'NONE'
}

// 할 일은 날짜가 하나라 occurrenceDate가 곧 할 일 날짜 (서버 응답에서 endDate가 빠짐)
export function toScheduleUpdate(
  item: CalendarItem,
  overrides: Overrides = {},
): UpdateTaskRequest {
  const time = !item.isAllDay && item.endTime ? item.endTime : null
  return {
    taskId: item.taskId,
    title: item.title,
    endDateTime: `${item.occurrenceDate}T${time ?? '23:59:59'}`,
    isAllDay: time === null,
    isImportant: item.isImportant,
    notificationType: toNotificationType(item.notificationMinutes),
    repeatType: 'NONE',
    ...overrides,
  }
}

// endDateTime은 보내지 않는다 → 서버가 기존 반복 시작일·시각을 유지한다
// (occurrenceDate는 그날 회차라 시작일로 보내면 반복 시작일이 바뀜)
export function toRoutineUpdate(
  item: CalendarItem,
  overrides: Overrides = {},
): UpdateTaskRequest {
  return {
    taskId: item.taskId,
    title: item.title,
    isImportant: item.isImportant,
    notificationType: toNotificationType(item.notificationMinutes),
    repeatType: toRoutineRepeatType(item),
    repeatEndDate: item.repeatEndDate ?? undefined,
    repeatDaysOfWeek: (item.repeatDaysOfWeek ?? []) as DayOfWeek[],
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
