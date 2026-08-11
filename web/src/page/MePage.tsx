import { Bell, ChevronRight, Cloud, Info, Monitor, User, X } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { cn } from '@/lib/utils'
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/ui/avatar'
import { Card } from '@/shared/ui/card'
import {
	Dialog,
	DialogClose,
	DialogPopup,
	DialogTitle,
	DialogTrigger,
} from '@/shared/ui/dialog'

// ─── Mock data ────────────────────────────────────────────────────────────────

const MOCK_USER = {
	nickname: 'Username',
	email: 'user@example.com',
}

const MOCK_STATS = {
	streak: 12,
	totalCompleted: 48,
}

const MOCK_SPIRITS = [
	{
		id: 1,
		name: '루미',
		level: 3,
		exp: 999,
		maxExp: 1000,
		isRepresentative: true,
	},
	{
		id: 2,
		name: '가이아',
		level: 5,
		exp: 420,
		maxExp: 800,
		isRepresentative: false,
	},
	{
		id: 3,
		name: '플레임',
		level: 2,
		exp: 150,
		maxExp: 500,
		isRepresentative: false,
	},
	{
		id: 4,
		name: '아쿠아',
		level: 7,
		exp: 1200,
		maxExp: 1500,
		isRepresentative: false,
	},
	{
		id: 5,
		name: '윈드',
		level: 1,
		exp: 80,
		maxExp: 300,
		isRepresentative: false,
	},
	{
		id: 6,
		name: '썬더',
		level: 4,
		exp: 600,
		maxExp: 900,
		isRepresentative: false,
	},
]

const REPRESENTATIVE_SPIRIT =
	MOCK_SPIRITS.find((s) => s.isRepresentative) ?? MOCK_SPIRITS[0]

// ─── Settings items ───────────────────────────────────────────────────────────

const SETTING_ITEMS = [
	{
		icon: User,
		label: '계정 설정',
		description: '프로필 정보, 회원 탈퇴',
	},
	{
		icon: Bell,
		label: '알림 설정',
		description: '리마인드, 정기알림, 야간푸시, 이벤트 수신',
	},
	{
		icon: Monitor,
		label: '디스플레이 설정',
		description: '다크모드, 플랜 표시, 테마, 언어',
	},
	{
		icon: Cloud,
		label: '데이터 설정',
		description: '백업, 동기화, 데이터 내보내기',
	},
	{
		icon: Info,
		label: '고객 지원',
		description: '공지사항, 이용안내, FAQ, 약관 및 정책',
	},
]

// ─── Sub components ───────────────────────────────────────────────────────────

function StatCard({
	label,
	value,
	unit,
}: {
	label: string
	value: number
	unit: string
}) {
	return (
		<Card className="flex w-full flex-col items-center justify-center gap-2 p-4">
			<span className="text-sm text-gray-400">{label}</span>
			<div className="flex items-end gap-1">
				<span className="text-5xl font-bold text-[#B286FD]">{value}</span>
				<span className="mb-1 text-lg font-medium text-gray-500">{unit}</span>
			</div>
		</Card>
	)
}

function SettingRow({
	icon: Icon,
	label,
	description,
}: {
	icon: typeof User
	label: string
	description: string
}) {
	return (
		<button
			type="button"
			onClick={() => {}}
			className="flex w-full items-center gap-4 rounded-xl bg-white px-5 py-4 text-left ring-1 ring-gray-100 transition-colors hover:bg-gray-50"
		>
			<div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gray-100">
				<Icon size={16} className="text-gray-500" />
			</div>
			<div className="flex flex-1 flex-col gap-0.5">
				<span className="text-sm font-medium text-gray-800">{label}</span>
				<span className="text-xs text-gray-400">{description}</span>
			</div>
			<ChevronRight size={16} className="shrink-0 text-gray-300" />
		</button>
	)
}

type Spirit = (typeof MOCK_SPIRITS)[number]

function SpiritCard({
	spirit,
	isSelected,
	onClick,
}: {
	spirit: Spirit
	isSelected: boolean
	onClick: () => void
}) {
	const expPercent = Math.round((spirit.exp / spirit.maxExp) * 100)
	return (
		<button
			type="button"
			onClick={onClick}
			className={cn(
				'flex flex-col items-center gap-2 rounded-2xl border-2 p-4 text-left transition-all',
				isSelected
					? 'border-[#B286FD] bg-[#F3E8FF]'
					: 'border-transparent bg-gray-50 hover:border-gray-200 hover:bg-gray-100',
			)}
		>
			{/* 정령 이미지 */}
			<div className="relative">
				<div className="h-16 w-16 rounded-xl bg-gray-200" />
				{spirit.isRepresentative && (
					<span className="absolute -top-1.5 -right-1.5 rounded-full bg-[#B286FD] px-1.5 py-0.5 text-[9px] font-bold text-white leading-tight">
						대표
					</span>
				)}
			</div>

			{/* 이름 + 레벨 */}
			<div className="flex items-center gap-1">
				<span className="text-sm font-semibold text-gray-800">
					{spirit.name}
				</span>
				<span className="flex h-4 w-4 items-center justify-center rounded-full bg-[#B286FD] text-[10px] font-bold text-white">
					{spirit.level}
				</span>
			</div>

			{/* EXP 바 */}
			<div className="w-full">
				<div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-200">
					<div
						className="h-full rounded-full bg-[#B286FD]"
						style={{ width: `${expPercent}%` }}
					/>
				</div>
				<span className="mt-0.5 block text-center text-[10px] text-gray-400">
					{expPercent}%
				</span>
			</div>
		</button>
	)
}

function SpiritListModal({ representativeId }: { representativeId: number }) {
	const [selectedId, setSelectedId] = useState<number>(representativeId)
	const [open, setOpen] = useState(false)

	const handleConfirm = () => {
		// TODO: call API to change representative spirit
		setOpen(false)
	}

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger className="flex-1 rounded-lg bg-[#B286FD] py-2 text-sm font-medium text-white transition-opacity hover:opacity-90">
				대표 정령 변경
			</DialogTrigger>

			<DialogPopup className="max-h-[90vh] max-w-2xl overflow-hidden flex flex-col">
				{/* 헤더 */}
				<div className="flex items-center justify-between mb-5">
					<DialogTitle>보유 정령</DialogTitle>
					<DialogClose className="flex h-7 w-7 items-center justify-center rounded-full text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors">
						<X size={16} />
					</DialogClose>
				</div>

				{/* 정령 그리드 */}
				<div className="flex-1 overflow-y-auto">
					<div className="grid grid-cols-3 gap-3 sm:grid-cols-4">
						{MOCK_SPIRITS.map((spirit) => (
							<SpiritCard
								key={spirit.id}
								spirit={spirit}
								isSelected={selectedId === spirit.id}
								onClick={() => setSelectedId(spirit.id)}
							/>
						))}
					</div>
				</div>

				{/* 하단 버튼 */}
				<div className="mt-5 flex gap-2 border-t border-gray-100 pt-5">
					<DialogClose className="flex-1 rounded-xl border border-gray-200 py-2.5 text-sm font-medium text-gray-600 transition-colors hover:bg-gray-50">
						취소
					</DialogClose>
					<button
						type="button"
						onClick={handleConfirm}
						disabled={selectedId === representativeId}
						className="flex-1 rounded-xl bg-[#B286FD] py-2.5 text-sm font-medium text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-40"
					>
						대표 정령으로 설정
					</button>
				</div>
			</DialogPopup>
		</Dialog>
	)
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export function MePage() {
	const spirit = REPRESENTATIVE_SPIRIT
	const expPercent = Math.round((spirit.exp / spirit.maxExp) * 100)

	return (
		<main className="flex h-screen flex-col overflow-hidden p-6">
			<div className="flex flex-col gap-5 ">
				{/* 상단 3열 카드 */}
				<div className="flex gap-4">
					{/* 프로필 카드 */}
					<Card className="flex w-[260px] shrink-0 flex-col items-center gap-3 p-6">
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
							className="rounded-full bg-gray-100 px-4 py-1.5 text-xs font-medium text-gray-600 transition-colors hover:bg-gray-200"
						>
							계정 관리
						</button>
					</Card>

					{/* 연속 달성 */}
					<StatCard label="연속 달성" value={MOCK_STATS.streak} unit="일" />

					{/* 총 완료 수 */}
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
						{/* 정령 이미지 */}
						<div className="h-24 w-24 shrink-0 rounded-xl bg-gray-100" />

						{/* 정령 정보 */}
						<div className="flex flex-1 flex-col gap-2">
							<div className="flex items-center justify-between">
								<div className="flex items-center gap-2">
									<span className="text-lg font-bold text-gray-800">
										{spirit.name}
									</span>
									<span className="flex h-5 w-5 items-center justify-center rounded-full bg-[#B286FD] text-xs font-bold text-white">
										{spirit.level}
									</span>
								</div>
								<span className="text-2xl font-bold text-[#B286FD]">
									{expPercent}%
								</span>
							</div>

							<span className="text-xs text-gray-400">
								EXP {spirit.exp.toLocaleString()} /{' '}
								{spirit.maxExp.toLocaleString()}
							</span>

							{/* EXP 바 */}
							<div className="h-2 w-full overflow-hidden rounded-full bg-gray-100">
								<div
									className="h-full rounded-full bg-[#B286FD] transition-all"
									style={{ width: `${expPercent}%` }}
								/>
							</div>

							{/* 버튼 */}
							<div className="mt-1 flex gap-2">
								<Link
									to="/forest"
									className="flex-1 rounded-lg border border-gray-200 py-2 text-center text-sm font-medium text-gray-600 transition-colors hover:bg-gray-50"
								>
									정령 보기
								</Link>
								<SpiritListModal representativeId={spirit.id} />
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
