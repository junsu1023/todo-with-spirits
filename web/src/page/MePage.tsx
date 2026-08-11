import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getCurrentSpirit } from '@/entity/spirit'
import { SETTING_ITEMS, SettingRow } from '@/feature/setting'
import { SpiritListModal } from '@/feature/spirit'
import sampleSpiritImage from '@/shared/assets/sample-spirit.png'
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/ui/avatar'
import { Card } from '@/shared/ui/card'
import { StatCard } from '@/shared/ui/StatCard'

// ─── Mock data (유저/스탯은 API 미연결) ────────────────────────────────────────

const MOCK_USER = {
	nickname: 'Username',
	email: 'user@example.com',
}

const MOCK_STATS = {
	streak: 12,
	totalCompleted: 48,
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export function MePage() {
	const { data: spiritData } = useQuery({
		queryKey: ['spirit', 'current'],
		queryFn: getCurrentSpirit,
	})

	const spirit =
		spiritData?.result === 'success' ? spiritData.detail : undefined

	return (
		<main className="flex h-screen flex-col overflow-hidden p-6">
			<div className="flex flex-col gap-5">
				{/* 상단 3열 카드 */}
				<div className="flex gap-4">
					<Card className="flex w-65 shrink-0 flex-col items-center gap-3 p-6">
						<Avatar className="size-16">
							<AvatarImage src="" alt="profile" />
							<AvatarFallback className="text-lg">
								{MOCK_USER.nickname.slice(0, 1)}
							</AvatarFallback>
						</Avatar>
						<div className="flex flex-col items-center gap-0.5">
							<span className="text-base font-semibold text-gray-800">
								{MOCK_USER.nickname}
							</span>
							<span className="text-xs text-gray-400">{MOCK_USER.email}</span>
						</div>
						<button
							type="button"
							onClick={() => {}}
							className="cursor-pointer rounded-full bg-gray-100 px-4 py-1.5 text-xs font-medium text-gray-600 transition-colors hover:bg-gray-200"
						>
							계정 관리
						</button>
					</Card>

					<StatCard label="연속 달성" value={MOCK_STATS.streak} unit="일" />
					<StatCard
						label="총 완료 수"
						value={MOCK_STATS.totalCompleted}
						unit="개"
					/>
				</div>

				{/* 내 정령 */}
				<Card className="flex flex-col gap-4 p-6">
					<div className="flex items-center justify-between">
						<span className="text-base font-bold text-gray-800">내 정령</span>
						<span className="rounded-full bg-[#F3E8FF] px-3 py-1 text-xs font-medium text-[#B286FD]">
							대표 정령
						</span>
					</div>

					<div className="flex items-center gap-5">
						<img
							src={spirit?.imageUrl || sampleSpiritImage}
							alt={spirit?.spiritName ?? '정령'}
							className="h-24 w-24 shrink-0 rounded-xl object-cover"
							onError={(e) => { e.currentTarget.src = sampleSpiritImage }}
						/>

						<div className="flex flex-1 flex-col gap-2">
							<div className="flex items-center justify-between">
								<div className="flex items-center gap-2">
									<span className="text-lg font-bold text-gray-800">
										{spirit?.spiritName ?? '—'}
									</span>
									<span className="flex h-5 w-5 items-center justify-center rounded-full bg-[#B286FD] text-xs font-bold text-white">
										{spirit?.stage ?? '—'}
									</span>
								</div>
								<span className="text-2xl font-bold text-[#B286FD]">
									{spirit?.exp ?? 0}%
								</span>
							</div>

							<span className="text-xs text-gray-400">
								EXP {spirit?.exp ?? 0}
							</span>

							<div className="h-2 w-full overflow-hidden rounded-full bg-gray-100">
								<div
									className="h-full rounded-full bg-[#B286FD] transition-all"
									style={{ width: `${spirit?.exp ?? 0}%` }}
								/>
							</div>

							<div className="mt-1 flex gap-2">
								<Link
									to="/forest"
									className="flex-1 rounded-lg border border-gray-200 py-2 text-center text-sm font-medium text-gray-600 transition-colors hover:bg-gray-50"
								>
									정령 보기
								</Link>
								<SpiritListModal representativeId={spirit?.id ?? 0} />
							</div>
						</div>
					</div>
				</Card>

				{/* 설정 목록 */}
				<div className="flex flex-col gap-2">
					{SETTING_ITEMS.map((item) => (
						<SettingRow
							key={item.label}
							icon={item.icon}
							label={item.label}
							description={item.description}
						/>
					))}
				</div>
			</div>
		</main>
	)
}

export default MePage
