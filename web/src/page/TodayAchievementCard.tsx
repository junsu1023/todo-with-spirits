import { Card } from "@/shared/ui/card";

const RADIUS = 70;
const STROKE_WIDTH = 14;
const SIZE = 180;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;
const cx = SIZE / 2;
const cy = SIZE / 2;

function CircularRing({ percentage }: { percentage: number }) {
	const filled = (CIRCUMFERENCE * Math.min(percentage, 97)) / 100;
	const offset = CIRCUMFERENCE - filled;

	return (
		<svg width={SIZE} height={SIZE} viewBox={`0 0 ${SIZE} ${SIZE}`} aria-label="오늘의 달성률 원형 차트">
			<title>오늘의 달성률 원형 차트</title>
			<circle
				cx={cx}
				cy={cy}
				r={RADIUS}
				fill="none"
				stroke="#E5E7EB"
				strokeWidth={STROKE_WIDTH}
			/>
			<circle
				cx={cx}
				cy={cy}
				r={RADIUS}
				fill="none"
				stroke="#B286FD"
				strokeWidth={STROKE_WIDTH}
				strokeDasharray={CIRCUMFERENCE}
				strokeDashoffset={offset}
				strokeLinecap="round"
				transform={`rotate(-90 ${cx} ${cy})`}
			/>
		</svg>
	);
}

interface TodayAchievementCardProps {
	percentage: number;
}

export function TodayAchievementCard({
	percentage,
}: TodayAchievementCardProps) {
	return (
		<Card className="flex w-full flex-col items-center p-6 gap-4">
			<span className="text-muted-foreground text-lg">오늘의 달성률</span>
			<div className="relative">
				<CircularRing percentage={percentage} />
				<div className="absolute inset-0 flex items-center justify-center">
					<span className="text-[#B286FD] text-[28px] font-bold">
						{percentage} %
					</span>
				</div>
			</div>
		</Card>
	);
}
