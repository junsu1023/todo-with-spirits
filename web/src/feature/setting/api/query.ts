import { type ApiResponse, apiClient } from '@/lib/api'
import type { DisplaySetting } from '../model/type'

export const getDisplaySetting = () =>
	apiClient.get('api/setting/display').json<ApiResponse<DisplaySetting>>()
