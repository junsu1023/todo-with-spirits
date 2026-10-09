import { CircleDashed, Trophy } from 'lucide-react'
import { WEEKLY_MISSED_CATEGORY_MOCK, WEEKLY_TIP_MOCK } from '../model/mock'
import type { WeeklyAchievement, WeeklyRecordDetail } from '../model/type'
import { RecordCard } from './RecordCard'
import { type RankItem, RecordRankList } from './RecordRankList'
import { RecordTip } from './RecordTip'
import { WeeklyTypeRatioBar } from './WeeklyTypeRatioBar'

const EMPTY_ANALYSIS = { scheduleRatio: 0, routineRatio: 0, delayedRatio: 0 }

// android와 동일: 주간 실천 Top 3 = achievements (title + targetCount)
// todo: achievement.icon 값 목록 미확정 → Trophy 아이콘으로 임시 대체, targetCount 의미 확인 필요
const toAchievementRankItem = (achievement: WeeklyAchievement): RankItem => ({
	key: achievement.code,
	icon: Trophy,
	label: achievement.title,
	value: `${achievement.targetCount}회`,
})

export function WeeklyAnalysisCard({ record }: { record: WeeklyRecordDetail }) {
	const topItems = (record.achievements ?? []).slice(0, 3).map(toAchievementRankItem)

	return (
		<RecordCard title="주간 분석">
			<WeeklyTypeRatioBar analysis={record.typeAnalysis ?? EMPTY_ANALYSIS} />
			<RecordRankList title="주간 실천 Top 3" items={topItems} />
			{/* todo: 주간 API에 놓친 분야 없음 → 임시 값 사용 중 (android도 기본값 표시), API 추후 수정 필요 */}
			<RecordRankList
				title="자주 놓친 분야"
				items={[
					{ key: 'missed', icon: CircleDashed, ...WEEKLY_MISSED_CATEGORY_MOCK },
				]}
			/>
			{/* todo: 하이라이트 문구 API에 없음 → 임시 문구 사용 중 (android도 하드코딩) */}
			<RecordTip {...WEEKLY_TIP_MOCK} />
		</RecordCard>
	)
}
