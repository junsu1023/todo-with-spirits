import type { MissionType } from './type'

// android TodayRewardCard의 missionType 번역과 동일
export const MISSION_LABEL: Record<MissionType, string> = {
	DAILY: '일일 미션',
	CONSISTENCY: '끈기 스코어',
	HIDDEN: '히든 미션',
}

// android와 동일하게 4개까지 노출, 5개 이상일 때만 더보기 노출
export const REWARD_PREVIEW_COUNT = 4
