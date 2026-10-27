import { useMutation, useQueryClient } from '@tanstack/react-query'
import { type FormEvent, useState } from 'react'
import { createRoutine } from '../api/mutate'
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

// web/src/feature/task/ui/RoutineForm.tsx를 사이드패널용으로 단순화 (react-hook-form/zod 없이)
export function RoutineForm() {
  const queryClient = useQueryClient()
  const [title, setTitle] = useState('')
  const [repeatType, setRepeatType] = useState<RoutineRepeatType>('DAILY')
  const [daysOfWeek, setDaysOfWeek] = useState<DayOfWeek[]>([])
  const [daysOfMonth, setDaysOfMonth] = useState<number[]>([])
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const { mutate, isPending } = useMutation({
    mutationFn: createRoutine,
    onSuccess: (res) => {
      if (res.result !== 'success') {
        setError(res.detail.description[0]?.message ?? '등록하지 못했어요.')
        return
      }
      queryClient.invalidateQueries({ queryKey: ['task', 'calendar'] })
      // 오늘이 반복 요일/날짜가 아니면 목록에 안 보이므로, 등록됐다는 것을 따로 알려준다
      setNotice(`'${title.trim()}' 루틴을 등록했어요.`)
      setTitle('')
      setDaysOfWeek([])
      setDaysOfMonth([])
    },
    onError: () => setError('등록하지 못했어요. 잠시 후 다시 시도해 주세요.'),
  })

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    setNotice(null)
    const trimmed = title.trim()
    if (!trimmed) return setError('루틴 이름을 입력해 주세요.')
    if (repeatType === 'WEEKLY' && daysOfWeek.length === 0)
      return setError('요일을 선택해 주세요.')
    if (repeatType === 'MONTHLY' && daysOfMonth.length === 0)
      return setError('날짜를 선택해 주세요.')
    setError(null)

    mutate({
      title: trimmed,
      repeatType,
      ...(repeatType === 'WEEKLY' && { repeatDaysOfWeek: daysOfWeek }),
      ...(repeatType === 'MONTHLY' && { repeatDaysOfMonth: daysOfMonth }),
    })
  }

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-2">
      <div className="flex items-center gap-2 rounded-xl bg-white px-3 py-2">
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="새로운 루틴"
          maxLength={255}
          className="min-w-0 flex-1 bg-transparent text-sm text-gray-700 outline-none placeholder:text-gray-300"
        />
        <button
          type="submit"
          disabled={isPending}
          className="shrink-0 rounded-lg bg-[#B286FD] px-3 py-1.5 text-sm font-medium text-white disabled:opacity-60"
        >
          추가
        </button>
      </div>

      <div className="flex flex-col gap-2 rounded-xl bg-white px-3 py-2">
        <div className="flex gap-1.5">
          {REPEAT_OPTIONS.map((option) => (
            <button
              key={option.value}
              type="button"
              onClick={() => setRepeatType(option.value)}
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
                onClick={() => setDaysOfWeek(toggle(daysOfWeek, day.value))}
                className={`flex size-7 items-center justify-center rounded-full text-xs font-medium transition-colors ${
                  daysOfWeek.includes(day.value)
                    ? selectedClass
                    : unselectedClass
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
                onClick={() => setDaysOfMonth(toggle(daysOfMonth, date))}
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

      {error && <p className="px-1 text-xs text-red-400">{error}</p>}
      {notice && <p className="px-1 text-xs text-gray-500">{notice}</p>}
    </form>
  )
}
