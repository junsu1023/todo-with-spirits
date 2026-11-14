// web/.env에서 실제로 쓰이는 API 주소와 같게 맞춘다.
// (web/src/lib/api.ts의 기본값 onrender.com은 현재 응답하지 않음)
export const API_BASE_URL = 'https://custom-lucky-draw.store'
export const API_HOST = new URL(API_BASE_URL).hostname

// 로그인을 처리하는 웹 주소
export const WEB_URL = 'https://todo-with-spirits.vercel.app'

// 백엔드 CORS 허용 목록에 있는 Origin. background.ts의 CORS 우회 규칙에서 쓴다.
export const ORIGIN_OVERRIDE = WEB_URL
