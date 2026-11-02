# todo-with-spirits 크롬 익스텐션 기획

> "무엇을 만드는가"를 정리한 문서. "어떻게 개발하는가"는 `../CLAUDE.md` 참고.

## 1. 목적

- 사용자가 **브라우저 안에서 바로** 할 일을 확인하고 관리할 수 있게 한다.
- 웹사이트에 따로 들어가거나 데스크탑 앱을 설치하지 않아도 쓸 수 있게 한다 (데스크탑 앱을 원하지 않는 사용자도 있음).
- 처음에는 사이드패널 하나로 시작하고, 이후 기능과 형태를 넓혀 간다.

## 2. 형태

- **Chrome 사이드패널** (`chrome.sidePanel` API). ChatGPT 익스텐션처럼 툴바 아이콘을 누르면 브라우저 옆에 패널이 열린다.
- 사이드패널은 탭을 옮겨 다녀도 계속 열어 둘 수 있다.
- ⚠️ **왼쪽/오른쪽 위치는 익스텐션이 정할 수 없다.** Chrome 사용자 설정(설정 → 모양 → 사이드 패널 위치)을 따른다. 사용자가 "왼쪽"으로 설정하면 왼쪽에 열린다. → 필요하면 익스텐션 안에 이 설정 방법을 안내한다.
- 패널 폭이 좁으므로 (대략 320~500px) 웹의 2열 레이아웃 대신 **세로 1열 레이아웃**으로 만든다.

## 3. MVP 범위 (Step 1)

**오늘 날짜만** 다룬다.

| # | 기능 | 할 일 (SCHEDULE) | 루틴 (ROUTINE) |
| --- | --- | --- | --- |
| 1 | 오늘 목록 보기 | ✅ | ✅ |
| 2 | 새로 등록 | ✅ | ✅ |
| 3 | 완료 체크 / 해제 | ✅ | ✅ |
| - | 로그인 | 위 기능을 쓰려면 필수 (아래 7번 참고) | |

- **중요 표시는 MVP에서 제외** (2026-10-27 결정). 백엔드 루틴 요청에 `isImportant` 필드가 없어 루틴은 불가능하고, 할 일/루틴을 똑같이 맞추기 위해 둘 다 뺀다. 목록에서 기존 중요 상태를 보여주는 것도 하지 않는다.

### MVP에서 제외 (나중에)
- 할 일/루틴 **수정**, 삭제, 미루기
- **중요 표시** (할 일은 API로 가능하므로 나중에 추가 가능, 루틴은 백엔드 필드 추가 필요)
- 다른 날짜 보기 (어제/내일/날짜 선택)
- 정령, 숲(Unity), 기록(Record), 마이페이지/설정
- 알림, 뱃지, 단축키 등 익스텐션 전용 기능

## 4. 사용할 API (웹과 동일, 백엔드 수정 없음)

Base URL: `https://custom-lucky-draw.store` (`src/lib/config.ts`. web/.env의 실제 값과 같음)
응답 형식: `{ result: 'success', detail }` | `{ result: 'fail', detail: { status, errorCode, description[] } }`

| 기능 | 메서드 / 경로 | 웹 참고 코드 |
| --- | --- | --- |
| 오늘 목록 (할 일 + 루틴) | `GET api/task/calendar?from=YYYY-MM-DD&to=YYYY-MM-DD` (from=to=오늘) → `taskType === 'ROUTINE'`이면 루틴, 아니면 할 일 | `web/src/entity/task/api/query.ts`, `web/src/feature/task/ui/TodayTodoCard.tsx` |
| 할 일 등록 | `POST api/task/schedule` (`title`, `isAllDay`, `startDatetime`, `endDateTime`, `isImportant?`) | `web/src/feature/task/ui/ScheduleForm.tsx` |
| 루틴 등록 | `POST api/task/routine` (`title`, `repeatType`, `repeatDaysOfWeek?`, `repeatDaysOfMonth?`, ...) | `web/src/feature/task/ui/RoutineForm.tsx`, `web/src/feature/task/model/routineSchema.ts` |
| 완료 | `POST api/task/{taskId}/complete?date=YYYY-MM-DD` | `web/src/feature/task/api/mutate.ts` |
| 완료 해제 | `DELETE api/task/{taskId}/complete?date=YYYY-MM-DD` | 위와 같음 |
| 로그인 / 토큰 재발급 | `web/src/feature/auth/api/*`, `POST api/auth/reissue` | `web/src/lib/api.ts` |

- 참고: 웹의 `CreateRoutineRequest`는 알림 필드를 `notification`으로 보내지만 백엔드 필드명은 `notificationType`이다. 익스텐션에서는 `notificationType`을 쓴다 (MVP에서는 알림을 보내지 않아도 됨).

## 5. 화면 구성 (초안)

```
┌─────────────────────────┐
│ 10월 27일 (월)   [로그아웃] │  ← 오늘 날짜 헤더
├─────────────────────────┤
│ 할 일                    │
│ ☐ 보고서 쓰기            │  ← 체크 = 완료
│ ☑ 메일 답장              │
├─────────────────────────┤
│ 루틴                     │
│ ☐ 물 마시기   매일         │
├─────────────────────────┤
│ [할 일 | 루틴]  입력...  [+] │  ← 하단 입력창
└─────────────────────────┘
```

- 로그인 전에는 로그인 화면만 보인다.
- 루틴 등록은 반복 타입(매일/매주/매월)과 요일/날짜 선택이 필요하므로 간단한 폼을 연다.

## 6. 로드맵

- **Step 1 (MVP)**: 위 3번 범위 — ✅ 완료 (2026-10-28)
- **Step 2**: 다른 날짜 보기 (이전/다음 날), 할 일/루틴 수정
- **이후 후보**: 삭제·미루기, 정령/기록 화면, 툴바 뱃지(남은 할 일 수), 알림, 단축키, 팝업/새 탭 등 다른 형태

## 7. 로그인

**원하는 방식**: 로그인은 웹(`https://todo-with-spirits.vercel.app`)에서 하고, 익스텐션은 그 결과(토큰)를 받아서 쓴다.

흐름 (안):
1. 사이드패널에 "웹에서 로그인" 버튼 → 웹 `/login` 탭을 연다.
2. 사용자가 웹에서 로그인한다 (이메일, 구글, 카카오 모두 웹에서 이미 동작).
3. 웹은 토큰을 localStorage의 `auth` 키에 저장한다 (zustand persist).
4. 익스텐션의 **content script**(웹 도메인에서만 실행)가 이 값을 읽어 익스텐션으로 보낸다 → `chrome.storage.local`에 저장. **이 단계는 웹 코드를 수정하지 않아도 된다.**
5. 사이드패널이 저장된 토큰으로 API를 호출한다.

⚠️ **문제 1: refresh token 공유 시 강제 로그아웃**
- 백엔드는 refresh token을 **한 번 쓰면 폐기(rotation)** 하고, **폐기된 토큰이 다시 쓰이면 그 사용자의 모든 refresh token을 삭제**한다 (`back/.../service/AuthService.java` `reissue`).
- access token은 1시간, refresh token은 7일.
- 웹과 익스텐션이 같은 refresh token을 나눠 쓰면, 한쪽이 재발급한 뒤 다른 쪽이 옛 토큰으로 재발급을 시도하는 순간 **웹과 익스텐션 둘 다 로그아웃**된다.
- 해결 선택지:
  - (A) **백엔드에 "익스텐션용 토큰 발급" API 추가 요청** — 유효한 access token을 받으면 별도의 새 토큰 쌍을 발급. 익스텐션이 독립된 세션을 갖게 되어 가장 안정적. *(추천, 백엔드 담당에게 요청 필요)*
  - (B) 익스텐션은 refresh를 하지 않고, access token이 만료되면 웹에서 다시 받아온다 (웹 탭을 열어야 함, 1시간마다 불편할 수 있음).
  - (C) 웹과 익스텐션이 새 토큰을 서로 동기화 — 웹 탭이 닫혀 있을 때 꼬이기 쉬워서 비추천.
- **결정 (2026-10-28): MVP는 (B)로 진행한다.**
  - 익스텐션은 `api/auth/reissue`를 **절대 호출하지 않는다** (웹 세션까지 끊길 수 있으므로).
  - API가 401을 반환하면 저장된 토큰을 지우고 "웹에서 다시 로그인" 화면을 보여준다.
  - 웹 탭이 열려 있으면 content script가 웹의 최신 토큰을 다시 가져와 자동으로 갱신한다.
  - 웹이 로그아웃 상태면 익스텐션도 로그아웃된다. 익스텐션의 로그아웃 버튼은 익스텐션에서만 로그아웃한다 (웹은 그대로).
  - 익스텐션에는 access token, userId, nickname만 저장한다 (refresh token은 쓰지 않으므로 저장 안 함).
- 🔧 **개선 필요**: (B)는 임시 방식이다. 1시간마다 웹을 거쳐야 할 수 있어 불편하다. 사용자가 다른 익스텐션 레퍼런스를 조사한 뒤 (A) 등으로 개선할 예정.

⚠️ **문제 2: CORS**
- 백엔드 CORS 허용 목록에 `chrome-extension://<익스텐션 ID>`가 없다 (`back/.../config/SecurityConfig.java`).
- **확인 결과 (2026-10-28, curl)**: Origin이 `chrome-extension://...`이면 **403 "Invalid CORS request"**. `vercel.app`, `localhost:5173`이면 통과(로그인 안 한 상태라 401).
- **현재 처리**: 익스텐션의 `declarativeNetRequest` 규칙으로 API 요청의 Origin 헤더를 `https://todo-with-spirits.vercel.app`으로 바꾼다 (`src/entrypoints/background.ts`). 백엔드 수정 없이 동작. 로컬 개발/배포 모두 같은 방식. **실제 크롬에서 동작 확인 완료 (2026-10-28, 401 응답).**
- 🔧 **개선 필요**: Origin을 바꾸는 것은 우회 방법이다. 정식으로는 백엔드 허용 목록에 익스텐션 Origin(`chrome-extension://<스토어 배포 후 고정 ID>`)을 추가하고 이 규칙을 지운다.

## 8. 열린 질문

- [x] refresh token 문제 해결 방식 → MVP는 (B)
- [ ] 백엔드 변경(토큰 발급 API, CORS)을 백엔드 담당에게 요청할 수 있는지

## 9. 개선 필요 (나중에 다시 볼 것)

- 🔧 **로그인/토큰 갱신 방식** — 현재 (B) 임시 방식. 다른 익스텐션 레퍼런스 조사 후 개선 (7번 참고).
- 🔧 **CORS** — 현재 Origin 헤더를 바꾸는 우회 방식. 백엔드 허용 목록에 익스텐션 Origin 추가 후 우회 규칙 제거 (7번 문제 2).
- 🔧 **API 주소** — `web/.env`에 `VITE_API_BASE_URL`이 두 번 정의되어 있고, `web/src/lib/api.ts`의 기본값(onrender.com)은 응답하지 않는다. 어떤 주소가 정식인지 확인 필요.
