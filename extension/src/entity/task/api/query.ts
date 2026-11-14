import { type ApiResponse, apiClient } from '@/lib/api'
import type { CalendarDetail, TaskScheduleParams } from '../model/type'

function buildSearchParams(params?: TaskScheduleParams) {
  const searchParams = new URLSearchParams()
  if (params?.from) searchParams.set('from', params.from)
  if (params?.to) searchParams.set('to', params.to)
  return searchParams
}

export const getTaskCalendar = (params?: TaskScheduleParams) =>
  apiClient
    .get('api/task/calendar', { searchParams: buildSearchParams(params) })
    .json<ApiResponse<CalendarDetail>>()
