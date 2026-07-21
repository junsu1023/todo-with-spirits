import { z } from 'zod'

export const loginSchema = z.object({
  email: z.email({ error: '올바른 이메일 형식을 입력해주세요.' }),
  password: z
    .string()
    .min(8, '비밀번호는 8자 이상이어야 합니다.')
    .regex(/^(?=.*[a-zA-Z])(?=.*[0-9])/, '영문과 숫자를 조합해주세요.'),
})

export type LoginFormValues = z.infer<typeof loginSchema>
