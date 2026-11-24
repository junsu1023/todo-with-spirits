// todo: 서버에서 기록 API(api/record/*)가 삭제됨 (back becd231). 기록 탭은 라우터·사이드바에서 빠져 있어 호출되지 않는다
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
