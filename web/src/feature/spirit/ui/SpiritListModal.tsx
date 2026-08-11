import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { X } from 'lucide-react'
import { useState } from 'react'
import { getSpiritList } from '@/entity/spirit'
import { SpiritCard } from '@/entity/spirit/ui/SpiritCard'
import {
	Dialog,
	DialogClose,
	DialogPopup,
	DialogTitle,
	DialogTrigger,
} from '@/shared/ui/dialog'
import { changeRepresentativeSpirit } from '../api/mutate'

export function SpiritListModal({
	representativeId,
}: {
	representativeId: number
}) {
	const [selectedId, setSelectedId] = useState<number>(representativeId)
	const [open, setOpen] = useState(false)
	const queryClient = useQueryClient()

	const handleOpenChange = (next: boolean) => {
		if (next) setSelectedId(representativeId)
		setOpen(next)
	}

	const {
		data: spiritListData,
		isPending,
		isError,
	} = useQuery({
		queryKey: ['spirit', 'list'],
		queryFn: getSpiritList,
		enabled: open,
	})

	const { mutate: changeRepresentative, isPending: isChanging } = useMutation({
		mutationFn: changeRepresentativeSpirit,
		onSuccess: (res) => {
			if (res.result !== 'success') return
			queryClient.invalidateQueries({ queryKey: ['spirit', 'current'] })
			queryClient.invalidateQueries({ queryKey: ['spirit', 'list'] })
			setOpen(false)
		},
	})

	const handleConfirm = () => {
		changeRepresentative(selectedId)
	}

	return (
		<Dialog open={open} onOpenChange={handleOpenChange}>
			<DialogTrigger className="flex-1 rounded-lg bg-[#B286FD] py-2 text-sm font-medium text-white transition-opacity hover:opacity-90">
				대표 정령 변경
			</DialogTrigger>

			<DialogPopup className="flex max-h-[90vh] max-w-2xl flex-col overflow-hidden">
				<div className="mb-5 flex items-center justify-between">
					<DialogTitle>보유 정령</DialogTitle>
					<DialogClose className="flex h-7 w-7 items-center justify-center rounded-full text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600">
						<X size={16} />
					</DialogClose>
				</div>

				<div className="flex-1 overflow-y-auto">
					{isPending && (
						<p className="py-10 text-center text-sm text-gray-400">
							불러오는 중...
						</p>
					)}
					{isError && (
						<p className="py-10 text-center text-sm text-red-400">
							불러오기 실패
						</p>
					)}
					{spiritListData?.result === 'success' && (
						<div className="grid grid-cols-3 gap-3 sm:grid-cols-4">
							{spiritListData.detail.length > 0 ? (
								spiritListData.detail.map((spirit) => (
									<SpiritCard
										key={spirit.id}
										spirit={spirit}
										isRepresentative={spirit.representative}
										isSelected={selectedId === spirit.id}
										onClick={() => setSelectedId(spirit.id)}
									/>
								))
							) : (
								<p className="col-span-3 py-10 text-center text-sm text-gray-400">
									보유한 정령이 없습니다.
								</p>
							)}
						</div>
					)}
				</div>

				<div className="mt-5 flex gap-2 border-t border-gray-100 pt-5">
					<DialogClose className="flex-1 rounded-xl border border-gray-200 py-2.5 text-sm font-medium text-gray-600 transition-colors hover:bg-gray-50">
						취소
					</DialogClose>
					<button
						type="button"
						onClick={handleConfirm}
						disabled={selectedId === representativeId || isChanging}
						className="flex-1 rounded-xl bg-[#B286FD] py-2.5 text-sm font-medium text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-40"
					>
						{isChanging ? '변경 중...' : '대표 정령으로 설정'}
					</button>
				</div>
			</DialogPopup>
		</Dialog>
	)
}
