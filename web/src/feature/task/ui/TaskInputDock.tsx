import { useState } from 'react'
import { RoutineForm } from './RoutineForm'
import { ScheduleForm } from './ScheduleForm'

type InputTab = '할 일' | '루틴'
const INPUT_TABS: InputTab[] = ['할 일', '루틴']

interface TaskInputDockProps {
	dateStr: string
}

export function TaskInputDock({ dateStr }: TaskInputDockProps) {
	const [inputTab, setInputTab] = useState<InputTab>('할 일')

	return (
		<div className="flex flex-col gap-3 rounded-2xl bg-gray-50 p-4">
			<div className="flex gap-2">
				{INPUT_TABS.map((tab) => (
					<button
						key={tab}
						type="button"
						onClick={() => setInputTab(tab)}
						className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
							inputTab === tab
								? 'bg-[#B286FD] text-white'
								: 'border border-gray-200 bg-white text-gray-600'
						}`}
					>
						{tab}
					</button>
				))}
			</div>

			{inputTab === '할 일' ? (
				<ScheduleForm dateStr={dateStr} />
			) : (
				<RoutineForm dateStr={dateStr} />
			)}
		</div>
	)
}
