import type { TaskItem } from '@/entity/task/model/type'
import { type ApiResponse, apiClient } from '@/lib/api'
import type {
	CreateRoutineRequest,
	CreateScheduleRequest,
	DeleteTaskRequest,
	DeleteTaskResponse,
	RoutineDetail,
	UpdateRoutineRequest,
	UpdateScheduleRequest,
} from '../model/type'

export const createSchedule = (body: CreateScheduleRequest) =>
	apiClient
		.post('api/task/schedule', { json: body })
		.json<ApiResponse<TaskItem>>()

export const createRoutine = (body: CreateRoutineRequest) =>
	apiClient
		.post('api/task/routine', { json: body })
		.json<ApiResponse<RoutineDetail>>()

export const updateRoutine = ({ taskId, ...body }: UpdateRoutineRequest) =>
	apiClient
		.patch(`api/task/routine/${taskId}`, { json: body })
		.json<ApiResponse<RoutineDetail>>()

export const updateSchedule = ({ taskId, ...body }: UpdateScheduleRequest) =>
	apiClient
		.patch(`api/task/schedule/${taskId}`, { json: body })
		.json<ApiResponse<TaskItem>>()

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

export const deleteTasks = (body: DeleteTaskRequest) =>
	apiClient
		.delete('api/task', { json: body })
		.json<ApiResponse<DeleteTaskResponse>>()
