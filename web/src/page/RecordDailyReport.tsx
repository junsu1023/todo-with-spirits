import { Gem, Medal, Repeat } from 'lucide-react'
import sampleSpiritImage from '@/shared/assets/sample-spirit.png'
import { DAILY_MOCK } from './recordMock'
import { RecordCard, RecordHeadline, RecordRing } from './RecordShared'

const REWARD_ICON = {
	'달성 미션': Medal,
	'연기 스트릭': Repeat,
	'히든 미션': Gem,
} as const

function GoalProgress({ done, total }: { done: number; total: number }) {
	const percentage = Math.round((done / total) * 100)

	return (
		<div className="flex flex-col gap-2 pt-10">
			<div className="relative h-3 rounded-full bg-gray-100">
				<div
					className="h-full rounded-full bg-brand-light"
					style={{ width: `${percentage}%` }}
				/>
				{/* 진행 위치에 정령 표시 */}
				<div
					className="absolute bottom-0 flex -translate-x-1/2 flex-col items-center"
					style={{ left: `${percentage}%` }}
				>
					<span className="mb-1 rounded-full bg-white px-2 py-0.5 text-xs font-semibold text-brand ring-1 ring-brand-light">
						{percentage}%
					</span>
					<img src={sampleSpiritImage} alt="" className="size-10 object-contain" />
				</div>
			</div>
			<span className="self-end text-xs text-gray-400">
				오늘 목표 {done} / {total}개 달성
			</span>
		</div>
	)
}

function RingStat({
	label,
	done,
	total,
	color,
}: {
	label: string
	done: number
	total: number
	color: string
}) {
	return (
		<div className="flex flex-1 flex-col items-center gap-3 rounded-xl bg-gray-50 p-5">
			<span className="text-sm font-medium text-gray-600">{label}</span>
			<RecordRing percentage={Math.round((done / total) * 100)} color={color} />
			<span className="text-xs text-gray-400">
				{done} / {total}
			</span>
		</div>
	)
}

export function RecordDailyReport() {
	const d = DAILY_MOCK

	return (
		<div className="grid grid-cols-1 gap-6 lg:grid-cols-[3fr_2fr]">
			<RecordCard title="데일리 리포트">
				<RecordHeadline title={d.headline} message={d.message} />
				<GoalProgress done={d.goalDone} total={d.goalTotal} />
				<div className="flex gap-4">
					<RingStat label="To do" {...d.todo} color="#8DE4FF" />
					<RingStat label="루틴" {...d.routine} color="#B2F042" />
				</div>
				<div className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3">
					<span className="text-sm text-gray-600">내일 다시 도전할 목표</span>
					<span className="text-sm font-semibold text-gray-800">
						{d.retryGoalCount}개
					</span>
				</div>
			</RecordCard>

			<RecordCard title="오늘의 보상">
				<ul className="flex flex-col gap-2">
					{d.rewards.map((reward, i) => {
						const Icon = REWARD_ICON[reward.kind as keyof typeof REWARD_ICON]
						const isHidden = reward.kind === '히든 미션'
						return (
							<li
								// biome-ignore lint/suspicious/noArrayIndexKey: mock 데이터
								key={i}
								className="flex items-center gap-3 rounded-xl bg-gray-50 px-4 py-3"
							>
								<Icon
									size={20}
									className={isHidden ? 'text-brand' : 'text-gray-400'}
								/>
								<div className="flex flex-1 flex-col">
									<span
										className={`text-xs ${isHidden ? 'text-brand' : 'text-gray-400'}`}
									>
										{reward.kind}
									</span>
									<span className="text-sm text-gray-700">{reward.title}</span>
								</div>
								<div className="flex flex-col items-end">
									<span
										className={`text-lg font-bold ${isHidden ? 'text-brand' : 'text-gray-700'}`}
									>
										{reward.exp}
									</span>
									<span className="text-[10px] text-gray-400">EXP</span>
								</div>
							</li>
						)
					})}
				</ul>
				<button
					type="button"
					className="self-center rounded-full bg-brand-muted px-4 py-1.5 text-xs font-medium text-brand hover:bg-brand-light/50"
				>
					더보기
				</button>
			</RecordCard>
		</div>
	)
}
