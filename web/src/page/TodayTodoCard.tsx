import { useQuery } from '@tanstack/react-query'
import { Check, Clock, Star, X } from 'lucide-react'
import { useState } from 'react'
import { getRoutineList, getTaskSchedule } from '@/entity/task/api/query'
import type { RepeatType, TaskItem } from '@/entity/task/model/type'
import { Card } from '@/shared/ui/card'
import { Popover, PopoverContent, PopoverTrigger } from '@/shared/ui/popover'

type DisplayRepeatType = '매일' | '매주' | '매월'

const DAYS = ['일', '월', '화', '수', '목', '금', '토']

const REPEAT_MAP: Record<RepeatType, DisplayRepeatType | null> = {
  DAILY: '매일',
  WEEKLY: '매주',
  MONTHLY: '매월',
  YEARLY: null,
}

const REPEAT_REVERSE_MAP: Record<DisplayRepeatType, RepeatType> = {
  매일: 'DAILY',
  매주: 'WEEKLY',
  매월: 'MONTHLY',
}

type MainTab = 'todo' | 'completed'
type InputTab = '할 일' | '루틴'

const INPUT_TABS: InputTab[] = ['할 일', '루틴']

function formatDateBadge(date: string, time?: string) {
  const d = new Date(date)
  const label = `${d.getMonth() + 1}.${d.getDate()}`
  return time ? `${label} ${time}` : label
}

function formatRepeat(repeatType: RepeatType | undefined) {
  if (!repeatType) return ''
  return REPEAT_MAP[repeatType] ?? ''
}

function CheckButton({
  checked,
  color,
  onToggle,
}: {
  checked: boolean
  color: string
  onToggle: () => void
}) {
  return (
    <button
      type="button"
      onClick={onToggle}
      style={checked ? { backgroundColor: color } : undefined}
      className={`flex size-7 shrink-0 items-center justify-center rounded-full transition-colors ${
        checked ? '' : 'border-2 border-gray-300 bg-white'
      }`}
    >
      {checked && <Check size={13} strokeWidth={3} className="text-white" />}
    </button>
  )
}

function Toggle({
  checked,
  onChange,
}: {
  checked: boolean
  onChange: () => void
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      onClick={onChange}
      className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ${
        checked ? 'bg-[#B286FD]' : 'bg-gray-200'
      }`}
    >
      <span
        aria-hidden="true"
        className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out ${
          checked ? 'translate-x-5' : 'translate-x-0'
        }`}
      />
    </button>
  )
}

interface TodayTodoCardProps {
  selectedDate: Date
}

function toDateString(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function TodayTodoCard({ selectedDate }: TodayTodoCardProps) {
  const dateStr = toDateString(selectedDate)

  const { data: todoList } = useQuery({
    queryKey: ['task', 'schedule', dateStr],
    queryFn: () => getTaskSchedule({ from: dateStr, to: dateStr }),
  })
  const { data: routineList } = useQuery({
    queryKey: ['task', 'routine', dateStr],
    queryFn: () => getRoutineList({ from: dateStr, to: dateStr }),
  })

  const [mainTab, setMainTab] = useState<MainTab>('todo')

  // toggle 오버레이 (API 아이템 XOR)
  const [completedTodoIds, setCompletedTodoIds] = useState<Set<number>>(
    new Set(),
  )
  const [starredTodoIds, setStarredTodoIds] = useState<Set<number>>(new Set())
  const [completedRoutineIds, setCompletedRoutineIds] = useState<Set<number>>(
    new Set(),
  )

  // add API 연동 전 로컬 임시 아이템
  const [localTodos, setLocalTodos] = useState<TaskItem[]>([])
  const [localRoutines, setLocalRoutines] = useState<TaskItem[]>([])

  // API 결과 직접 파생
  const apiTodos: TaskItem[] =
    todoList?.result === 'success' ? todoList.detail.items : []
  const apiRoutines: TaskItem[] =
    routineList?.result === 'success' ? routineList.detail.items : []

  const allTodos = [...apiTodos, ...localTodos]
  const allRoutines = [...apiRoutines, ...localRoutines]

  // 오버레이 적용
  const todosWithOverlay = allTodos.map((t) => ({
    ...t,
    isCompleted: completedTodoIds.has(t.taskId)
      ? !t.isCompleted
      : t.isCompleted,
    isImportant: starredTodoIds.has(t.taskId) ? !t.isImportant : t.isImportant,
  }))
  const routinesWithOverlay = allRoutines.map((r) => ({
    ...r,
    isCompleted: completedRoutineIds.has(r.taskId)
      ? !r.isCompleted
      : r.isCompleted,
  }))

  const displayedTodos =
    mainTab === 'todo'
      ? todosWithOverlay
      : todosWithOverlay.filter((t) => t.isCompleted)
  const displayedRoutines =
    mainTab === 'todo'
      ? routinesWithOverlay
      : routinesWithOverlay.filter((r) => r.isCompleted)

  // 입력 state
  const [inputText, setInputText] = useState('')
  const [inputTab, setInputTab] = useState<InputTab>('할 일')
  const [isStarred, setIsStarred] = useState(false)
  const [inputDate, setInputDate] = useState('')
  const [inputTime, setInputTime] = useState('')
  const [timeEnabled, setTimeEnabled] = useState(false)
  const [repeatType, setRepeatType] = useState<DisplayRepeatType>('매일')
  const [repeatDays, setRepeatDays] = useState<number[]>([])
  const [repeatDates, setRepeatDates] = useState<number[]>([])

  const hasDateTime = inputDate !== ''

  const clearDateTime = () => {
    setInputDate('')
    setInputTime('')
    setTimeEnabled(false)
  }

  const resetRoutineInput = () => {
    setRepeatType('매일')
    setRepeatDays([])
    setRepeatDates([])
  }

  const flipSet = (prev: Set<number>, id: number): Set<number> => {
    const next = new Set(prev)
    next.has(id) ? next.delete(id) : next.add(id)
    return next
  }

  const toggleTodo = (id: number) => {
    if (localTodos.some((t) => t.taskId === id)) {
      setLocalTodos((prev) =>
        prev.map((t) =>
          t.taskId === id ? { ...t, isCompleted: !t.isCompleted } : t,
        ),
      )
    } else {
      setCompletedTodoIds((prev) => flipSet(prev, id))
    }
  }

  const toggleStar = (id: number) => {
    if (localTodos.some((t) => t.taskId === id)) {
      setLocalTodos((prev) =>
        prev.map((t) =>
          t.taskId === id ? { ...t, isImportant: !t.isImportant } : t,
        ),
      )
    } else {
      setStarredTodoIds((prev) => flipSet(prev, id))
    }
  }

  const toggleRoutine = (id: number) => {
    if (localRoutines.some((r) => r.taskId === id)) {
      setLocalRoutines((prev) =>
        prev.map((r) =>
          r.taskId === id ? { ...r, isCompleted: !r.isCompleted } : r,
        ),
      )
    } else {
      setCompletedRoutineIds((prev) => flipSet(prev, id))
    }
  }

  const toggleRepeatDay = (day: number) =>
    setRepeatDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day],
    )

  const toggleRepeatDate = (date: number) =>
    setRepeatDates((prev) =>
      prev.includes(date) ? prev.filter((d) => d !== date) : [...prev, date],
    )

  const addItem = () => {
    if (!inputText.trim()) return
    const now = Date.now()
    if (inputTab === '루틴') {
      setLocalRoutines((prev) => [
        ...prev,
        {
          taskId: now,
          taskType: 'HABIT',
          title: inputText,
          category: null,
          memo: null,
          isAllDay: false,
          isCompleted: false,
          isImportant: false,
          isPublic: false,
          repeatType: REPEAT_REVERSE_MAP[repeatType],
        },
      ])
      resetRoutineInput()
    } else {
      setLocalTodos((prev) => [
        ...prev,
        {
          taskId: now,
          taskType: 'SCHEDULE',
          title: inputText,
          category: null,
          memo: null,
          isAllDay: false,
          isCompleted: false,
          isImportant: isStarred,
          isPublic: false,
          endDate: inputDate || undefined,
          endTime: timeEnabled && inputTime ? inputTime : undefined,
        },
      ])
      setIsStarred(false)
      clearDateTime()
    }
    setInputText('')
  }

  return (
    <Card className="flex min-h-0 flex-1 flex-col gap-5 overflow-hidden p-6">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold">Plan</h2>
        <div className="flex rounded-full bg-gray-100 p-1">
          {(['todo', 'completed'] as MainTab[]).map((tab) => (
            <button
              key={tab}
              type="button"
              onClick={() => setMainTab(tab)}
              className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
                mainTab === tab
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-400'
              }`}
            >
              {tab === 'todo' ? 'To-do' : 'Completed'}
            </button>
          ))}
        </div>
      </div>

      {/* To do + 루틴 2열 */}
      <div className="flex min-h-0 flex-1 gap-6 overflow-y-auto">
        {/* 할 일 */}
        <div className="flex flex-1 flex-col gap-4">
          <p className="font-semibold text-gray-700">To do</p>
          {displayedTodos.length === 0 && (
            <p className="text-sm text-gray-400">
              {mainTab === 'todo'
                ? '아직 등록된 할 일이 없습니다'
                : '완료된 할 일이 없습니다'}
            </p>
          )}
          {displayedTodos.map((todo) => (
            <div key={todo.taskId} className="flex items-center gap-3">
              <CheckButton
                checked={todo.isCompleted}
                color="#48CAD9"
                onToggle={() => toggleTodo(todo.taskId)}
              />
              <div className="flex flex-1 flex-col">
                <span
                  className={`text-base ${
                    todo.isCompleted
                      ? 'text-gray-400 line-through'
                      : 'text-gray-800'
                  }`}
                >
                  {todo.title}
                </span>
                {todo.endDate && (
                  <span className="text-xs text-[#B286FD]">
                    {formatDateBadge(todo.endDate, todo.endTime?.slice(0, 5))}
                  </span>
                )}
              </div>
              <button type="button" onClick={() => toggleStar(todo.taskId)}>
                <Star
                  size={16}
                  className={
                    todo.isImportant
                      ? 'fill-[#B286FD] text-[#B286FD]'
                      : 'text-gray-200'
                  }
                />
              </button>
            </div>
          ))}
        </div>

        <div className="w-px bg-gray-100" />

        {/* 루틴 */}
        <div className="flex flex-1 flex-col gap-4">
          <p className="font-semibold text-gray-700">루틴</p>
          {displayedRoutines.length === 0 && (
            <p className="text-sm text-gray-400">
              {mainTab === 'todo'
                ? '아직 등록된 루틴이 없습니다'
                : '완료한 루틴이 없습니다'}
            </p>
          )}
          {displayedRoutines.map((routine) => (
            <div key={routine.taskId} className="flex items-center gap-3">
              <CheckButton
                checked={routine.isCompleted}
                color="#B2F042"
                onToggle={() => toggleRoutine(routine.taskId)}
              />
              <div className="flex flex-1 flex-col">
                <span
                  className={`text-base ${
                    routine.isCompleted
                      ? 'text-gray-400 line-through'
                      : 'text-gray-800'
                  }`}
                >
                  {routine.title}
                </span>
                <span className="text-xs text-[#B2A042]">
                  {formatRepeat(routine.repeatType)}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 입력 도크 */}
      <div className="flex flex-col gap-3 rounded-2xl bg-gray-50 p-4">
        {/* 탭 + 별 */}
        <div className="flex items-center justify-between">
          <div className="flex gap-2">
            {INPUT_TABS.map((tab) => (
              <button
                key={tab}
                type="button"
                onClick={() => {
                  setInputTab(tab)
                  if (tab === '루틴') clearDateTime()
                  if (tab === '할 일') resetRoutineInput()
                }}
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
          {inputTab === '할 일' && (
            <button type="button" onClick={() => setIsStarred((s) => !s)}>
              <Star
                size={18}
                className={
                  isStarred ? 'fill-[#B286FD] text-[#B286FD]' : 'text-gray-300'
                }
              />
            </button>
          )}
        </div>

        {/* 텍스트 입력 */}
        <div className="flex items-center gap-2 rounded-xl bg-white px-4 py-3">
          <input
            type="text"
            placeholder={
              inputTab === '루틴'
                ? '루틴 이름을 입력하세요...'
                : '새로운 할 일을 입력하세요...'
            }
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && addItem()}
            className="flex-1 bg-transparent text-sm text-gray-700 outline-none placeholder:text-gray-300"
          />

          {inputTab === '할 일' && hasDateTime && (
            <span className="flex shrink-0 items-center gap-1 rounded-full bg-[#F4ECFF] px-2.5 py-1 text-xs text-[#B286FD]">
              {formatDateBadge(inputDate, timeEnabled ? inputTime : undefined)}
              <button
                type="button"
                onClick={clearDateTime}
                className="hover:opacity-60"
              >
                <X size={10} />
              </button>
            </span>
          )}

          {inputTab === '할 일' && (
            <Popover>
              <PopoverTrigger
                className="shrink-0 hover:opacity-70"
                aria-label="날짜 및 시간 설정"
              >
                <Clock
                  size={17}
                  className={hasDateTime ? 'text-[#B286FD]' : 'text-gray-300'}
                />
              </PopoverTrigger>
              <PopoverContent side="top" align="end" className="w-64 p-4">
                <div className="flex flex-col gap-4">
                  <div className="flex items-center justify-between gap-3">
                    <span className="shrink-0 text-sm text-gray-500">날짜</span>
                    <input
                      type="date"
                      value={inputDate}
                      onChange={(e) => setInputDate(e.target.value)}
                      className="flex-1 rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-700 outline-none focus:border-[#B286FD]"
                    />
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-500">시간</span>
                    <Toggle
                      checked={timeEnabled}
                      onChange={() => setTimeEnabled((t) => !t)}
                    />
                  </div>
                  {timeEnabled && (
                    <input
                      type="time"
                      value={inputTime}
                      onChange={(e) => setInputTime(e.target.value)}
                      className="w-full rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-700 outline-none focus:border-[#B286FD]"
                    />
                  )}
                </div>
              </PopoverContent>
            </Popover>
          )}

          <button
            type="button"
            onClick={addItem}
            className="shrink-0 rounded-lg bg-[#B286FD] px-4 py-1.5 text-sm font-medium text-white"
          >
            추가
          </button>
        </div>

        {/* 루틴 반복 설정 */}
        {inputTab === '루틴' && (
          <div className="flex flex-col gap-3 rounded-xl bg-white px-4 py-3">
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-500">반복</span>
              <div className="flex gap-1.5">
                {(['매일', '매주', '매월'] as DisplayRepeatType[]).map(
                  (type) => (
                    <button
                      key={type}
                      type="button"
                      onClick={() => setRepeatType(type)}
                      className={`rounded-full px-3 py-1 text-sm font-medium transition-colors ${
                        repeatType === type
                          ? 'bg-[#B286FD] text-white'
                          : 'border border-gray-200 text-gray-500 hover:border-[#B286FD] hover:text-[#B286FD]'
                      }`}
                    >
                      {type}
                    </button>
                  ),
                )}
              </div>
            </div>

            {repeatType === '매주' && (
              <div className="flex gap-2">
                {DAYS.map((day, i) => (
                  <button
                    key={day}
                    type="button"
                    onClick={() => toggleRepeatDay(i)}
                    className={`flex h-8 w-8 items-center justify-center rounded-full text-sm font-medium transition-colors ${
                      repeatDays.includes(i)
                        ? 'bg-[#B286FD] text-white'
                        : 'border border-gray-200 text-gray-500 hover:border-[#B286FD] hover:text-[#B286FD]'
                    }`}
                  >
                    {day}
                  </button>
                ))}
              </div>
            )}

            {repeatType === '매월' && (
              <div className="grid grid-cols-7 gap-1">
                {Array.from({ length: 31 }, (_, i) => i + 1).map((date) => (
                  <button
                    key={date}
                    type="button"
                    onClick={() => toggleRepeatDate(date)}
                    className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-medium transition-colors ${
                      repeatDates.includes(date)
                        ? 'bg-[#B286FD] text-white'
                        : 'border border-gray-100 text-gray-500 hover:border-[#B286FD] hover:text-[#B286FD]'
                    }`}
                  >
                    {date}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </Card>
  )
}
