import { Sparkles } from 'lucide-react'
import { CATEGORY_LABEL } from '@/entity/task/model/category'
import sampleSpiritImage from '@/shared/assets/sample-spirit.png'
import { toPercent } from '../lib/progress'
import type { MonthlyCategoryCount, MonthlyRecordDetail } from '../model/type'
import { RecordCard } from './RecordCard'
import { CATEGORY_ICON, type RankItem, RecordRankList } from './RecordRankList'

// android와 동일: Top 3는 '완료/전체'
const toTopRankItem = ({
	category,
	completedCount,
	totalCount,
}: MonthlyCategoryCount): RankItem => ({
	key: category,
	icon: CATEGORY_ICON[category] ?? CATEGORY_ICON.NONE,
	label: CATEGORY_LABEL[category] ?? CATEGORY_LABEL.NONE,
	value: `${completedCount}/${totalCount}`,
})

// android와 동일: 놓친 횟수 = 전체 - 완료
const toMissedRankItem = ({
	category,
	completedCount,
	totalCount,
}: MonthlyCategoryCount): RankItem => ({
	key: category,
	icon: CATEGORY_ICON[category] ?? CATEGORY_ICON.NONE,
	label: CATEGORY_LABEL[category] ?? CATEGORY_LABEL.NONE,
	value: `${Math.max(totalCount - completedCount, 0)}회`,
})

function MonthlyAnalysisHero({
	record,
	spiritImageUrl,
}: {
	record: MonthlyRecordDetail
	spiritImageUrl?: string
}) {
	const { mainCategory } = record

	return (
		<div className="flex flex-col items-center gap-6 rounded-xl bg-brand-muted p-5 sm:flex-row">
			<div className="relative shrink-0">
				<img
					src={spiritImageUrl || sampleSpiritImage}
					alt=""
					className="size-32 object-contain"
					onError={(e) => {
						e.currentTarget.src = sampleSpiritImage
					}}
				/>
				<Sparkles
					size={20}
					className="absolute -top-1 left-0 fill-[#FFE766] text-[#F5D000]"
				/>
				{/* todo: mainCategoryCompletionRate를 0~1 비율로 가정 (android 표시 기준) */}
				<span className="absolute -right-6 -top-2 flex size-16 flex-col items-center justify-center rounded-full bg-lime text-center leading-tight">
					<span className="text-[10px] text-gray-600">달성률</span>
					<span className="text-base font-bold text-gray-800">
						{toPercent(record.mainCategoryCompletionRate)}%
					</span>
				</span>
			</div>
			<div className="flex flex-col gap-2">
				{mainCategory && (
					<span className="w-fit rounded-full bg-white px-3 py-1 text-[11px] text-brand">
						{CATEGORY_LABEL[mainCategory]} 분야 · 또래 상위{' '}
						{record.mainCategoryPeerPercentile}%
					</span>
				)}
				<span className="text-lg font-bold text-gray-800">{record.title}</span>
				<span className="whitespace-pre-line text-xs text-gray-500">
					{record.content}
				</span>
			</div>
		</div>
	)
}

interface MonthlyAnalysisCardProps {
	record: MonthlyRecordDetail
	spiritImageUrl?: string
}

export function MonthlyAnalysisCard({ record, spiritImageUrl }: MonthlyAnalysisCardProps) {
	const topItems = (record.topCategories ?? []).map(toTopRankItem)
	const missedItems = record.bottomCategory ? [toMissedRankItem(record.bottomCategory)] : []

	return (
		<RecordCard title="월간 분석">
			<MonthlyAnalysisHero record={record} spiritImageUrl={spiritImageUrl} />
			<div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
				<RecordRankList title="월간 실천 Top 3" items={topItems} />
				<RecordRankList title="자주 놓친 분야" items={missedItems} />
			</div>
		</RecordCard>
	)
}
