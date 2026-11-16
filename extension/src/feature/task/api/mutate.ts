import { type ApiResponse, apiClient } from '@/lib/api'
import type {
  TaskCompletionResponse,
  TaskRequest,
  TaskResponse,
  UpdateTaskRequest,
} from '../model/type'

// 백엔드 TaskController 기준. 할 일·루틴 모두 같은 API를 쓰고 repeatType으로 구분한다
export const createTask = (body: TaskRequest) =>
  apiClient.post('api/task', { json: body }).json<ApiResponse<TaskResponse>>()

export const updateTask = ({ taskId, ...body }: UpdateTaskRequest) =>
  apiClient
    .patch(`api/task/${taskId}`, { json: body })
    .json<ApiResponse<TaskResponse>>()

// date 필수: 할 일은 해당 날짜, 루틴은 수행일이어야 한다
export const completeTask = ({
  taskId,
  date,
}: {
  taskId: number
  date: string
}) =>
  apiClient
    .post(`api/task/${taskId}/complete`, { searchParams: { date } })
    .json<ApiResponse<TaskCompletionResponse>>()

export const uncompleteTask = ({
  taskId,
  date,
}: {
  taskId: number
  date: string
}) =>
  apiClient
    .delete(`api/task/${taskId}/complete`, { searchParams: { date } })
    .json<ApiResponse<null>>()
