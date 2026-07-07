import { Check, Clock, Star, X } from 'lucide-react'
import { useState } from 'react'
import { Card } from '@/shared/ui/card'
import { Popover, PopoverContent, PopoverTrigger } from '@/shared/ui/popover'

type RepeatType = '매일' | '매주' | '매월'

interface TodoItem {
  id: number
  text: string
  completed: boolean
  starred: boolean
  date?: string
  time?: string
}

interface RoutineItem {
  id: number
  text: string
  completed: boolean
  repeat: RepeatType
  repeatDays?: number[]
  repeatDates?: number[]
}

const DAYS = ['일', '월', '화', '수', '목', '금', '토']

const MOCK_TODOS: TodoItem[] = [
  { id: 1, text: '성과 보고서 제출 마감', completed: true, starred: true },
  { id: 2, text: '26년도 하반기 KPI 목표 설정', completed: true, starred: true },
  { id: 3, text: '민지랑 저녁', completed: true, starred: false, date: '2026-07-08', time: '19:00' },
  { id: 4, text: '월세 내기', completed: false, starred: false, date: '2026-07-10' },
  { id: 5, text: '비행기 티켓 끊기', completed: false, starred: false },
]

const MOCK_ROUTINES: RoutineItem[] = [
  { id: 1, text: '영어 단어 100개 외우기', completed: true, repeat: '매일' },
  { id: 2, text: '책 20 페이지 읽기', completed: true, repeat: '매주', repeatDays: [1, 3, 5] },
]

type MainTab = 'todo' | 'completed'
type InputTab = '할 일' | '루틴'

const INPUT_TABS: InputTab[] = ['할 일', '루틴']

function formatDateBadge(date: string, time?: string) {
  const d = new Date(date)
  const label = `${d.getMonth() + 1}.${d.getDate()}`
  return time ? `${label} ${time}` : label
}

function formatRepeat(routine: RoutineItem) {
  if (routine.repeat === '매일') return '매일'
  if (routine.repeat === '매주') {
    const days = (routine.repeatDays ?? []).map((d) => DAYS[d]).join(' ')
    return days ? `매주 ${days}` : '매주'
  }
  if (routine.repeat === '매월') {
    const dates = (routine.repeatDates ?? [])
      .sort((a, b) => a - b)
      .join(', ')
    return dates ? `매월 ${dates}일` : '매월'
  }
  return ''
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

function Toggle({ checked, onChange }: { checked: boolean; onChange: () => void }) {
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

export function TodayTodoCard() {
  const [mainTab, setMainTab] = useState<MainTab>('todo')
  const [todos, setTodos] = useState<TodoItem[]>(MOCK_TODOS)
  const [routines, setRoutines] = useState<RoutineItem[]>(MOCK_ROUTINES)
  const [inputText, setInputText] = useState('')
  const [inputTab, setInputTab] = useState<InputTab>('할 일')

  // 할 일 입력 state
  const [isStarred, setIsStarred] = useState(false)
  const [selectedDate, setSelectedDate] = useState('')
  const [selectedTime, setSelectedTime] = useState('')
  const [timeEnabled, setTimeEnabled] = useState(false)

  // 루틴 입력 state
  const [repeatType, setRepeatType] = useState<RepeatType>('매일')
  const [repeatDays, setRepeatDays] = useState<number[]>([])
  const [repeatDates, setRepeatDates] = useState<number[]>([])

  const displayedTodos =
    mainTab === 'todo' ? todos : todos.filter((t) => t.completed)

  const hasDateTime = selectedDate !== ''

  const clearDateTime = () => {
    setSelectedDate('')
    setSelectedTime('')
    setTimeEnabled(false)
  }

  const resetRoutineInput = () => {
    setRepeatType('매일')
    setRepeatDays([])
    setRepeatDates([])
  }

  const toggleTodo = (id: number) =>
    setTodos((prev) =>
      prev.map((t) => (t.id === id ? { ...t, completed: !t.completed } : t)),
    )

  const toggleStar = (id: number) =>
    setTodos((prev) =>
      prev.map((t) => (t.id === id ? { ...t, starred: !t.starred } : t)),
    )

  const toggleRoutine = (id: number) =>
    setRoutines((prev) =>
      prev.map((r) => (r.id === id ? { ...r, completed: !r.completed } : r)),
    )

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
    if (inputTab === '루틴') {
      setRoutines((prev) => [
        ...prev,
        {
          id: Date.now(),
          text: inputText,
          completed: false,
          repeat: repeatType,
          repeatDays: repeatType === '매주' ? repeatDays : undefined,
          repeatDates: repeatType === '매월' ? repeatDates : undefined,
        },
      ])
      resetRoutineInput()
    } else {
      setTodos((prev) => [
        ...prev,
        {
          id: Date.now(),
          text: inputText,
          completed: false,
          starred: isStarred,
          date: selectedDate || undefined,
          time: timeEnabled && selectedTime ? selectedTime : undefined,
        },
      ])
      setIsStarred(false)
      clearDateTime()
    }
    setInputText('')
  }

  return (
    <Card className="flex min-h-0 flex-1 flex-col gap-5 p-6 overflow-hidden">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold">할 일</h2>
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

      {/* To do + 루틴 2열 — 내부만 스크롤 */}
      <div className="flex min-h-0 flex-1 gap-6 overflow-y-auto">
        <div className="flex flex-1 flex-col gap-4">
          <p className="font-semibold text-gray-700">To do</p>
          {displayedTodos.map((todo) => (
            <div key={todo.id} className="flex items-center gap-3">
              <CheckButton
                checked={todo.completed}
                color="#48CAD9"
                onToggle={() => toggleTodo(todo.id)}
              />
              <div className="flex flex-1 flex-col">
                <span
                  className={`text-base ${
                    todo.completed ? 'text-gray-400 line-through' : 'text-gray-800'
                  }`}
                >
                  {todo.text}
                </span>
                {todo.date && (
                  <span className="text-xs text-[#B286FD]">
                    {formatDateBadge(todo.date, todo.time)}
                  </span>
                )}
              </div>
              <button type="button" onClick={() => toggleStar(todo.id)}>
                <Star
                  size={16}
                  className={
                    todo.starred ? 'fill-[#B286FD] text-[#B286FD]' : 'text-gray-200'
                  }
                />
              </button>
            </div>
          ))}
        </div>

        {mainTab === 'todo' && <div className="w-px bg-gray-100" />}

        {mainTab === 'todo' && (
          <div className="flex flex-1 flex-col gap-4">
            <p className="font-semibold text-gray-700">루틴</p>
            {routines.map((routine) => (
              <div key={routine.id} className="flex items-center gap-3">
                <CheckButton
                  checked={routine.completed}
                  color="#B2F042"
                  onToggle={() => toggleRoutine(routine.id)}
                />
                <div className="flex flex-1 flex-col">
                  <span
                    className={`text-base ${
                      routine.completed ? 'text-gray-400 line-through' : 'text-gray-800'
                    }`}
                  >
                    {routine.text}
                  </span>
                  <span className="text-xs text-[#B2A042]">{formatRepeat(routine)}</span>
                </div>
              </div>
            ))}
          </div>
        )}
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
                className={isStarred ? 'fill-[#B286FD] text-[#B286FD]' : 'text-gray-300'}
              />
            </button>
          )}
        </div>

        {/* 텍스트 입력 */}
        <div className="flex items-center gap-2 rounded-xl bg-white px-4 py-3">
          <input
            type="text"
            placeholder={
              inputTab === '루틴' ? '루틴 이름을 입력하세요...' : '새로운 할 일을 입력하세요...'
            }
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && addItem()}
            className="flex-1 bg-transparent text-sm text-gray-700 outline-none placeholder:text-gray-300"
          />

          {/* 날짜 뱃지 (할 일만) */}
          {inputTab === '할 일' && hasDateTime && (
            <span className="flex shrink-0 items-center gap-1 rounded-full bg-[#F4ECFF] px-2.5 py-1 text-xs text-[#B286FD]">
              {formatDateBadge(selectedDate, timeEnabled ? selectedTime : undefined)}
              <button type="button" onClick={clearDateTime} className="hover:opacity-60">
                <X size={10} />
              </button>
            </span>
          )}

          {/* 날짜/시간 Popover (할 일만) */}
          {inputTab === '할 일' && (
            <Popover>
              <PopoverTrigger className="shrink-0 hover:opacity-70" aria-label="날짜 및 시간 설정">
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
                      value={selectedDate}
                      onChange={(e) => setSelectedDate(e.target.value)}
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
                      value={selectedTime}
                      onChange={(e) => setSelectedTime(e.target.value)}
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
            {/* 반복 타입 */}
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-500">반복</span>
              <div className="flex gap-1.5">
                {(['매일', '매주', '매월'] as RepeatType[]).map((type) => (
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
                ))}
              </div>
            </div>

            {/* 매주: 요일 칩 */}
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

            {/* 매월: 날짜 그리드 */}
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
