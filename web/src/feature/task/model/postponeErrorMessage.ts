import type { ApiErrorDetail } from '@/lib/api'

// 서버 영어 에러 메시지 → 사용자 노출용 한국어 (android ApiErrorMessageTranslator와 동일 문구)
const MESSAGE_MAP: Record<string, string> = {
	'originalDate is required for routines':
		'루틴을 미루기 위해서는 기존 일자가 필요합니다.',
	'Not an occurrence date for this routine': '루틴의 생성 일자가 잘못되었습니다.',
	'Postpone limit reached for this occurrence (max 3)':
		'최대 미루기 횟수 한도에 도달했습니다. (최대 3회)',
	'Daily routines cannot change date, only time': '일일 루틴은 미룰 수 없습니다.',
	'Weekly routines can only be postponed within the current week':
		'주간 루틴은 해당 주 내에서만 연기할 수 있습니다.',
	'Monthly routines can only be postponed within the current month':
		'월간 루틴은 해당 월에서만 연기할 수 있습니다.',
	'Unsupported repeat type for postpone': '미룰 수 없는 일정/루틴입니다.',
	'Target date already has an occurrence of this routine':
		'미룰 날짜에 같은 루틴이 있습니다.',
	'Target date is already occupied by another occurrence of this routine':
		'미룰 날짜에 같은 루틴이 있습니다.',
	'Not found schedule or routine.': '일정이나 루틴을 찾을 수 없습니다.',
}

export function getPostponeErrorMessage(detail: ApiErrorDetail): string {
	const message = detail.description[0]?.message?.trim()
	return (message && MESSAGE_MAP[message]) ?? '미루기에 실패했어요.'
}
