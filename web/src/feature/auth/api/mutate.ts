import { type ApiResponse, apiClient } from '@/lib/api'
import type { LoginFormValues } from '../model/loginSchema'
import type { SignupFormValues } from '../model/signupSchema'
import type {
	loginResponse,
	SignupResponse,
	SocialLoginRequest,
	SocialLoginResponse,
} from '../model/type'

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

export const socialLoginApi = (
	providerAccessToken: string,
	body: SocialLoginRequest,
) =>
	apiClient
		.post('api/auth/social/login', {
			json: body,
			headers: { Authorization: `Bearer ${providerAccessToken}` },
		})
		.json<ApiResponse<SocialLoginResponse>>()
