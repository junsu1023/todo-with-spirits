import {
  Calendar,
  Clock,
  LayoutList,
  Lock,
  RefreshCw,
  Star,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { DropdownSelect } from '@/shared/ui/dropdown-select'
import type { PlanItem, ItemType } from './PlanPage'

const CATEGORY_OPTIONS = [
  '없음',
  '업무/커리어',
  '인간관계/약속',
  '자기계발',
  '건강',
  '취미',
]
const PUBLIC_OPTIONS = ['비공개', '공개']
const REPEAT_OPTIONS = ['매일', '매주', '매월']

function formatDateLabel(date: string, time?: string): string {
  const d = new Date(date)
  const weekdays = ['일', '월', '화', '수', '목', '금', '토']
  const y = String(d.getFullYear()).slice(2)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const wd = weekdays[d.getDay()]
  const base = `${y}. ${m}. ${day} (${wd})`
  if (time) {
    const [h, min] = time.split(':').map(Number)
    const period = h >= 12 ? 'PM' : 'AM'
    const h12 = h > 12 ? h - 12 : h === 0 ? 12 : h
    return `${base} ${period} ${String(h12).padStart(2, '0')}:${String(min).padStart(2, '0')}`
  }
  return base
}

function calcDday(date: string): number {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const target = new Date(date)
  target.setHours(0, 0, 0, 0)
  return Math.max(0, Math.ceil((target.getTime() - today.getTime()) / 86400000))
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

function Checkbox({
  checked,
  onChange,
  small,
}: {
  checked: boolean
  onChange: () => void
  small?: boolean
}) {
  return (
    <button
      type="button"
      onClick={onChange}
      className={`flex items-center justify-center rounded-md transition-colors ${
        small ? 'h-5 w-5' : 'h-6 w-6'
      } ${checked ? 'bg-[#B286FD]' : 'border-2 border-gray-300 bg-white'}`}
    >
      {checked && (
        <svg
          viewBox="0 0 10 8"
          fill="none"
          className={small ? 'h-3 w-3' : 'h-3.5 w-3.5'}
          aria-hidden="true"
        >
          <title>checked</title>
          <path
            d="M1 4L3.5 6.5L9 1"
            stroke="white"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      )}
    </button>
  )
}

function FormRow({
  icon,
  label,
  last = false,
  children,
}: {
  icon: React.ReactNode
  label: string
  last?: boolean
  children: React.ReactNode
}) {
  return (
    <div
      className={`flex items-center justify-between py-3.5 ${
        last ? '' : 'border-b border-gray-100'
      }`}
    >
      <div className="flex items-center gap-3">
        <span className="text-gray-400">{icon}</span>
        <span className="text-sm text-gray-700">{label}</span>
      </div>
      {children}
    </div>
  )
}

interface PlanItemFormProps {
  item: PlanItem
  onSave: (updated: PlanItem) => void
  onCancel: () => void
  mode?: 'create' | 'edit'
}

export function PlanItemForm({
  item,
  onSave,
  onCancel,
  mode = 'edit',
}: PlanItemFormProps) {
  const [title, setTitle] = useState(item.title)
  const [type, setType] = useState<ItemType>(item.type)
  const [starred, setStarred] = useState(item.starred)
  const [date, setDate] = useState(item.date ?? '')
  const [timeEnabled, setTimeEnabled] = useState(!!item.time)
  const [time, setTime] = useState(item.time ?? '')
  const [repeat, setRepeat] = useState<string>(item.repeat ?? '매일')
  const [excludeHolidays, setExcludeHolidays] = useState(
    item.excludeHolidays ?? false,
  )
  const [category, setCategory] = useState(item.category ?? '없음')
  const [isPublic, setIsPublic] = useState(item.isPublic ? '공개' : '비공개')
  const [memo, setMemo] = useState(item.memo ?? '')

  const isRoutine = type === 'routine'

  const handleSave = () => {
    if (!title.trim()) return
    if (!isRoutine && !date) return
    const resolvedTime = timeEnabled && time ? time : undefined
    onSave({
      ...item,
      title,
      type,
      starred,
      date: isRoutine ? undefined : date || undefined,
      time: isRoutine ? undefined : resolvedTime,
      dateLabel:
        !isRoutine && date
          ? formatDateLabel(date, resolvedTime)
          : item.dateLabel,
      dday: !isRoutine && date ? calcDday(date) : item.dday,
      repeat: isRoutine ? (repeat as '매일' | '매주' | '매월') : undefined,
      excludeHolidays: isRoutine ? excludeHolidays : undefined,
      category,
      isPublic: isPublic === '공개',
      memo: memo.trim() || undefined,
      tags: category !== '없음' ? [category] : [],
    })
  }

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-5">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <button
          type="button"
          onClick={onCancel}
          className="flex h-8 w-8 items-center justify-center rounded-full text-gray-400 hover:bg-gray-100"
        >
          <X size={18} />
        </button>
        <span className="text-base font-semibold text-gray-800">
          {mode === 'create' ? '플랜 추가' : '플랜 수정'}
        </span>
        <div className="w-8" />
      </div>

      {/* 제목 */}
      <input
        type="text"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        placeholder="제목 없음"
        className="border-b border-gray-200 pb-2 text-xl font-medium text-gray-800 outline-none placeholder:text-gray-300 focus:border-[#B286FD]"
      />

      {/* 타입 탭 */}
      <div className="flex gap-2">
        {(['todo', 'routine'] as ItemType[]).map((t) => (
          <button
            key={t}
            type="button"
            onClick={() => setType(t)}
            className={`rounded-full border px-4 py-1.5 text-sm font-medium transition-colors ${
              type === t
                ? 'border-[#B286FD] text-[#B286FD]'
                : 'border-gray-200 text-gray-400 hover:border-gray-300'
            }`}
          >
            {t === 'todo' ? 'To do' : '루틴'}
          </button>
        ))}
      </div>

      {/* 설정 행들 */}
      <div className="flex flex-col rounded-xl bg-gray-50 px-4">
        {isRoutine ? (
          <>
            {/* 반복 */}
            <FormRow icon={<RefreshCw size={16} />} label="반복">
              <DropdownSelect
                options={REPEAT_OPTIONS}
                value={repeat}
                onChange={setRepeat}
              />
            </FormRow>

            {/* 공휴일 제외 (반복 아래 서브행) */}
            <div className="flex items-center gap-2 border-b border-gray-100 py-3">
              <Checkbox
                checked={excludeHolidays}
                onChange={() => setExcludeHolidays((v) => !v)}
                small
              />
              <span className="text-sm text-gray-500">공휴일 제외</span>
            </div>

            <FormRow icon={<LayoutList size={16} />} label="카테고리">
              <DropdownSelect
                options={CATEGORY_OPTIONS}
                value={category}
                onChange={setCategory}
              />
            </FormRow>

            <FormRow icon={<Lock size={16} />} label="공개 상태" last>
              <DropdownSelect
                options={PUBLIC_OPTIONS}
                value={isPublic}
                onChange={setIsPublic}
              />
            </FormRow>
          </>
        ) : (
          <>
            <FormRow icon={<Star size={16} />} label="중요">
              <Checkbox
                checked={starred}
                onChange={() => setStarred((s) => !s)}
              />
            </FormRow>

            <FormRow icon={<Calendar size={16} />} label="날짜">
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="bg-transparent text-sm text-gray-500 outline-none"
              />
            </FormRow>

            <FormRow icon={<Clock size={16} />} label="시간">
              <div className="flex items-center gap-3">
                {timeEnabled && (
                  <input
                    type="time"
                    value={time}
                    onChange={(e) => setTime(e.target.value)}
                    className="bg-transparent text-sm text-gray-500 outline-none"
                  />
                )}
                <Toggle
                  checked={timeEnabled}
                  onChange={() => setTimeEnabled((t) => !t)}
                />
              </div>
            </FormRow>

            <FormRow icon={<LayoutList size={16} />} label="카테고리">
              <DropdownSelect
                options={CATEGORY_OPTIONS}
                value={category}
                onChange={setCategory}
              />
            </FormRow>

            <FormRow icon={<Lock size={16} />} label="공개 상태" last>
              <DropdownSelect
                options={PUBLIC_OPTIONS}
                value={isPublic}
                onChange={setIsPublic}
              />
            </FormRow>
          </>
        )}
      </div>

      {/* 메모 */}
      <textarea
        value={memo}
        onChange={(e) => setMemo(e.target.value)}
        placeholder="메모"
        rows={4}
        className="flex-1 resize-none rounded-xl bg-gray-50 p-4 text-sm text-gray-700 outline-none placeholder:text-gray-300 focus:ring-1 focus:ring-[#B286FD]"
      />

      <button
        type="button"
        onClick={handleSave}
        className="w-full rounded-xl bg-[#B286FD] py-3.5 text-sm font-semibold text-white transition-opacity hover:opacity-90"
      >
        {mode === 'create' ? '등록하기' : '수정하기'}
      </button>
    </div>
  )
}
