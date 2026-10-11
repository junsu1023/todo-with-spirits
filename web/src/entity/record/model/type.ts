import type { Category, TaskType } from '@/entity/task/model/type'

// ─── Today ───────────────────────────────────────────────────────────────────

export type MissionType = 'DAILY' | 'CONSISTENCY' | 'HIDDEN'

// todo: iconType 대신 iconImage로 바뀔 수 있음 (서버 협의 필요)
export type RewardIconType = 'THUMB_UP' | 'FLAME' | 'DIAMOND'

export interface RecordTypeProgress {
	completed: number
	total: number
}

export interface RecordTaskItem {
	taskId: number
	title: string
	taskType: TaskType
	growthType: string | null
	growthValue: number
	interpretation: string
	completed: boolean
}

export interface RecordReward {
	missionType: MissionType
	title: string
	rewardExp: number
	iconType: RewardIconType
	achieved: boolean
}

export interface DailyRecordDetail {
	date: string
	completionRate: number
	completedCount: number
	totalCount: number
	earnedGrowthPower: number
	typeBreakdown?: Partial<Record<'ROUTINE' | 'SCHEDULE', RecordTypeProgress>>
	items?: RecordTaskItem[]
	todayRewards?: RecordReward[]
}

// ─── Weekly / Monthly 공통 ───────────────────────────────────────────────────

export interface RecordParams {
	date: string
}

// ─── Weekly ──────────────────────────────────────────────────────────────────

// 문서 기준 값 (android는 String으로 받음)
export type WeeklyDayIcon = 'SUCCESS' | 'FAILED' | 'EMPTY'

export interface WeeklyDailyChart {
	date: string
	dayOfWeek: 'SUN' | 'MON' | 'TUE' | 'WED' | 'THU' | 'FRI' | 'SAT'
	dayNumber?: number
	growthPower: number | null
	scheduleCompleted: number
	scheduleTotal: number
	routineCompleted: number
	routineTotal: number
	icon: WeeklyDayIcon
}

export interface WeeklyTypeAnalysis {
	scheduleRatio: number
	routineRatio: number
	delayedRatio: number
}

export interface WeeklyPlanAnalysis {
	analysisTitle: string
	taskTitle: string
	completedCount: number
	targetCount: number
}

export interface WeeklyAchievement {
	code: string
	title: string
	description: string
	icon: string
	targetCount: number
}

export interface WeeklyRecordDetail {
	week: number
	message: string
	dailyCharts?: WeeklyDailyChart[] | null
	completedTaskCount: number
	delayedCount: number
	totalTaskCount: number
	averageCompletionRate: number
	typeAnalysis?: WeeklyTypeAnalysis | null
	analyses?: WeeklyPlanAnalysis[] | null
	// 주간 실천 Top 3 영역에 사용 (android 기준)
	achievements?: WeeklyAchievement[] | null
}

// ─── Monthly ─────────────────────────────────────────────────────────────────

export interface MonthlyDailyHeatmap {
	date: string
	scheduleTotalCount: number
	scheduleCompletedCount: number
	routineTotalCount: number
	routineCompletedCount: number
}

export interface MonthlyCategoryCount {
	category: Category
	completedCount: number
	totalCount: number
}

export interface MonthlyComparison {
	month: number
	completedRate: number
}

export interface MonthlyRecordDetail {
	year: number
	month: number
	message: string
	completedTaskCount: number
	totalTaskCount: number
	averageCompletionRate: number
	dailyHeatmaps?: MonthlyDailyHeatmap[] | null
	monthlyComparisons?: MonthlyComparison[] | null
	mainCategory: Category | null
	mainCategoryPeerPercentile: number
	mainCategoryCompletionRate: number
	title: string
	content: string
	topCategories?: MonthlyCategoryCount[] | null
	bottomCategory: MonthlyCategoryCount | null
}
