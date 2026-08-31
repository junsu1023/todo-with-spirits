import { z } from 'zod'

export const signupSchema = z.object({
	email: z.email({ error: '올바른 이메일 형식을 입력해주세요.' }),
	password: z
		.string()
		.min(8, '비밀번호는 8자 이상이어야 합니다.')
		.max(20, '비밀번호는 20자 이하이어야 합니다.')
		.regex(/^(?=.*[a-zA-Z])(?=.*[0-9])/, '영문과 숫자를 조합해주세요.'),
	nickname: z
		.string()
		.refine((v) => v === '' || (v.length >= 2 && v.length <= 12), {
			message: '닉네임은 2~12자로 입력해주세요.',
		})
		.optional(),
})

export type SignupFormValues = z.infer<typeof signupSchema>
