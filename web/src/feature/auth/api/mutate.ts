import { type ApiResponse, apiClient } from '@/lib/api'
import type { LoginFormValues } from '../model/loginSchema'
import type { SignupFormValues } from '../model/signupSchema'
import type { loginResponse, SignupResponse } from '../model/type'

export const loginApi = (body: LoginFormValues) =>
	apiClient
		.post('api/auth/login', { json: body })
		.json<ApiResponse<loginResponse>>()

export const logoutApi = () =>
	apiClient.post('api/auth/logout').json<ApiResponse<null>>()

export const signupApi = (body: SignupFormValues) =>
	apiClient
		.post('api/auth/signup', { json: body, throwHttpErrors: false })
		.json<ApiResponse<SignupResponse>>()
