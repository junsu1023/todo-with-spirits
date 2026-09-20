import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/feature/auth/model/authStore'
import { deleteMe, updateMe } from '@/feature/user/api/mutate'
import { getMe } from '@/feature/user/api/query'
import { ROUTES } from '@/shared/routes'
import { Card } from '@/shared/ui/card'
import {
	Dialog,
	DialogClose,
	DialogPopup,
	DialogTitle,
	DialogTrigger,
} from '@/shared/ui/dialog'

const PROVIDER_LABEL: Record<string, string> = {
	KAKAO: '카카오',
	GOOGLE: '구글',
}

export function AccountPage() {
	const queryClient = useQueryClient()
	const navigate = useNavigate()
	const clearAuth = useAuthStore((s) => s.clearAuth)

	const { data: meData } = useQuery({
		queryKey: ['user', 'me'],
		queryFn: getMe,
	})
	const me = meData?.result === 'success' ? meData.detail : null

	const [nickname, setNickname] = useState('')

	useEffect(() => {
		if (me?.nickname) setNickname(me.nickname)
	}, [me?.nickname])

	const { mutate: save, isPending } = useMutation({
		mutationFn: (body: Parameters<typeof updateMe>[0]) => updateMe(body),
		onSuccess: (res) => {
			if (res.result === 'success') {
				queryClient.invalidateQueries({ queryKey: ['user', 'me'] })
			}
		},
	})

	const { mutate: withdraw, isPending: isWithdrawing } = useMutation({
		mutationFn: deleteMe,
		onSuccess: () => {
			clearAuth()
			queryClient.clear()
			navigate(ROUTES.LOGIN, { replace: true })
		},
	})

	const isDirty = nickname.trim() !== (me?.nickname ?? '')
	const isValid = nickname.trim().length >= 2 && nickname.trim().length <= 12

	return (
		<div className="flex max-w-lg flex-1 flex-col gap-4 ">
			{/* 닉네임 */}
			<Card className="flex flex-col gap-4 p-6">
				<span className="text-sm font-semibold text-gray-700">닉네임</span>
				<div className="flex flex-col gap-1.5">
					<input
						type="text"
						value={nickname}
						onChange={(e) => setNickname(e.target.value)}
						maxLength={12}
						className="rounded-xl border border-gray-200 px-4 py-3 text-sm text-gray-800 outline-none transition-colors focus:border-[#B286FD]"
					/>
					<span className="text-xs text-gray-400">
						2~12자 이내로 입력해주세요
					</span>
				</div>
			</Card>

			{/* 연동 정보 */}
			<Card className="flex flex-col gap-4 p-6">
				<span className="text-sm font-semibold text-gray-700">로그인 정보</span>
				<div className="flex flex-col gap-3">
					<div className="flex items-center justify-between">
						<span className="text-sm text-gray-500">로그인 방식</span>
						<span className="text-sm font-medium text-gray-800">
							{me?.loginType === 'SOCIAL' && me.provider
								? `${PROVIDER_LABEL[me.provider] ?? me.provider} 소셜 로그인`
								: '이메일 로그인'}
						</span>
					</div>
					{me?.email && (
						<div className="flex items-center justify-between">
							<span className="text-sm text-gray-500">이메일</span>
							<span className="text-sm text-gray-800">{me.email}</span>
						</div>
					)}
				</div>
			</Card>

			{/* 탈퇴하기 */}
			<Dialog>
				<DialogTrigger className="self-start text-xs text-gray-300 underline-offset-2 transition-colors hover:text-gray-400 hover:underline cursor-pointer">
					탈퇴하기
				</DialogTrigger>
				<DialogPopup className="max-w-sm">
					<div className="flex flex-col gap-6">
						<div className="flex flex-col gap-2">
							<DialogTitle>정말 탈퇴하시겠어요?</DialogTitle>
							<p className="text-sm text-gray-500 leading-relaxed">
								탈퇴하면 모든 데이터가 삭제되며 복구할 수 없어요.
							</p>
						</div>
						<div className="flex gap-2">
							<button
								type="button"
								onClick={() => withdraw()}
								disabled={isWithdrawing}
								className="flex-1 rounded-xl bg-red-500 py-3 text-sm font-semibold text-white transition-opacity hover:bg-red-600 disabled:opacity-50 cursor-pointer"
							>
								{isWithdrawing ? '처리 중...' : '그래도 할래요'}
							</button>
							<DialogClose className="flex-1 rounded-xl border border-gray-200 py-3 text-sm font-medium text-gray-600 transition-colors hover:bg-gray-50 cursor-pointer">
								안 할래요
							</DialogClose>
						</div>
					</div>
				</DialogPopup>
			</Dialog>

			{/* 저장 버튼 */}
			<button
				type="button"
				onClick={() => save({ nickname: nickname.trim() })}
				disabled={!isDirty || !isValid || isPending}
				className="w-full rounded-xl cursor-pointer bg-[#B286FD] py-3.5 text-sm font-semibold text-white transition-opacity disabled:opacity-40"
			>
				{isPending ? '저장 중...' : '저장'}
			</button>
		</div>
	)
}
