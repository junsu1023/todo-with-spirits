import { useState } from 'react'
import { REWARD_PREVIEW_COUNT } from '../model/reward'
import type { RecordReward } from '../model/type'
import { RecordCard } from './RecordCard'
import { RewardItem } from './RewardItem'

export function TodayRewardCard({ rewards }: { rewards: RecordReward[] }) {
	const [isExpanded, setIsExpanded] = useState(false)

	const visibleRewards = isExpanded
		? rewards
		: rewards.slice(0, REWARD_PREVIEW_COUNT)
	const canExpand = !isExpanded && rewards.length > REWARD_PREVIEW_COUNT

	return (
		<RecordCard title="오늘의 보상">
			{rewards.length === 0 ? (
				<p className="py-6 text-center text-sm text-gray-300">
					오늘 받을 수 있는 보상이 없어요
				</p>
			) : (
				<ul className="flex flex-col gap-2">
					{visibleRewards.map((reward) => (
						<RewardItem
							key={`${reward.missionType}-${reward.title}`}
							reward={reward}
						/>
					))}
				</ul>
			)}
			{canExpand && (
				<button
					type="button"
					onClick={() => setIsExpanded(true)}
					className="self-center rounded-full bg-brand-muted px-4 py-1.5 text-xs font-medium text-brand hover:bg-brand-light/50"
				>
					더보기
				</button>
			)}
		</RecordCard>
	)
}
