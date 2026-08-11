import { type ApiResponse, apiClient } from '@/lib/api'
import type { RepresentativeSpirit, SpiritList } from '../model/type'

export const getCurrentSpirit = () =>
	apiClient.get('api/spirit/current').json<ApiResponse<RepresentativeSpirit>>()

export const getSpiritList = () =>
	apiClient.get('api/spirit').json<ApiResponse<SpiritList>>()
