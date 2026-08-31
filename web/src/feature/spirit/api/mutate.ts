import { type ApiResponse, apiClient } from '@/lib/api'

export const changeRepresentativeSpirit = (id: number) =>
	apiClient
		.patch('api/spirit/current', { json: { id } })
		.json<ApiResponse<null>>()
