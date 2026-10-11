import { getDailyMessage } from '../lib/message'
import type { DailyRecordDetail, RecordTypeProgress } from '../model/type'
import { DailyGoalProgress } from './DailyGoalProgress'
import { DailyRetryGoal } from './DailyRetryGoal'
import { DailyTypeProgress } from './DailyTypeProgress'
import { RecordCard, RecordHeadline } from './RecordCard'

// todo: daily API에 헤드라인 없음 → 임시 문구 사용 중 (android도 하드코딩), API 추후 수정 필요
const DAILY_HEADLINE_MOCK = '다 잘해 진짜!'

const EMPTY_PROGRESS: RecordTypeProgress = { completed: 0, total: 0 }

interface DailyReportCardProps {
	record: DailyRecordDetail
	spiritName?: string
	spiritImageUrl?: string
}

export function DailyReportCard({
	record,
	spiritName,
	spiritImageUrl,
}: DailyReportCardProps) {
	const { typeBreakdown, items = [], completedCount, totalCount } = record

	// 해당 타입 일정이 없는 날은 typeBreakdown 키가 빠질 수 있음
	const scheduleProgress = typeBreakdown?.SCHEDULE ?? EMPTY_PROGRESS
	const routineProgress = typeBreakdown?.ROUTINE ?? EMPTY_PROGRESS

	// android와 동일: 오늘 미완료 항목 = 내일 다시 도전할 목표
	const retryCount = items.filter((item) => !item.completed).length

	return (
		<RecordCard title="데일리 리포트">
			<RecordHeadline
				title={DAILY_HEADLINE_MOCK}
				// todo: 문구는 기획 확정 전 임시, 정령 이름과 남은 개수만 실제 값
				message={getDailyMessage({ spiritName, completedCount, totalCount })}
			/>
			<DailyGoalProgress
				// todo: completionRate를 0~1 비율로 가정 (android 기준), 0~100으로 내려오면 수정 필요
				completionRate={record.completionRate}
				completedCount={completedCount}
				totalCount={totalCount}
				spiritImageUrl={spiritImageUrl}
			/>
			<div className="flex gap-4">
				<DailyTypeProgress label="To do" color="#8DE4FF" progress={scheduleProgress} />
				<DailyTypeProgress label="루틴" color="#B2F042" progress={routineProgress} />
			</div>
			<DailyRetryGoal count={retryCount} />
		</RecordCard>
	)
}
