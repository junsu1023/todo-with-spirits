interface ImportantButtonProps {
  isImportant: boolean
  onToggle: () => void
}

// web TodayTodoCard의 별(lucide Star)과 같은 모양·색. 익스텐션에는 lucide가 없어 SVG로 그린다
export function ImportantButton({
  isImportant,
  onToggle,
}: ImportantButtonProps) {
  return (
    <button
      type="button"
      onClick={onToggle}
      aria-label={isImportant ? '중요 해제' : '중요 표시'}
      aria-pressed={isImportant}
      className={`shrink-0 p-1 ${
        isImportant
          ? 'fill-[#B286FD] text-[#B286FD]'
          : 'fill-none text-gray-200 hover:text-gray-300'
      }`}
    >
      <svg
        viewBox="0 0 24 24"
        className="size-4 stroke-current stroke-2"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
      </svg>
    </button>
  )
}
