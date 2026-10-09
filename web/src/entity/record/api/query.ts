import { type ApiResponse, apiClient } from '@/lib/api'
import type {
	DailyRecordDetail,
	MonthlyRecordDetail,
	RecordParams,
	WeeklyRecordDetail,
} from '../model/type'

export const getTodayRecord = () =>
	apiClient.get('api/record/today').json<ApiResponse<DailyRecordDetail>>()

export const getWeeklyRecord = ({ date }: RecordParams) =>
	apiClient
		.get('api/record/weekly', { searchParams: { date } })
		.json<ApiResponse<WeeklyRecordDetail>>()

export const getMonthlyRecord = ({ date }: RecordParams) =>
	apiClient
		.get('api/record/monthly', { searchParams: { date } })
		.json<ApiResponse<MonthlyRecordDetail>>()
