import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getTaskCalendar } from '@/entity/task/api/query'
import type {
  CalendarDetail,
  CalendarItem,
  RepeatType,
} from '@/entity/task/model/type'
import type { ApiResponse } from '@/lib/api'
import { completeTask, uncompleteTask } from '../api/mutate'
import { TaskItem } from './TaskItem'

// web TodayTodoCard와 같은 색
const TODO_COLOR = '#48CAD9'
const ROUTINE_COLOR = '#B2F042'

const REPEAT_LABEL: Record<RepeatType, string | null> = {
  DAILY: '매일',
  WEEKLY: '매주',
  MONTHLY: '매월',
  YEARLY: null,
}

interface TodayTaskListProps {
  dateStr: string
}

export function TodayTaskList({ dateStr }: TodayTaskListProps) {
  const queryClient = useQueryClient()
  const queryKey = ['task', 'calendar', dateStr, dateStr]

  const { data, isPending, isError } = useQuery({
    queryKey,
    queryFn: () => getTaskCalendar({ from: dateStr, to: dateStr }),
  })

  // 체크하면 바로 화면에 반영하고(낙관적 업데이트), 실패하면 되돌린다
  const { mutate: toggleComplete } = useMutation({
    mutationFn: (item: CalendarItem) =>
      item.isCompleted
        ? uncompleteTask({ taskId: item.taskId, date: dateStr })
        : completeTask({ taskId: item.taskId, date: dateStr }),
    onMutate: async (item) => {
      await queryClient.cancelQueries({ queryKey })
      const previous =
        queryClient.getQueryData<ApiResponse<CalendarDetail>>(queryKey)
      queryClient.setQueryData<ApiResponse<CalendarDetail>>(queryKey, (old) =>
        old?.result === 'success'
          ? {
              ...old,
              detail: {
                ...old.detail,
                items: old.detail.items.map((i) =>
                  i.taskId === item.taskId
                    ? { ...i, isCompleted: !i.isCompleted }
                    : i,
                ),
              },
            }
          : old,
      )
      return { previous }
    },
    onError: (_error, _item, context) => {
      queryClient.setQueryData(queryKey, context?.previous)
    },
    onSettled: () =>
      queryClient.invalidateQueries({ queryKey: ['task', 'calendar'] }),
  })

  if (isPending)
    return <p className="text-sm text-neutral-500">불러오는 중...</p>
  if (isError || data.result !== 'success')
    return <p className="text-sm text-red-500">할 일을 불러오지 못했어요.</p>

  const items = data.detail.items
  const todos = items.filter((i) => i.taskType !== 'ROUTINE')
  const routines = items.filter((i) => i.taskType === 'ROUTINE')

  return (
    <div className="flex flex-col gap-5">
      <TaskSection
        title="할 일"
        emptyText="오늘 할 일이 없어요"
        items={todos}
        checkColor={TODO_COLOR}
        getSubtitle={(todo) =>
          !todo.isAllDay && todo.endTime ? todo.endTime.slice(0, 5) : undefined
        }
        onToggle={toggleComplete}
      />
      <TaskSection
        title="루틴"
        emptyText="오늘 루틴이 없어요"
        items={routines}
        checkColor={ROUTINE_COLOR}
        getSubtitle={(routine) =>
          routine.repeatType
            ? (REPEAT_LABEL[routine.repeatType] ?? undefined)
            : undefined
        }
        onToggle={toggleComplete}
      />
    </div>
  )
}

interface TaskSectionProps {
  title: string
  emptyText: string
  items: CalendarItem[]
  checkColor: string
  getSubtitle: (item: CalendarItem) => string | undefined
  onToggle: (item: CalendarItem) => void
}

function TaskSection({
  title,
  emptyText,
  items,
  checkColor,
  getSubtitle,
  onToggle,
}: TaskSectionProps) {
  const completedCount = items.filter((i) => i.isCompleted).length

  return (
    <section className="flex flex-col gap-1">
      <h2 className="flex items-baseline gap-2 font-semibold text-gray-700">
        {title}
        <span className="text-xs font-normal text-gray-400">
          {completedCount}/{items.length}
        </span>
      </h2>
      {items.length === 0 ? (
        <p className="py-3 text-sm text-gray-300">{emptyText}</p>
      ) : (
        <ul className="flex flex-col">
          {items.map((item) => (
            <TaskItem
              key={item.taskId}
              title={item.title}
              isCompleted={item.isCompleted}
              checkColor={checkColor}
              subtitle={getSubtitle(item)}
              onToggle={() => onToggle(item)}
            />
          ))}
        </ul>
      )}
    </section>
  )
}
