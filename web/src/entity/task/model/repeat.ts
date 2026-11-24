import type { RepeatType } from './type'

// 서버 응답에 taskType이 없어져 repeatType으로 구분한다. NONE이면 할 일, 그 외는 루틴
export function isRoutine(item: { repeatType?: RepeatType }) {
  return item.repeatType !== undefined && item.repeatType !== 'NONE'
}
