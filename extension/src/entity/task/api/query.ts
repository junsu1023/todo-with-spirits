import { type ApiResponse, apiClient } from '@/lib/api'
import type { CalendarDetail, TaskScheduleParams } from '../model/type'

// 백엔드 GET api/task (from, to 필수)
export const getTaskCalendar = ({ from, to }: TaskScheduleParams) =>
  apiClient
    .get('api/task', { searchParams: { from, to } })
    .json<ApiResponse<CalendarDetail>>()
