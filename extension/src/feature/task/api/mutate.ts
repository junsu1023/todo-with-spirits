import { type ApiResponse, apiClient } from '@/lib/api'
import type { CreateRoutineRequest, CreateScheduleRequest } from '../model/type'

// web/src/feature/task/api/mutate.ts와 동일
export const createSchedule = (body: CreateScheduleRequest) =>
  apiClient
    .post('api/task/schedule', { json: body })
    .json<ApiResponse<unknown>>()

export const createRoutine = (body: CreateRoutineRequest) =>
  apiClient
    .post('api/task/routine', { json: body })
    .json<ApiResponse<unknown>>()

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
