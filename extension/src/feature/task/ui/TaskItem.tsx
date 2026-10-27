interface TaskItemProps {
  title: string
  isCompleted: boolean
  checkColor: string
  onToggle: () => void
  subtitle?: string
}

// web/src/feature/task/ui/TaskItem.tsx의 체크 버튼 모양을 따르고, 사이드패널용으로 단순화
export function TaskItem({
  title,
  isCompleted,
  checkColor,
  onToggle,
  subtitle,
}: TaskItemProps) {
  return (
    <li className="flex items-center gap-3 rounded-lg px-1 py-2">
      <button
        type="button"
        onClick={onToggle}
        aria-label={isCompleted ? '완료 해제' : '완료'}
        style={isCompleted ? { backgroundColor: checkColor } : undefined}
        className={`flex size-6 shrink-0 items-center justify-center rounded-full transition-colors ${
          isCompleted ? '' : 'border-2 border-gray-300 bg-white'
        }`}
      >
        {isCompleted && (
          <svg
            viewBox="0 0 24 24"
            className="size-3 fill-none stroke-white stroke-[3]"
            aria-hidden="true"
          >
            <polyline points="20 6 9 17 4 12" />
          </svg>
        )}
      </button>
      <div className="min-w-0 flex-1">
        <p
          className={`truncate text-sm ${
            isCompleted ? 'text-gray-400 line-through' : 'text-gray-800'
          }`}
        >
          {title}
        </p>
        {subtitle && <p className="text-xs text-gray-400">{subtitle}</p>}
      </div>
    </li>
  )
}
