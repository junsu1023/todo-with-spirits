import { Dialog as DialogPrimitive } from '@base-ui/react/dialog'
import { cn } from '@/lib/utils'

function Dialog({ ...props }: DialogPrimitive.Root.Props) {
	return <DialogPrimitive.Root {...props} />
}

function DialogTrigger({ ...props }: DialogPrimitive.Trigger.Props) {
	return <DialogPrimitive.Trigger {...props} />
}

function DialogBackdrop({
	className,
	...props
}: DialogPrimitive.Backdrop.Props) {
	return (
		<DialogPrimitive.Backdrop
			className={cn(
				'fixed inset-0 z-40 bg-black/40 backdrop-blur-sm transition-opacity duration-200 data-ending-style:opacity-0 data-starting-style:opacity-0',
				className,
			)}
			{...props}
		/>
	)
}

function DialogPopup({ className, ...props }: DialogPrimitive.Popup.Props) {
	return (
		<DialogPrimitive.Portal>
			<DialogBackdrop />
			<DialogPrimitive.Popup
				className={cn(
					'fixed top-1/2 left-1/2 z-50 w-full max-w-lg -translate-x-1/2 -translate-y-1/2 rounded-2xl bg-white p-6 shadow-xl ring-1 ring-gray-100 transition-all duration-200 data-ending-style:scale-95 data-ending-style:opacity-0 data-starting-style:scale-95 data-starting-style:opacity-0',
					className,
				)}
				{...props}
			/>
		</DialogPrimitive.Portal>
	)
}

function DialogTitle({ className, ...props }: DialogPrimitive.Title.Props) {
	return (
		<DialogPrimitive.Title
			className={cn('text-lg font-bold text-gray-800', className)}
			{...props}
		/>
	)
}

function DialogClose({ className, ...props }: DialogPrimitive.Close.Props) {
	return <DialogPrimitive.Close className={cn(className)} {...props} />
}

export { Dialog, DialogClose, DialogPopup, DialogTitle, DialogTrigger }
