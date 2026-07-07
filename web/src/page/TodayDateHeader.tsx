import { ChevronDown, ChevronUp } from 'lucide-react'
import { useState } from 'react'

const WEEKDAYS = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']
const WEEKDAYS_KO = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일']

// Mock dot data keyed by day-of-week index (0=Sun) for demo
const MOCK_DOT_MAP: Record<number, string[]> = {
  0: ['#B2F042', '#B286FD'],
  1: ['#B2F042', '#B286FD', '#48CAD9'],
  2: ['#B286FD'],
  3: ['#48CAD9', '#B286FD'],
  4: ['#B286FD'],
  5: ['#48CAD9', '#B286FD'],
  6: [],
}

function getWeekDates(base: Date): Date[] {
  const sunday = new Date(base)
  sunday.setDate(base.getDate() - base.getDay())
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(sunday)
    d.setDate(sunday.getDate() + i)
    return d
  })
}

function toKey(date: Date) {
  return `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`
}

function formatHeader(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}. ${m}. ${d} (${WEEKDAYS_KO[date.getDay()]})`
}

export function TodayDateHeader() {
  const [isExpanded, setIsExpanded] = useState(false)
  const [selectedDate, setSelectedDate] = useState(() => new Date())

  const today = new Date()
  const todayKey = toKey(today)
  const selectedKey = toKey(selectedDate)
  const weekDates = getWeekDates(selectedDate)

  return (
    <div className="flex flex-col gap-3">
      {/* 헤더 행 */}
      <div className="flex items-center justify-between">
        <button
          type="button"
          onClick={() => setIsExpanded((e) => !e)}
          className="flex items-center gap-2 hover:opacity-70"
        >
          <span className="text-xl font-bold">{formatHeader(selectedDate)}</span>
          {isExpanded ? (
            <ChevronUp size={20} className="text-gray-500" />
          ) : (
            <ChevronDown size={20} className="text-gray-500" />
          )}
        </button>
        <button
          type="button"
          className="text-sm text-gray-400 hover:text-[#B286FD] transition-colors"
        >
          플랜 전체 보기
        </button>
      </div>

      {/* 주간 캘린더 */}
      {isExpanded && (
        <div className="flex justify-between px-2">
          {weekDates.map((date) => {
            const key = toKey(date)
            const isSelected = key === selectedKey
            const isToday = key === todayKey
            const dots = MOCK_DOT_MAP[date.getDay()] ?? []

            return (
              <button
                key={key}
                type="button"
                onClick={() => setSelectedDate(new Date(date))}
                className="flex flex-col items-center gap-1.5"
              >
                <span className="text-xs font-medium text-gray-400">
                  {WEEKDAYS[date.getDay()]}
                </span>
                <div
                  className={`flex h-9 w-9 items-center justify-center rounded-full text-sm font-semibold transition-colors ${
                    isSelected
                      ? 'bg-[#B286FD] text-white'
                      : isToday
                        ? 'border-2 border-[#B286FD] text-[#B286FD]'
                        : 'text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  {date.getDate()}
                </div>
                <div className="flex gap-0.5">
                  {dots.map((color, i) => (
                    <div
                      // biome-ignore lint/suspicious/noArrayIndexKey: static mock list
                      key={i}
                      style={{ backgroundColor: color }}
                      className="h-1.5 w-1.5 rounded-full"
                    />
                  ))}
                </div>
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}
