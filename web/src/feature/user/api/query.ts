import { type ApiResponse, apiClient } from '@/lib/api'
import type { UserProfile } from '../model/type'

export const getMe = () =>
	apiClient.get('api/user/me').json<ApiResponse<UserProfile>>()
