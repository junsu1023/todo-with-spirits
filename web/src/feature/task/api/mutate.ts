import type { TaskItem } from '@/entity/task/model/type'
import { type ApiResponse, apiClient } from '@/lib/api'
import type {
	CreateRoutineRequest,
	CreateScheduleRequest,
	RoutineDetail,
} from '../model/type'

export const createSchedule = (body: CreateScheduleRequest) =>
	apiClient
		.post('api/task/schedule', { json: body })
		.json<ApiResponse<TaskItem>>()

export const createRoutine = (body: CreateRoutineRequest) =>
	apiClient
		.post('api/task/routine', { json: body })
		.json<ApiResponse<RoutineDetail>>()

export const completeTask = ({
	taskId,
	date,
}: {
	taskId: number
	date?: string
}) =>
	apiClient
		.post(`api/task/${taskId}/complete`, { json: date ? { date } : {} })
		.json<ApiResponse<null>>()

export const uncompleteTask = ({
	taskId,
	date,
}: {
	taskId: number
	date?: string
}) =>
	apiClient
		.delete(`api/task/${taskId}/complete`, { json: date ? { date } : {} })
		.json<ApiResponse<null>>()
