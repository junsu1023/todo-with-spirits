import type { NotificationType } from './type'

const NOTIFICATION_BY_MINUTES: Record<number, NotificationType> = {
  10: 'TEN_MINUTES',
  30: 'THIRTY_MINUTES',
  60: 'ONE_HOUR',
}

/**
 * 조회 응답의 notificationMinutes → 수정 요청의 notificationType.
 *
 * 웹은 아직 알림 기능을 적용하지 않았지만, 수정 API(PATCH api/task/schedule|routine/{id})는
 * notificationType을 보내지 않으면 기존 알림을 지운다 (back TaskService.updateSchedule/updateRoutine).
 * android 등에서 설정한 알림이 웹 수정으로 사라지지 않도록 조회한 값을 그대로 돌려보낸다.
 * todo: 웹에 알림 설정 UI가 생기면 사용자가 고른 값을 보내도록 바꾼다
 */
export function toNotificationType(minutes?: number | null): NotificationType {
  return (minutes && NOTIFICATION_BY_MINUTES[minutes]) || 'NONE'
}
