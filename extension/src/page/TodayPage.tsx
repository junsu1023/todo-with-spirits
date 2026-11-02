import { TaskInputDock } from '@/feature/task/ui/TaskInputDock'
import { TodayTaskList } from '@/feature/task/ui/TodayTaskList'
import { toDateString } from '@/shared/lib/date'

// MVP는 오늘 날짜만 본다. 다른 날짜 보기는 Step 2 (docs/spec.md)
export function TodayPage() {
  const dateStr = toDateString(new Date())

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-3">
      <div className="min-h-0 flex-1 overflow-y-auto">
        <TodayTaskList dateStr={dateStr} />
      </div>
      <TaskInputDock dateStr={dateStr} />
    </div>
  )
}
