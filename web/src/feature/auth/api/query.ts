import { type ApiResponse, apiClient } from '@/lib/api'
import type { CheckEmailResponse } from '../model/type'

export const checkEmailApi = (email: string) =>
	apiClient
		.get('api/auth/check-email', {
			searchParams: { email },
			throwHttpErrors: false,
		})
		.json<ApiResponse<CheckEmailResponse>>()
