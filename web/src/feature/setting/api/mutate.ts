import { type ApiResponse, apiClient } from '@/lib/api'
import type { DisplaySetting, UpdateDisplaySettingRequest } from '../model/type'

export const updateDisplaySetting = (body: UpdateDisplaySettingRequest) =>
	apiClient
		.patch('api/setting/display', { json: body })
		.json<ApiResponse<DisplaySetting>>()
