import { apiClient } from '@/lib/api'
import type { LoginFormValues } from '../model/loginSchema'

export const loginApi = (body: LoginFormValues) =>
  apiClient.post('api/auth/login', { json: body }).json()
