const STROKE_WIDTH = 10

export function RecordRing({
	percentage,
	color,
	size = 120,
}: {
	percentage: number
	color: string
	size?: number
}) {
	const radius = (size - STROKE_WIDTH) / 2
	const circumference = 2 * Math.PI * radius
	const offset = circumference - (circumference * percentage) / 100
	const center = size / 2

	return (
		<div className="relative" style={{ width: size, height: size }}>
			<svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
				<title>{`달성률 ${percentage}%`}</title>
				<circle
					cx={center}
					cy={center}
					r={radius}
					fill="none"
					stroke="#F3F4F6"
					strokeWidth={STROKE_WIDTH}
				/>
				{percentage > 0 && (
					<circle
						cx={center}
						cy={center}
						r={radius}
						fill="none"
						stroke={color}
						strokeWidth={STROKE_WIDTH}
						strokeDasharray={circumference}
						strokeDashoffset={offset}
						strokeLinecap={percentage >= 100 ? 'butt' : 'round'}
						transform={`rotate(-90 ${center} ${center})`}
					/>
				)}
			</svg>
			<span className="absolute inset-0 flex items-center justify-center text-xl font-bold text-gray-700">
				{percentage}%
			</span>
		</div>
	)
}
