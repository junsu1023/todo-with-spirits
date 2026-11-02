# CLAUDE.md — todo-with-spirits / extension

이 폴더는 기존 **todo-with-spirits 웹앱(`../web`)을 크롬 익스텐션(Manifest V3)으로 옮기는 작업 공간**이다.
Claude는 이 문서를 작업 전 기준으로 삼는다.

- **무엇을 만드는지 (기능·범위·로드맵)** → [`docs/spec.md`](docs/spec.md). 기능 작업 전에 반드시 읽고, 범위 밖 기능은 만들지 않는다.
- 형태: **Chrome 사이드패널** / MVP: **오늘 날짜의 할 일·루틴 조회, 등록, 완료 체크** (중요 표시·수정은 제외)

---

## 1. 작업 범위 규칙 (가장 중요)

| 경로 | 권한 | 비고 |
| --- | --- | --- |
| `extension/` | **읽기 + 쓰기** | 모든 신규 개발은 여기서만 한다 |
| `web/` | **읽기 (주 참고 대상)** / 쓰기는 예외적으로 | 수정이 필요하면 **반드시 먼저 사용자에게 이유를 설명하고 허락을 받은 뒤** 수정한다 |
| `back/`, `android/`, `unity/` | **읽기 전용** | 참고만 한다. 절대 수정·생성·삭제하지 않는다 |
| 레포 루트 파일 (`README.md`, `.gitignore`, `biome.json`, `.github/`, `.agents/`, `.codex/` 등) | **읽기 전용** | 절대 수정하지 않는다 |

- `npm install`, 포맷터(`biome format --write`), 코드 생성기 등도 **`extension/` 안에서만** 실행한다. 레포 루트나 `web/`에서 쓰기 동작이 있는 명령을 실행하지 않는다.
- `web/.env`에는 실제 키가 들어 있다. 값을 출력하거나 다른 파일에 복사하지 않는다 (키 이름만 참고).
- 이 규칙은 `extension/.claude/settings.json`의 deny 규칙으로도 일부 강제된다. 단, Claude를 레포 루트에서 실행하면 그 설정은 적용되지 않으니 **Claude는 `extension/` 폴더에서 실행하는 것을 권장**한다.

---

## 2. 프로젝트 개요

- **todo-with-spirits**: 할 일을 완료하면 "정령(spirit)"을 얻고 숲(forest)을 키우는 투두 앱.
- 레포 구성 (모노레포):
  - `web/` — React 웹 프론트엔드 (Vercel 배포). **익스텐션의 원본**
  - `back/` — Spring Boot(Java/Gradle) 백엔드 API. **실제 API 주소: `https://custom-lucky-draw.store`** (`web/.env`의 마지막 `VITE_API_BASE_URL` 값. `web/src/lib/api.ts`의 기본값 onrender.com은 2026-10-28 기준 응답 없음)
  - `android/` — 안드로이드 앱
  - `unity/` — 숲 화면 Unity 프로젝트 (WebGL 빌드가 `web/public/forest-webgl`에 들어감)
- 익스텐션은 **같은 백엔드 API를 그대로 사용**한다. 백엔드는 수정할 수 없으므로 익스텐션 쪽에서 맞춘다.

---

## 3. 참고 대상: `web/` 요약

### 스택
- Vite 8 + React 19 + TypeScript 6
- Tailwind CSS v4 (`@tailwindcss/vite`) + shadcn/ui (style: `base-nova`, `@base-ui/react` 기반) + lucide-react 아이콘
- 라우팅: react-router-dom v7 (`createBrowserRouter`)
- 서버 상태: @tanstack/react-query v5
- 클라이언트 상태: zustand v5 (`persist` → **localStorage**)
- HTTP: ky v2 (`web/src/lib/api.ts`)
- 폼: react-hook-form + zod v4
- 토스트: sonner
- 소셜 로그인: Google(`@react-oauth/google`), Kakao(JS SDK를 `index.html`에서 CDN 로드)
- 포맷/린트: Biome (`web/biome.json` — space 2칸, single quote, 세미콜론 없음, trailing comma)
- 경로 별칭: `@/*` → `src/*`

### 폴더 구조 (FSD 비슷한 레이어 구조)
```
web/src/
  app/       router.tsx, route-guards.tsx
  page/      라우트 단위 페이지 (TodayPage, PlanPage, RecordPage, ForestPage, ...)
  feature/   사용자 행동 단위 (auth, task, spirit, setting, user) — api/mutate, model, ui
  entity/    도메인 단위 (task, record, spirit) — api/query, model/type, ui
  shared/    ui(shadcn 컴포넌트), layout, lib, routes, assets
  lib/       api.ts(ky 클라이언트), utils.ts(cn)
  hooks/
```

### 주요 화면 (`web/src/shared/routes/index.ts`)
`/login`, `/join`, `/today`, `/plan`, `/record`, `/forest`, `/mypage`, `/mypage/setting/*`

### API / 인증 핵심 (`web/src/lib/api.ts`, `web/src/feature/auth/model/authStore.ts`)
- 응답 형태: `{ result: 'success', detail: T }` | `{ result: 'fail', detail: { status, errorCode, description[] } }`
- `Authorization: Bearer <accessToken>` 헤더, 401이면 `POST api/auth/reissue`로 토큰 재발급 후 재시도
- 토큰은 zustand `persist`(키 `auth`)로 localStorage에 저장
- 환경변수 키: `VITE_API_BASE_URL`, `VITE_GOOGLE_CLIENT_ID`, `VITE_KAKAO_APP_KEY`, `VITE_KAKAO_JS_KEY`, `VITE_KAKAO_REST_API_KEY`

---

## 4. 웹 → 익스텐션 전환 시 주의할 점

웹 코드를 그대로 복사하면 안 되는 부분들. 개발 중 이 항목들을 항상 확인한다.

1. **Manifest V3 CSP**: 원격 스크립트 로드 금지. `web/index.html`처럼 Kakao SDK를 CDN `<script>`로 넣을 수 없다 → 번들에 포함하거나 다른 방식(REST/`chrome.identity`) 사용.
2. **저장소**: localStorage는 팝업/사이드패널 등 익스텐션 페이지끼리는 공유되지만 **service worker(background)에서는 접근 불가**. 백그라운드에서도 토큰이 필요하면 `chrome.storage.local`로 옮긴다 (zustand persist의 storage를 교체하는 방식 고려).
3. **소셜 로그인**: 웹의 리다이렉트 콜백(`/oauth/kakao`) 방식은 익스텐션에서 그대로 동작하지 않는다 → `chrome.identity.launchWebAuthFlow` 검토. 리다이렉트 URI(`https://<extension-id>.chromiumapp.org/`)가 Google/Kakao 콘솔과 백엔드에 등록되어 있어야 할 수 있음 → **백엔드/콘솔 변경이 필요하면 사용자에게 알린다** (직접 수정 금지).
4. **라우팅**: 익스텐션 페이지는 `chrome-extension://` 파일 경로라 `createBrowserRouter`의 history 라우팅이 새로고침 시 깨진다 → `createHashRouter` 또는 `createMemoryRouter` 사용.
5. **CORS / host_permissions**: API 도메인을 `host_permissions`에 등록해야 한다. 또한 백엔드는 `chrome-extension://` Origin을 **403 "Invalid CORS request"로 막는다** (2026-10-28 curl로 확인). 임시로 `src/entrypoints/background.ts`의 declarativeNetRequest 규칙이 API 요청의 Origin을 `https://todo-with-spirits.vercel.app`으로 바꾼다. 🔧 개선 필요 (spec 9번).
6. **화면 크기**: 팝업은 최대 약 800×600. 웹의 사이드바 레이아웃(`AppLayout`)은 그대로 맞지 않으므로 팝업/사이드패널용 레이아웃이 필요.
7. **Unity WebGL 숲 화면(`/forest`)**: 용량이 크고 COOP/COEP 헤더·wasm 실행 정책 문제가 있어 익스텐션에서는 **초기 범위에서 제외**하거나 웹으로 링크하는 방식을 우선 고려.
8. **환경변수/키**: `import.meta.env.VITE_*`는 빌드 시 번들에 박힌다. 익스텐션 번들은 누구나 열어볼 수 있으므로 비밀 키(REST API 키 등)는 넣지 않는다.

---

## 5. 스택 (확정: 2026-10-28)

- **WXT 0.21** (Vite 8 기반 익스텐션 프레임워크) + `@wxt-dev/module-react`
- React 19 + TypeScript 6 (web과 맞춤. TS 7로 올리지 않는다)
- Tailwind CSS v4 (`@tailwindcss/vite`, `src/index.css`)
- Biome 2.5 (`extension/biome.json`, 레포 루트 설정과 독립 — `"root": true`)
- ky v2 (HTTP), @tanstack/react-query v5 (서버 상태)
- 이후 필요 시 web과 같은 라이브러리(zustand, zod, shadcn 등)를 추가한다. **새 패키지 추가 전에는 사용자에게 확인.**

### 폴더 구조
```
extension/
  wxt.config.ts          manifest 설정 (권한, host_permissions 등)은 여기서 한다. manifest.json을 직접 만들지 않는다
  src/                   (srcDir) `@/` 별칭 = src/
    entrypoints/
      background.ts      사이드패널 열기 설정, CORS 우회 규칙
      sidepanel/         사이드패널 화면 (index.html, main.tsx, App.tsx)
      web-auth.content.ts  웹(vercel) localStorage의 로그인 정보를 익스텐션 저장소로 복사
    page/                화면 단위 (TodayPage)
    feature/auth/        authStorage(chrome.storage.local 'auth'), useAuth, LoginScreen
    feature/task/        등록·완료/해제 API, TodayTaskList(목록, 낙관적 업데이트), TaskItem, TaskInputDock(할 일/루틴 등록 폼)
    entity/task/         할 일 타입, API 조회 함수
    shared/lib/          공용 유틸 (date)
    lib/api.ts           ky 클라이언트 (토큰 헤더, 401 → 로그아웃, 재발급 안 함)
    lib/config.ts        API 주소 등 상수
    index.css
  public/icon/           익스텐션 아이콘 (현재 WXT 기본 아이콘)
  docs/spec.md           기획 문서
  .output/chrome-mv3/    빌드 결과 (git 제외)
```
- 엔트리포인트 규칙: `src/entrypoints/` 아래 파일/폴더 이름으로 WXT가 종류를 판단한다 (`background.ts`, `sidepanel/`, `*.content.ts` 등).
- `defineBackground`, `defineContentScript`, `browser` 등은 WXT가 자동 import한다.

### 명령어 (모두 `extension/`에서 실행)
| 명령 | 설명 |
| --- | --- |
| `npm run dev` | 익스텐션이 설치된 크롬을 띄우고, 코드 수정 시 자동 새로고침 |
| `npm run build` | `.output/chrome-mv3/`에 빌드 |
| `npm run compile` | 타입 체크 |
| `npm run check` | Biome 린트+포맷 검사 |
| `npm run zip` | 웹 스토어 업로드용 zip 생성 |

개발 시: `npm run dev` 실행 후 `chrome://extensions`에서 `extension/.output/chrome-mv3-dev`를 한 번 로드하면 이후 자동 반영된다 (`.output`은 숨김 폴더 → 파일 선택 창에서 `Cmd+Shift+.`). `web-ext`는 설치하지 않았으므로 브라우저 자동 실행은 안 된다 (localhost:3000은 WXT 내부 개발 서버라 열 필요 없음).

수동 설치: `npm run build` → `chrome://extensions` → 개발자 모드 → "압축해제된 확장 프로그램을 로드합니다" → `extension/.output/chrome-mv3` 선택.

작업 후에는 `npm run compile`, `npm run check`, `npm run build`가 통과하는지 확인한다.

---

## 6. 코드 컨벤션 (web 기준을 따른다)

- TypeScript, React 함수형 컴포넌트, named export 우선
- Biome 포맷: space 2칸, single quote, 세미콜론 없음, trailing comma `all`
- 경로 별칭 `@/*` 사용
- 기능 코드가 생기면 `src/` 아래에 web과 같은 레이어(`app / page / feature / entity / shared`)를 만들어 웹 코드를 옮기기 쉽게 한다 (엔트리포인트는 `src/entrypoints/`)
- 웹 코드를 가져올 때는 **`web/`에서 import하지 말고 `extension/` 안으로 복사**한 뒤 익스텐션 환경에 맞게 고친다 (web을 건드리지 않기 위함)
- 커밋 메시지는 레포 관례를 따른다: `feat(extension): ...`, `fix(extension): ...`, `refactor(extension): ...`

---

## 7. 진행 상황

- [x] `extension/` 폴더 및 CLAUDE.md 생성
- [x] 익스텐션 형태 결정 → 사이드패널
- [x] MVP 범위 결정 → `docs/spec.md`
- [x] 스택 결정 → WXT + React + TS + Tailwind
- [x] 프로젝트 스캐폴딩 (사이드패널 + background + CORS 우회 규칙)
- [x] 실제 크롬에서 사이드패널 열기 / API 연결 테스트 확인 (2026-10-28, Origin 우회 규칙으로 401 응답 = CORS 통과)
- [x] 로그인 구현 (B 방식: 웹 localStorage → content script → chrome.storage.local) — 실제 크롬 확인 완료 (2026-10-28)
- [x] 오늘 할 일·루틴 목록 조회 — 확인 완료
- [x] 할 일·루틴 등록 — 확인 완료 (폼은 react-hook-form/zod 없이 useState로 구현)
- [x] 완료 체크 — 확인 완료

**✅ MVP (Step 1) 완료 — 2026-10-28 실제 크롬에서 전체 확인.** 다음은 docs/spec.md 6번 로드맵 (Step 2) 및 9번 개선 필요 항목.

작업이 진행되면 이 체크리스트와 결정 사항을 갱신한다.
