import { type ApiResponse, apiClient } from '@/lib/api'
import type { UpdateUserRequest, UserProfile } from '../model/type'

export const updateMe = (body: UpdateUserRequest, authToken?: string) =>
	apiClient
		.patch('api/user/me', {
			json: body,
			...(authToken
				? { headers: { Authorization: `Bearer ${authToken}` } }
				: {}),
		})
		.json<ApiResponse<UserProfile>>()

export const deleteMe = () =>
	apiClient.delete('api/user/me').json<ApiResponse<null>>()
