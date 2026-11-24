import { type ApiResponse, apiClient } from '@/lib/api'

export const changeRepresentativeSpirit = (id: number) =>
	apiClient
		.patch('api/spirits/current', { json: { id } })
		.json<ApiResponse<null>>()
