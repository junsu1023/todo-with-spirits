import { type ApiResponse, apiClient } from '@/lib/api'
import type {
	DeleteTaskRequest,
	DeleteTaskResponse,
	PostponeTaskRequest,
	PostponeTaskResponse,
	TaskCompletionResponse,
	TaskRequest,
	TaskResponse,
	UpdateTaskRequest,
} from '../model/type'

// 서버 TaskController 기준. 할 일·루틴 모두 같은 API를 쓰고 repeatType으로 구분한다
export const createTask = (body: TaskRequest) =>
	apiClient.post('api/task', { json: body }).json<ApiResponse<TaskResponse>>()

export const updateTask = ({ taskId, ...body }: UpdateTaskRequest) =>
	apiClient
		.patch(`api/task/${taskId}`, { json: body })
		.json<ApiResponse<TaskResponse>>()

// 서버에서 schedule/routine API가 없어지고 위 createTask, updateTask로 합쳐졌다
// export const createSchedule = (body: CreateScheduleRequest) =>
// 	apiClient
// 		.post('api/task/schedule', { json: body })
// 		.json<ApiResponse<TaskItem>>()

// export const createRoutine = (body: CreateRoutineRequest) =>
// 	apiClient
// 		.post('api/task/routine', { json: body })
// 		.json<ApiResponse<RoutineDetail>>()

// export const updateRoutine = ({ taskId, ...body }: UpdateRoutineRequest) =>
// 	apiClient
// 		.patch(`api/task/routine/${taskId}`, { json: body })
// 		.json<ApiResponse<RoutineDetail>>()

// export const updateSchedule = ({ taskId, ...body }: UpdateScheduleRequest) =>
// 	apiClient
// 		.patch(`api/task/schedule/${taskId}`, { json: body })
// 		.json<ApiResponse<TaskItem>>()

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

export const postponeTask = ({ taskId, ...body }: PostponeTaskRequest) =>
	apiClient
		.patch(`api/task/${taskId}/postpone`, {
			json: body,
			throwHttpErrors: false,
		})
		.json<ApiResponse<PostponeTaskResponse>>()

export const deleteTasks =(body: DeleteTaskRequest) =>
	apiClient
		.delete('api/task', { json: body })
		.json<ApiResponse<DeleteTaskResponse>>()
