import { useMutation, useQueryClient } from '@tanstack/react-query'
import { type FormEvent, useState } from 'react'
import { createSchedule } from '../api/mutate'

interface ScheduleFormProps {
  dateStr: string
}

// 오늘 날짜로만 등록한다 (날짜 선택은 Step 2). 시간은 선택, 비우면 하루 종일
export function ScheduleForm({ dateStr }: ScheduleFormProps) {
  const queryClient = useQueryClient()
  const [title, setTitle] = useState('')
  const [time, setTime] = useState('')
  const [error, setError] = useState<string | null>(null)

  const { mutate, isPending } = useMutation({
    mutationFn: createSchedule,
    onSuccess: (res) => {
      if (res.result !== 'success') {
        setError(res.detail.description[0]?.message ?? '등록하지 못했어요.')
        return
      }
      queryClient.invalidateQueries({ queryKey: ['task', 'calendar'] })
      setTitle('')
      setTime('')
    },
    onError: () => setError('등록하지 못했어요. 잠시 후 다시 시도해 주세요.'),
  })

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed) {
      setError('할 일을 입력해 주세요.')
      return
    }
    setError(null)

    // web ScheduleForm과 같은 형식
    const isAllDay = time === ''
    mutate({
      title: trimmed,
      isAllDay,
      startDatetime: `${dateStr}T00:00:00`,
      endDateTime: isAllDay ? `${dateStr}T23:59:59` : `${dateStr}T${time}:00`,
    })
  }

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-2">
      <div className="flex items-center gap-2 rounded-xl bg-white px-3 py-2">
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="새로운 할 일"
          maxLength={255}
          className="min-w-0 flex-1 bg-transparent text-sm text-gray-700 outline-none placeholder:text-gray-300"
        />
        <input
          type="time"
          value={time}
          onChange={(e) => setTime(e.target.value)}
          aria-label="시간 (선택)"
          className="shrink-0 text-xs text-gray-500 outline-none"
        />
        <button
          type="submit"
          disabled={isPending}
          className="shrink-0 rounded-lg bg-[#B286FD] px-3 py-1.5 text-sm font-medium text-white disabled:opacity-60"
        >
          추가
        </button>
      </div>
      {error && <p className="px-1 text-xs text-red-400">{error}</p>}
    </form>
  )
}
