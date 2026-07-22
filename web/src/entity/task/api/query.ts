import { type ApiResponse, apiClient } from '@/lib/api'
import type { TaskScheduleDetail, TaskScheduleParams } from '../model/type'

function buildSearchParams(params?: TaskScheduleParams) {
	const searchParams = new URLSearchParams()
	if (params?.from) searchParams.set('from', params.from)
	if (params?.to) searchParams.set('to', params.to)
	return searchParams
}

export const getTaskSchedule = (params?: TaskScheduleParams) =>
	apiClient
		.get('api/task/schedule', { searchParams: buildSearchParams(params) })
		.json<ApiResponse<TaskScheduleDetail>>()

export const getRoutineList = (params?: TaskScheduleParams) =>
	apiClient
		.get('api/task/routine', { searchParams: buildSearchParams(params) })
		.json<ApiResponse<TaskScheduleDetail>>()
