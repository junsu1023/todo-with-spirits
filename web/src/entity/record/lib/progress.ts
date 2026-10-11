/** 0~1 비율 → 0~100 정수 퍼센트 (범위 밖 값은 잘라냄) */
export const toPercent = (rate: number) =>
	Math.round(Math.min(Math.max(rate, 0), 1) * 100)

/** 완료 / 전체 → 0~1 비율 (전체가 0이면 0) */
export const toRate = (completed: number, total: number) =>
	total > 0 ? completed / total : 0
