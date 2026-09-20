import { type ApiResponse, apiClient } from '@/lib/api'
import type { SubscriptionDetail, UserProfile } from '../model/type'

export const getMe = () =>
	apiClient.get('api/user/me').json<ApiResponse<UserProfile>>()

export const getMySubscription = () =>
	apiClient
		.get('api/user/me/subscription', { throwHttpErrors: false })
		.json<ApiResponse<SubscriptionDetail>>()
