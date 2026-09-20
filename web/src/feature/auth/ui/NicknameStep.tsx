import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { toast } from 'sonner'
import type { loginResponse } from '@/feature/auth/model/type'
import { updateMe } from '@/feature/user/api/mutate'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'

interface Props {
	pendingAuth: loginResponse
	onComplete: () => void
}

export function NicknameStep({ pendingAuth, onComplete }: Props) {
	const [nickname, setNickname] = useState('')

	const { mutate, isPending } = useMutation({
		mutationFn: async () => {
			const trimmed = nickname.trim()
			if (trimmed)
				await updateMe({ nickname: trimmed }, pendingAuth.accessToken)
		},
		onSuccess: onComplete,
		onError: () => toast.error('닉네임 저장에 실패했어요.'),
	})

	return (
		<div className="flex flex-col gap-8">
			<div className="flex flex-col gap-1">
				<h1 className="text-2xl font-bold text-gray-900">닉네임 설정</h1>
				<p className="text-sm text-gray-400">
					나중에 마이페이지에서 변경할 수 있어요.
				</p>
			</div>

			<div className="flex flex-col gap-5">
				<div className="flex flex-col gap-1.5">
					<label
						htmlFor="nickname"
						className="text-sm font-medium text-gray-700"
					>
						닉네임{' '}
						<span className="text-xs font-normal text-gray-400">(선택)</span>
					</label>
					<Input
						id="nickname"
						type="text"
						placeholder="미입력 시 자동 생성됩니다"
						value={nickname}
						onChange={(e) => setNickname(e.target.value)}
					/>
				</div>

				<Button
					size="lg"
					className="w-full"
					disabled={isPending}
					onClick={() => mutate()}
				>
					{isPending ? '저장 중...' : '시작하기'}
				</Button>
			</div>
		</div>
	)
}
