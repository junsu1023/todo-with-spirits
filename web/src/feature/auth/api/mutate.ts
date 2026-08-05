import { type ApiResponse, apiClient } from '@/lib/api'
import type { LoginFormValues } from '../model/loginSchema'
import type { loginResponse } from '../model/type'

export const loginApi = (body: LoginFormValues) =>
  apiClient
    .post('api/auth/login', { json: body })
    .json<ApiResponse<loginResponse>>()

export const logoutApi = () =>
  apiClient
    .post('api/auth/logout')
    .json<ApiResponse<null>>()
