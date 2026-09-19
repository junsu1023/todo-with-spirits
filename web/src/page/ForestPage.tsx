import { useEffect, useState } from 'react'

const LOADING_DURATION_MS = 5000

export function ForestPage() {
	const [isLoading, setIsLoading] = useState(true)

	useEffect(() => {
		const timer = setTimeout(() => setIsLoading(false), LOADING_DURATION_MS)
		return () => clearTimeout(timer)
	}, [])

	return (
		<div className="relative flex-1 min-h-0 w-full overflow-hidden bg-[#2c4a1e]">
			<iframe
				src="/forest-webgl/WebGL/index.html"
				title="TodoSpirits Forest"
				className="h-full w-full border-0 block"
				allow="autoplay; fullscreen"
			/>
			{isLoading && (
				<div className="absolute inset-0 flex flex-col items-center justify-center bg-[#2c4a1e] z-10">
					<div className="flex flex-col items-center gap-4">
						<div className="h-12 w-12 rounded-full border-4 border-green-300 border-t-transparent animate-spin" />
						<p className="text-green-200 text-sm">숲을 불러오는 중...</p>
					</div>
				</div>
			)}
		</div>
	)
}

export default ForestPage
