import { type ApiResponse, apiClient } from '@/lib/api'
import type { RepresentativeSpirit, SpiritList } from '../model/type'

// 서버 정령 API 경로가 api/spirit → api/spirits로 바뀌었다
// todo: 응답 구조도 바뀜 (대표 정령: image·trait, 목록: 정령도감 형태). 타입·화면은 따로 정리

export const getCurrentSpirit = () =>
	apiClient.get('api/spirits/current').json<ApiResponse<RepresentativeSpirit>>()

export const getSpiritList = () =>
	apiClient.get('api/spirits').json<ApiResponse<SpiritList>>()
