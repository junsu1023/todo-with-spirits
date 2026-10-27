import { type ApiResponse, apiClient } from '@/lib/api'

// web/src/feature/task/api/mutate.ts와 동일
export const completeTask = ({
  taskId,
  date,
}: {
  taskId: number
  date?: string
}) =>
  apiClient
    .post(`api/task/${taskId}/complete`, {
      searchParams: date ? { date } : {},
    })
    .json<ApiResponse<null>>()

export const uncompleteTask = ({
  taskId,
  date,
}: {
  taskId: number
  date?: string
}) =>
  apiClient
    .delete(`api/task/${taskId}/complete`, {
      searchParams: date ? { date } : {},
    })
    .json<ApiResponse<null>>()
