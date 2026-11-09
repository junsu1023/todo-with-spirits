import { type ApiResponse, apiClient } from '@/lib/api'
import type {
  CreateRoutineRequest,
  CreateScheduleRequest,
  UpdateRoutineRequest,
  UpdateScheduleRequest,
} from '../model/type'

// web/src/feature/task/api/mutate.ts와 동일
export const createSchedule = (body: CreateScheduleRequest) =>
  apiClient
    .post('api/task/schedule', { json: body })
    .json<ApiResponse<unknown>>()

export const createRoutine = (body: CreateRoutineRequest) =>
  apiClient
    .post('api/task/routine', { json: body })
    .json<ApiResponse<unknown>>()

export const updateSchedule = ({ taskId, ...body }: UpdateScheduleRequest) =>
  apiClient
    .patch(`api/task/schedule/${taskId}`, { json: body })
    .json<ApiResponse<unknown>>()

export const updateRoutine = ({ taskId, ...body }: UpdateRoutineRequest) =>
  apiClient
    .patch(`api/task/routine/${taskId}`, { json: body })
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
