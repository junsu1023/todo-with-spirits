import { useMutation, useQueryClient } from '@tanstack/react-query'
import { type FormEvent, useEffect, useState } from 'react'
import type { CalendarItem } from '@/entity/task/model/type'
import type { ApiResponse } from '@/lib/api'
import { updateRoutine, updateSchedule } from '../api/mutate'
import type { DayOfWeek, RoutineRepeatType } from '../model/type'
import {
  getScheduleDate,
  getScheduleTime,
  toDaysOfWeek,
  toEndDateTime,
  toNotificationType,
  toScheduleUpdate,
} from '../model/update'
import { ImportantButton } from './ImportantButton'
import { RoutineRepeatPicker } from './RoutineRepeatPicker'

interface TaskEditSheetProps {
  item: CalendarItem
  onClose: () => void
}

/**
 * web/src/feature/task/ui/TaskEditForm.tsx를 사이드패널 하단 시트로 옮김.
 * 카테고리·공개 상태·메모는 백엔드 수정 API에 필드가 없어 저장되지 않으므로 뺐다.
 */
export function TaskEditSheet({ item, onClose }: TaskEditSheetProps) {
  const isRoutine = item.taskType === 'ROUTINE'

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  return (
    <div className="fixed inset-0 z-10 flex flex-col justify-end">
      <button
        type="button"
        aria-label="닫기"
        onClick={onClose}
        className="absolute inset-0 bg-black/30"
      />
      <div className="relative flex max-h-[90vh] flex-col gap-4 overflow-y-auto rounded-t-2xl bg-white p-4">
        <div className="flex items-center justify-between">
          <h2 className="font-semibold text-gray-800">
            {isRoutine ? '루틴 수정' : '할 일 수정'}
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="text-xs text-gray-400 hover:text-gray-600"
          >
            닫기
          </button>
        </div>
        {isRoutine ? (
          <RoutineEditForm item={item} onDone={onClose} />
        ) : (
          <ScheduleEditForm item={item} onDone={onClose} />
        )}
      </div>
    </div>
  )
}

interface EditFormProps {
  item: CalendarItem
  onDone: () => void
}

function useSave<T>(
  mutationFn: (body: T) => Promise<ApiResponse<unknown>>,
  onDone: () => void,
) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)

  const mutation = useMutation({
    mutationFn,
    onSuccess: (res) => {
      if (res.result !== 'success') {
        setError(res.detail.description[0]?.message ?? '수정하지 못했어요.')
        return
      }
      queryClient.invalidateQueries({ queryKey: ['task', 'calendar'] })
      onDone()
    },
    onError: () => setError('수정하지 못했어요. 잠시 후 다시 시도해 주세요.'),
  })

  return { ...mutation, error, setError }
}

function ScheduleEditForm({ item, onDone }: EditFormProps) {
  const [title, setTitle] = useState(item.title)
  const [isImportant, setIsImportant] = useState(item.isImportant)
  const [date, setDate] = useState(getScheduleDate(item))
  const [time, setTime] = useState(getScheduleTime(item))
  const { mutate, isPending, error, setError } = useSave(updateSchedule, onDone)

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed) return setError('할 일을 입력해 주세요.')
    if (!date) return setError('날짜를 선택해 주세요.')
    setError(null)

    mutate(
      toScheduleUpdate(item, {
        title: trimmed,
        isImportant,
        endDateTime: toEndDateTime(date, time),
        isAllDay: time === '',
      }),
    )
  }

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-3">
      <TitleInput value={title} onChange={setTitle} placeholder="할 일" />

      <div className="flex flex-col rounded-xl bg-gray-50 px-3">
        <FieldRow label="중요">
          <ImportantButton
            isImportant={isImportant}
            onToggle={() => setIsImportant((v) => !v)}
          />
        </FieldRow>
        <FieldRow label="날짜">
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="rounded-lg border border-gray-200 bg-white px-2 py-1 text-xs text-gray-700 outline-none focus:border-[#B286FD]"
          />
        </FieldRow>
        <FieldRow label="시간" last>
          <div className="flex items-center gap-2">
            <input
              type="time"
              value={time}
              onChange={(e) => setTime(e.target.value)}
              aria-label="시간 (선택)"
              className="rounded-lg border border-gray-200 bg-white px-2 py-1 text-xs text-gray-700 outline-none focus:border-[#B286FD]"
            />
            {time && (
              <button
                type="button"
                onClick={() => setTime('')}
                className="text-xs text-gray-400 hover:text-gray-600"
              >
                지우기
              </button>
            )}
          </div>
        </FieldRow>
      </div>
      <p className="px-1 text-xs text-gray-400">
        시간을 비우면 하루 종일 할 일이 돼요.
      </p>

      <SubmitArea isPending={isPending} error={error} />
    </form>
  )
}

function RoutineEditForm({ item, onDone }: EditFormProps) {
  const [title, setTitle] = useState(item.title)
  const [repeatType, setRepeatType] = useState<RoutineRepeatType>(
    item.repeatType && item.repeatType !== 'YEARLY' ? item.repeatType : 'DAILY',
  )
  const [daysOfWeek, setDaysOfWeek] = useState<DayOfWeek[]>(
    toDaysOfWeek(item.repeatDaysOfWeek),
  )
  const [daysOfMonth, setDaysOfMonth] = useState<number[]>(
    item.repeatDaysOfMonth ?? [],
  )
  const [excludeHoliday, setExcludeHoliday] = useState(
    item.excludeHoliday ?? false,
  )
  const { mutate, isPending, error, setError } = useSave(updateRoutine, onDone)

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed) return setError('루틴 이름을 입력해 주세요.')
    if (repeatType === 'WEEKLY' && daysOfWeek.length === 0)
      return setError('요일을 선택해 주세요.')
    if (repeatType === 'MONTHLY' && daysOfMonth.length === 0)
      return setError('날짜를 선택해 주세요.')
    setError(null)

    mutate({
      taskId: item.taskId,
      title: trimmed,
      repeatType,
      ...(repeatType === 'WEEKLY' && { repeatDaysOfWeek: daysOfWeek }),
      ...(repeatType === 'MONTHLY' && { repeatDaysOfMonth: daysOfMonth }),
      excludeHoliday,
      // 편집 화면에 없는 값은 기존 값을 그대로 보낸다 (안 보내면 지워짐)
      repeatEndDate: item.repeatEndDate ?? undefined,
      notificationType: toNotificationType(item.notificationMinutes),
    })
  }

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-3">
      <TitleInput value={title} onChange={setTitle} placeholder="루틴" />

      <div className="flex flex-col gap-3 rounded-xl bg-gray-50 p-3">
        <RoutineRepeatPicker
          repeatType={repeatType}
          daysOfWeek={daysOfWeek}
          daysOfMonth={daysOfMonth}
          onRepeatTypeChange={setRepeatType}
          onDaysOfWeekChange={setDaysOfWeek}
          onDaysOfMonthChange={setDaysOfMonth}
        />
        <label className="flex items-center gap-2 text-sm text-gray-500">
          <input
            type="checkbox"
            checked={excludeHoliday}
            onChange={(e) => setExcludeHoliday(e.target.checked)}
            className="size-4 accent-[#B286FD]"
          />
          공휴일 제외
        </label>
      </div>

      <SubmitArea isPending={isPending} error={error} />
    </form>
  )
}

function TitleInput({
  value,
  onChange,
  placeholder,
}: {
  value: string
  onChange: (value: string) => void
  placeholder: string
}) {
  return (
    <input
      type="text"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder={placeholder}
      maxLength={255}
      // biome-ignore lint/a11y/noAutofocus: 시트를 열면 바로 제목을 고칠 수 있게
      autoFocus
      className="border-b border-gray-200 pb-2 text-base font-medium text-gray-800 outline-none placeholder:text-gray-300 focus:border-[#B286FD]"
    />
  )
}

function FieldRow({
  label,
  last = false,
  children,
}: {
  label: string
  last?: boolean
  children: React.ReactNode
}) {
  return (
    <div
      className={`flex items-center justify-between py-2.5 ${last ? '' : 'border-b border-gray-100'}`}
    >
      <span className="text-sm text-gray-500">{label}</span>
      {children}
    </div>
  )
}

function SubmitArea({
  isPending,
  error,
}: {
  isPending: boolean
  error: string | null
}) {
  return (
    <>
      {error && <p className="px-1 text-xs text-red-400">{error}</p>}
      <button
        type="submit"
        disabled={isPending}
        className="rounded-xl bg-[#B286FD] py-2.5 text-sm font-semibold text-white disabled:opacity-60"
      >
        {isPending ? '저장 중...' : '저장'}
      </button>
    </>
  )
}
