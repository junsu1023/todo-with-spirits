import { Flame, Gem, type LucideIcon, ThumbsUp } from 'lucide-react'
import { MISSION_LABEL } from '../model/reward'
import type { RecordReward, RewardIconType } from '../model/type'

// todo: iconType → iconImage로 바뀔 수 있음 (서버 협의 중), 현재는 lucide 아이콘으로 임시 대체
const REWARD_ICON: Record<RewardIconType, LucideIcon> = {
	THUMB_UP: ThumbsUp,
	FLAME: Flame,
	DIAMOND: Gem,
}

export function RewardItem({ reward }: { reward: RecordReward }) {
	const { missionType, title, rewardExp, iconType, achieved } = reward
	const Icon = REWARD_ICON[iconType] ?? Gem
	const accent = missionType === 'HIDDEN' ? 'text-brand' : 'text-gray-500'

	return (
		<li
			className={`flex items-center gap-3 rounded-xl bg-gray-50 px-4 py-3 ${
				achieved ? '' : 'opacity-60'
			}`}
		>
			<Icon size={20} className={accent} />
			<div className="flex flex-1 flex-col">
				<span className={`text-xs ${accent}`}>
					{MISSION_LABEL[missionType] ?? '기타'}
				</span>
				<span className="text-sm text-gray-700">{title}</span>
			</div>
			<div className="flex flex-col items-end">
				<span className={`text-lg font-bold ${accent}`}>{rewardExp}</span>
				<span className="text-[10px] text-gray-400">EXP</span>
			</div>
		</li>
	)
}
