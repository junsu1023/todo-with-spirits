import { useRef, useState } from 'react'
import { useAuthStore } from '@/feature/auth/model/authStore'
import type { loginResponse } from '@/feature/auth/model/type'
import { CredentialsStep } from '@/feature/auth/ui/CredentialsStep'
import { NicknameStep } from '@/feature/auth/ui/NicknameStep'
import { VerificationStep } from '@/feature/auth/ui/VerificationStep'

type JoinStep = 'credentials' | 'verification' | 'nickname'

export function JoinPage() {
	const setAuth = useAuthStore((s) => s.setAuth)

	const [step, setStep] = useState<JoinStep>('credentials')
	const [email, setEmail] = useState('')
	const [password, setPassword] = useState('')
	const pendingAuthRef = useRef<loginResponse | null>(null)

	const handleVerificationSent = (email: string, password: string) => {
		setEmail(email)
		setPassword(password)
		setStep('verification')
	}

	const handleVerificationSuccess = (auth: loginResponse) => {
		pendingAuthRef.current = auth
		setStep('nickname')
	}

	const handleNicknameComplete = () => {
		if (pendingAuthRef.current) {
			setAuth(pendingAuthRef.current)
			pendingAuthRef.current = null
		}
	}

	return (
		<main className="flex h-screen items-center justify-center bg-gray-50">
			<div className="w-full max-w-sm rounded-2xl bg-white p-8 shadow-sm ring-1 ring-gray-100">
				{step === 'credentials' && (
					<CredentialsStep onVerificationSent={handleVerificationSent} />
				)}
				{step === 'verification' && (
					<VerificationStep
						email={email}
						password={password}
						onSuccess={handleVerificationSuccess}
						onBack={() => setStep('credentials')}
						onTimeout={() => setStep('credentials')}
					/>
				)}
				{step === 'nickname' && pendingAuthRef.current && (
					<NicknameStep
						pendingAuth={pendingAuthRef.current}
						onComplete={handleNicknameComplete}
					/>
				)}
			</div>
		</main>
	)
}
