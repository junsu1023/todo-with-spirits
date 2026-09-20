import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/feature/auth/model/authStore'
import {
	deleteMe,
	sendVerificationEmailForMe,
	updateMe,
} from '@/feature/user/api/mutate'
import { getMe, getMySubscription } from '@/feature/user/api/query'
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

function formatDate(iso: string) {
	const d = new Date(iso)
	return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
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

	const { data: subData } = useQuery({
		queryKey: ['user', 'subscription'],
		queryFn: getMySubscription,
		enabled: !!me?.premium,
	})
	const subscription = subData?.result === 'success' ? subData.detail : null

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

	const [resendSuccess, setResendSuccess] = useState(false)

	const { mutate: resend, isPending: isResending } = useMutation({
		mutationFn: sendVerificationEmailForMe,
		onSuccess: (res) => {
			if (res.result === 'success') setResendSuccess(true)
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

			{/* 플랜 */}
			<Dialog>
				<Card className="flex flex-col gap-4 p-6">
					<span className="text-sm font-semibold text-gray-700">플랜</span>
					{me?.premium ? (
						<div className="flex items-center justify-between">
							<div className="flex items-center gap-2">
								<span className="rounded-full bg-[#F3E8FF] px-2.5 py-0.5 text-xs font-semibold text-[#B286FD]">
									프리미엄
								</span>
								<span className="text-sm text-gray-700">프리미엄 이용 중</span>
							</div>
							<DialogTrigger className="text-xs text-gray-400 hover:text-gray-600">
								상세 보기
							</DialogTrigger>
						</div>
					) : (
						<div className="flex items-center justify-between">
							<span className="text-sm text-gray-500">
								현재 무료 플랜 이용 중
							</span>
							<DialogTrigger className="rounded-lg bg-[#B286FD] px-3 py-1.5 text-xs font-semibold text-white hover:opacity-90">
								유료 플랜 보기
							</DialogTrigger>
						</div>
					)}
				</Card>
				<DialogPopup className="max-w-sm">
					<div className="flex flex-col gap-4">
						<DialogTitle>
							{me?.premium ? '프리미엄 플랜' : '유료 플랜 안내'}
						</DialogTitle>
						{me?.premium && subscription ? (
							<div className="flex flex-col gap-3">
								<div className="flex items-center justify-between">
									<span className="text-sm text-gray-500">구독 유형</span>
									<span className="text-sm font-medium text-gray-800">
										{subscription.planType === 'MONTHLY'
											? '월간 구독'
											: '연간 구독'}
									</span>
								</div>
								<div className="flex items-center justify-between">
									<span className="text-sm text-gray-500">시작일</span>
									<span className="text-sm text-gray-800">
										{formatDate(subscription.startedAt)}
									</span>
								</div>
								<div className="flex items-center justify-between">
									<span className="text-sm text-gray-500">만료일</span>
									<span className="text-sm text-gray-800">
										{formatDate(subscription.expiresAt)}
									</span>
								</div>
								<div className="flex items-center justify-between">
									<span className="text-sm text-gray-500">자동 갱신</span>
									<span className="text-sm text-gray-800">
										{subscription.autoRenew ? '켜짐' : '꺼짐'}
									</span>
								</div>
							</div>
						) : (
							<p className="text-sm text-gray-400">plan 관련 내용 추가 예정</p>
						)}
						<DialogClose className="w-full rounded-xl border border-gray-200 py-3 text-sm font-medium text-gray-600 hover:bg-gray-50">
							닫기
						</DialogClose>
					</div>
				</DialogPopup>
			</Dialog>

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
					{me?.email ? (
						<>
							<div className="flex items-center justify-between">
								<span className="text-sm text-gray-500">이메일</span>
								<span className="text-sm text-gray-800">{me.email}</span>
							</div>
							<div className="flex items-center justify-between">
								<span className="text-sm text-gray-500">이메일 인증</span>
								<div className="flex items-center gap-2">
									{me.emailVerificationStatus === 'VERIFIED' ? (
										<span className="rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-600">
											인증 완료
										</span>
									) : (
										<>
											<span className="rounded-full bg-orange-100 px-2.5 py-0.5 text-xs font-medium text-orange-500">
												{me.emailVerificationStatus === 'PENDING'
													? '인증 대기'
													: '미인증'}
											</span>
											{resendSuccess ? (
												<span className="text-xs text-green-600">발송됨</span>
											) : (
												<button
													type="button"
													onClick={() => resend()}
													disabled={isResending}
													className="text-xs text-[#B286FD] hover:opacity-70 disabled:opacity-50"
												>
													{isResending ? '발송 중...' : '인증 메일 발송'}
												</button>
											)}
										</>
									)}
								</div>
							</div>
						</>
					) : (
						me?.loginType === 'SOCIAL' && (
							<div className="rounded-xl bg-orange-50 p-3 flex flex-col gap-1">
								<span className="text-sm font-medium text-orange-700">
									이메일이 연결되어 있지 않아요
								</span>
								<span className="text-xs text-orange-400">
									소셜 계정에 이메일 정보가 없어 일부 기능이 제한될 수 있어요.
								</span>
							</div>
						)
					)}
					{me?.createdAt && (
						<div className="flex items-center justify-between">
							<span className="text-sm text-gray-500">가입일</span>
							<span className="text-sm text-gray-800">
								{formatDate(me.createdAt)}
							</span>
						</div>
					)}
				</div>
			</Card>

			{/* 탈퇴하기 */}
			<Dialog>
				<DialogTrigger className="self-start text-xs text-gray-300 underline-offset-2 transition-colors hover:text-gray-400 hover:underline">
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
								className="flex-1 rounded-xl bg-red-500 py-3 text-sm font-semibold text-white transition-opacity hover:bg-red-600 disabled:opacity-50"
							>
								{isWithdrawing ? '처리 중...' : '그래도 할래요'}
							</button>
							<DialogClose className="flex-1 rounded-xl border border-gray-200 py-3 text-sm font-medium text-gray-600 transition-colors hover:bg-gray-50">
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
				className="w-full rounded-xl bg-[#B286FD] py-3.5 text-sm font-semibold text-white transition-opacity disabled:opacity-40"
			>
				{isPending ? '저장 중...' : '저장'}
			</button>
		</div>
	)
}
