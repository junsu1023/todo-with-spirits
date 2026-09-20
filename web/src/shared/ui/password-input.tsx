import { Eye, EyeOff } from 'lucide-react'
import { forwardRef, useState } from 'react'
import { cn } from '@/lib/utils'

const PasswordInput = forwardRef<
	HTMLInputElement,
	React.ComponentProps<'input'>
>(({ className, ...props }, ref) => {
	const [show, setShow] = useState(false)

	return (
		<div className="relative">
			<input
				ref={ref}
				type={show ? 'text' : 'password'}
				data-slot="input"
				className={cn(
					'h-8 w-full min-w-0 rounded-lg border border-input bg-transparent px-2.5 py-1 pr-9 text-base transition-colors outline-none placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 md:text-sm',
					className,
				)}
				{...props}
			/>
			<button
				type="button"
				onClick={() => setShow((v) => !v)}
				className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
				tabIndex={-1}
				aria-label={show ? '비밀번호 숨기기' : '비밀번호 보기'}
			>
				{show ? <EyeOff size={16} /> : <Eye size={16} />}
			</button>
		</div>
	)
})

PasswordInput.displayName = 'PasswordInput'

export { PasswordInput }
