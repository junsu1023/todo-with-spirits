import { type ApiResponse, apiClient } from '@/lib/api'
import type { UpdateUserRequest, UserProfile } from '../model/type'

export const updateMe = (body: UpdateUserRequest) =>
	apiClient
		.patch('api/user/me', { json: body })
		.json<ApiResponse<UserProfile>>()

export const deleteMe = () =>
	apiClient.delete('api/user/me').json<ApiResponse<null>>()
