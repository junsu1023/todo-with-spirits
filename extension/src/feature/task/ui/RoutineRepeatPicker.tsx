import type { DayOfWeek, RoutineRepeatType } from '../model/type'

const REPEAT_OPTIONS: { label: string; value: RoutineRepeatType }[] = [
  { label: '매일', value: 'DAILY' },
  { label: '매주', value: 'WEEKLY' },
  { label: '매월', value: 'MONTHLY' },
]

const DAYS: { label: string; value: DayOfWeek }[] = [
  { label: '일', value: 'SUNDAY' },
  { label: '월', value: 'MONDAY' },
  { label: '화', value: 'TUESDAY' },
  { label: '수', value: 'WEDNESDAY' },
  { label: '목', value: 'THURSDAY' },
  { label: '금', value: 'FRIDAY' },
  { label: '토', value: 'SATURDAY' },
]

const selectedClass = 'bg-[#B286FD] text-white'
const unselectedClass =
  'border border-gray-200 text-gray-500 hover:border-[#B286FD] hover:text-[#B286FD]'

function toggle<T>(list: T[], value: T) {
  return list.includes(value)
    ? list.filter((v) => v !== value)
    : [...list, value]
}

interface RoutineRepeatPickerProps {
  repeatType: RoutineRepeatType
  daysOfWeek: DayOfWeek[]
  daysOfMonth: number[]
  onRepeatTypeChange: (repeatType: RoutineRepeatType) => void
  onDaysOfWeekChange: (days: DayOfWeek[]) => void
  onDaysOfMonthChange: (days: number[]) => void
}

// 루틴 등록(RoutineForm)과 수정(TaskEditSheet)에서 같이 쓰는 반복 선택
export function RoutineRepeatPicker({
  repeatType,
  daysOfWeek,
  daysOfMonth,
  onRepeatTypeChange,
  onDaysOfWeekChange,
  onDaysOfMonthChange,
}: RoutineRepeatPickerProps) {
  return (
    <div className="flex flex-col gap-2">
      <div className="flex gap-1.5">
        {REPEAT_OPTIONS.map((option) => (
          <button
            key={option.value}
            type="button"
            onClick={() => onRepeatTypeChange(option.value)}
            className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
              repeatType === option.value ? selectedClass : unselectedClass
            }`}
          >
            {option.label}
          </button>
        ))}
      </div>

      {repeatType === 'WEEKLY' && (
        <div className="flex gap-1">
          {DAYS.map((day) => (
            <button
              key={day.value}
              type="button"
              onClick={() => onDaysOfWeekChange(toggle(daysOfWeek, day.value))}
              className={`flex size-7 items-center justify-center rounded-full text-xs font-medium transition-colors ${
                daysOfWeek.includes(day.value) ? selectedClass : unselectedClass
              }`}
            >
              {day.label}
            </button>
          ))}
        </div>
      )}

      {repeatType === 'MONTHLY' && (
        <div className="grid grid-cols-7 gap-1">
          {Array.from({ length: 31 }, (_, i) => i + 1).map((date) => (
            <button
              key={date}
              type="button"
              onClick={() => onDaysOfMonthChange(toggle(daysOfMonth, date))}
              className={`flex size-7 items-center justify-center rounded-full text-xs font-medium transition-colors ${
                daysOfMonth.includes(date) ? selectedClass : unselectedClass
              }`}
            >
              {date}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
