import { type ApiResponse, apiClient } from '@/lib/api'
import type { CalendarDetail, TaskScheduleParams } from '../model/type'

// 서버에서 api/task/schedule, api/task/routine이 없어지고 GET api/task 하나로 합쳐졌다
// export const getTaskSchedule = (params?: TaskScheduleParams) =>
// 	apiClient
// 		.get('api/task/schedule', { searchParams: buildSearchParams(params) })
// 		.json<ApiResponse<TaskScheduleDetail>>()

// export const getRoutineList = (params?: TaskScheduleParams) =>
// 	apiClient
// 		.get('api/task/routine', { searchParams: buildSearchParams(params) })
// 		.json<ApiResponse<TaskScheduleDetail>>()

export const getTaskCalendar = ({ from, to }: TaskScheduleParams) =>
	apiClient
		.get('api/task', { searchParams: { from, to } })
		.json<ApiResponse<CalendarDetail>>()
