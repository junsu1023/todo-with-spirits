const HANGUL_START = 0xac00
const HANGUL_END = 0xd7a3

/** 마지막 글자 받침 유무에 따라 '이랑' / '랑' 선택 (한글이 아니면 '랑') */
const withRang = (name: string) => {
	const code = name.charCodeAt(name.length - 1)
	const isHangul = code >= HANGUL_START && code <= HANGUL_END
	const hasFinalConsonant = isHangul && (code - HANGUL_START) % 28 !== 0
	return `${name}${hasFinalConsonant ? '이랑' : '랑'}`
}

const DEFAULT_SPIRIT_NAME = '정령'

/** 데일리 리포트 헤드라인 아래 안내 문구 */
export function getDailyMessage({
	spiritName,
	completedCount,
	totalCount,
}: {
	spiritName?: string
	completedCount: number
	totalCount: number
}) {
	const name = withRang(spiritName || DEFAULT_SPIRIT_NAME)
	const remaining = Math.max(totalCount - completedCount, 0)

	if (totalCount === 0) return '오늘은 등록된 일정이 없어요.'
	if (remaining === 0) return `${name} 오늘 일정을 모두 해치웠어요!`
	return `${name} 남은 ${remaining}개도 끝내볼까요?`
}
